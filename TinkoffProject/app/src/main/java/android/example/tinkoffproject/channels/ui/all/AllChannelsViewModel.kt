package android.example.tinkoffproject.channels.ui.all

import android.example.tinkoffproject.channels.model.ChannelItem
import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel
import android.example.tinkoffproject.network.NetworkClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject

class AllChannelsViewModel : BaseChannelsViewModel() {
    override var getChannelsDisposable: Disposable? = null
    override val queryGetChannels: PublishSubject<Unit>
    override val allChannels = mutableListOf<ChannelItem>()

    init {
        queryGetChannels = NetworkClient.makePublishSubject()
        subscribeGetTopics()
        subscribeGetChannels()
        subscribeToSearch()
        subscribeChannelClick()
        _isLoaded.value = false
    }

    override fun loadChannels() {
        _isLoading.value = true
        queryGetChannels.onNext(Unit)
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