package android.example.tinkoffproject.channels.ui.all

import android.example.tinkoffproject.channels.model.db.ChannelDAO
import android.example.tinkoffproject.channels.model.network.ChannelItem
import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel
import android.example.tinkoffproject.utils.makePublishSubject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class AllChannelsViewModel(channelsDAO: ChannelDAO) :
    BaseChannelsViewModel(channelsDAO) {
    override var getChannelsDisposable: Disposable? = null
    override val queryGetChannels: PublishSubject<Unit>
    override val allChannels = mutableListOf<ChannelItem>()

    init {
        queryGetChannels = makePublishSubject()
        subscribeGetTopics()
        subscribeGetChannels()
        subscribeToSearch()
        subscribeChannelClick()
        _isLoaded.value = false
    }

    override fun loadChannels() {
        channelsDAO.getAllChannels()
            .subscribeOn(Schedulers.io())
            .map { dbList ->
                Pair(dbList.map {
                    ChannelItem(
                        it.name,
                        it.isTopic,
                        it.isExpanded,
                        it.parentChannel,
                        it.streamID
                    )
                }, dbList.map {
                    ChannelItem(
                        it.name,
                        it.isTopic,
                        it.isExpanded,
                        it.parentChannel,
                        it.streamID
                    )
                }.filter { it.isExpanded || (!it.isTopic) })
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = { (all, current) ->
                if (all.isNotEmpty()) {
                    allChannels.clear()
                    allChannels.addAll(all)
                    for (parent in allChannels.filter { it.parentChannel == null })
                        topics[parent.name] = allChannels.filter { it.parentChannel == parent.name }
                    currentChannels = current
                    _isLoading.value = false
                    _isLoaded.value = true
                } else {
                    _isLoading.value = true
                    queryGetChannels.onNext(Unit)
                }
            })
//        queryGetChannels.onNext(Unit)
    }

    override fun subscribeGetChannels() {
        getChannelsDisposable?.dispose()
        getChannelsDisposable = getChannelsObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                currentChannels = allChannels
                _isLoading.value = false
                _isLoaded.value = true
            }, onError = {
                _errorMessage.value = "Ошибка при загрузке каналов"
                subscribeGetChannels()
            })
    }
}