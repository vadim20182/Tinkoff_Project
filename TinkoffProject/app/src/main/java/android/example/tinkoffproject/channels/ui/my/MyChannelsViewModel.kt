package android.example.tinkoffproject.channels.ui.my

import android.example.tinkoffproject.channels.model.db.ChannelDAO
import android.example.tinkoffproject.channels.model.db.ChannelEntity
import android.example.tinkoffproject.channels.model.network.ChannelItem
import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel
import android.example.tinkoffproject.network.NetworkClient
import android.example.tinkoffproject.utils.makePublishSubject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class MyChannelsViewModel(channelsDAO: ChannelDAO) : BaseChannelsViewModel(channelsDAO) {
    override var getChannelsDisposable: Disposable? = null
    override val queryGetChannels: PublishSubject<Unit>
    override val allChannels = mutableListOf<ChannelItem>()
    private var getSubscriptionStatusDisposable: Disposable? = null

    init {
        queryGetChannels = makePublishSubject()
        subscribeToSearch()
        subscribeChannelClick()
        subscribeGetTopics()
        subscribeGetChannels()
        _isLoaded.value = false
    }

    private fun findMyChannels() = Observable.fromIterable(allChannels)
        .observeOn(Schedulers.io())
        .flatMapSingle { channel ->
            NetworkClient.client.getSubscriptionStatus(
                channel.streamID,
                NetworkClient.MY_USER_ID
            )
                .map {
                    Pair(channel, it)
                }
        }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(onComplete = {
//            for (topics in topics.values)
//                allChannels.addAll(topics)
//
//            for (channel in allChannels.map {
//                ChannelEntity(
//                    it.streamID,
//                    it.name,
//                    it.isTopic,
//                    it.isExpanded,
//                    it.parentChannel,
//                    isMy = true
//                )
//            })
//                channelsDAO.updateChannels(channel.name, channel.parentChannel, true).subscribe()
//            channelsDAO.insertChannels(allChannels.map {
//                ChannelEntity(
//                    it.streamID,
//                    it.name,
//                    it.isTopic,
//                    it.isExpanded,
//                    it.parentChannel,
//                    isMy = true
//                )
//            }).subscribe()
//            channelsDAO.insertChannels(allChannels.map {
//                ChannelEntity(
//                    it.streamID, it.name, it.isTopic, it.isExpanded, it.parentChannel, true
//                )
//            }).subscribe()
            currentChannels = allChannels
            _isLoading.value = false
            _isLoaded.value = true
            getSubscriptionStatusDisposable?.dispose()
        }, onNext = { (channel, status) ->
            if (!status.isSubscribed)
                allChannels.remove(channel)
        }, onError = {
            _errorMessage.value = "Ошибка при загрузке подписок"
            getSubscriptionStatusDisposable?.dispose()
        })

    override fun subscribeGetChannels() {
        getChannelsDisposable?.dispose()
        getChannelsDisposable =
            getChannelsObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    getSubscriptionStatusDisposable = findMyChannels()
                }, onError = {
                    _errorMessage.value = "Ошибка при загрузке каналов"
                    subscribeGetChannels()
                })
    }

    override fun loadChannels() {
//        _isLoading.value = true
//        channelsDAO.getMyAllChannels()
//            .subscribeOn(Schedulers.io())
//            .map { dbList ->
//                Pair(dbList.map {
//                    ChannelItem(
//                        it.name,
//                        it.isTopic,
//                        it.isExpanded,
//                        it.parentChannel,
//                        it.streamID
//                    )
//                }, dbList.map {
//                    ChannelItem(
//                        it.name,
//                        it.isTopic,
//                        it.isExpanded,
//                        it.parentChannel,
//                        it.streamID
//                    )
//                }.filter { it.isExpanded || (!it.isTopic) })
//            }
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribeBy(onSuccess = { (all, current) ->
//                if (all.isNotEmpty()) {
//                    allChannels.clear()
//                    allChannels.addAll(all)
//                    for (parent in allChannels.filter { it.parentChannel == null })
//                        topics[parent.name] = allChannels.filter { it.parentChannel == parent.name }
//                    currentChannels = current
//                    _isLoading.value = false
//                    _isLoaded.value = true
//                } else {
//                    _isLoading.value = true
//                    queryGetChannels.onNext(Unit)
//                }
//            })
//        queryGetChannels.onNext(Unit)
    }

    override fun onCleared() {
        getSubscriptionStatusDisposable?.dispose()
        super.onCleared()
    }
}