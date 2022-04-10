package android.example.tinkoffproject.channels.ui.my

import android.example.tinkoffproject.channels.model.ChannelItem
import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel
import android.example.tinkoffproject.network.NetworkClient
import android.example.tinkoffproject.utils.makePublishSubject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class MyChannelsViewModel : BaseChannelsViewModel() {
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
        _isLoading.value = true
        queryGetChannels.onNext(Unit)
    }

    override fun onCleared() {
        getSubscriptionStatusDisposable?.dispose()
        super.onCleared()
    }
}