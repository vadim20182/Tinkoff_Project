package android.example.tinkoffproject.channels.ui.my

import android.example.tinkoffproject.channels.model.ChannelsRepository
import android.example.tinkoffproject.channels.model.db.ChannelEntity
import android.example.tinkoffproject.channels.model.network.ChannelItem
import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel
import android.example.tinkoffproject.network.NetworkClient
import android.example.tinkoffproject.network.NetworkClient.client
import android.example.tinkoffproject.utils.convertChannelFromDbToNetwork
import android.example.tinkoffproject.utils.convertChannelFromNetworkToDb
import android.example.tinkoffproject.utils.makePublishSubject
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class MyChannelsViewModel(channelsRepository: ChannelsRepository<ChannelEntity.MyChannelsEntity>) :
    BaseChannelsViewModel<ChannelEntity.MyChannelsEntity>(channelsRepository) {
    private val queryGetTopics: PublishSubject<Pair<Int, String>> by lazy { makePublishSubject<Pair<Int, String>>() }
    override val channelEntityType: Int = 0
    override val queryGetChannels: PublishSubject<Unit> by lazy { makePublishSubject<Unit>() }
    override val allChannels = mutableListOf<ChannelItem>()

    private val compositeDisposable = CompositeDisposable()


    init {
        subscribeGetTopics()
        subscribeGetChannels()
        subscribeToSearch()
        subscribeChannelClick()
        _isLoaded.value = false
    }

    override fun subscribeGetChannels() {
        getChannelsObservable
            .switchMapSingle {
                client.getAllStreams()
                    .map { channels ->
                        allChannels.clear()
                        allChannels.addAll(channels.channelsList)
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = { findMyChannels() }, onError = {
                _errorMessage.value = "Ошибка при загрузке каналов"
            })
            .addTo(compositeDisposable)
    }

    override fun subscribeGetTopics() {
        queryGetTopics
            .observeOn(Schedulers.io())
            .flatMapSingle { (streamId, name) ->
                client.getTopicsForStream(streamId)
                    .map { topicsResponse ->
                        val channelsProcessed = topicsResponse.channelsList.map {
                            it.copy(
                                isTopic = true,
                                parentChannel = name
                            )
                        }
                        topics[name] = channelsProcessed
                        channelsRepository.insertChannelsIgnore(channelsProcessed.map {
                            convertChannelFromNetworkToDb(
                                it,
                                channelEntityType, true
                            ) as ChannelEntity.MyChannelsEntity
                        })
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {}, onError = {
                _errorMessage.value = "Ошибка при загрузке каналов"
            }).addTo(compositeDisposable)
    }

    private fun findMyChannels() {
        Observable.fromIterable(allChannels.filter { it.parentChannel == "\t" })
            .observeOn(Schedulers.io())
            .flatMapSingle { channel ->
                client.getSubscriptionStatus(
                    channel.streamID,
                    NetworkClient.MY_USER_ID
                )
                    .map { status ->
                        if (!status.isSubscribed)
                            allChannels.remove(channel)
                        else
                            queryGetTopics.onNext(Pair(channel.streamID, channel.name))
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onComplete = {
                channelsRepository.insertChannelsIgnore(allChannels.map {
                    convertChannelFromNetworkToDb(
                        it,
                        channelEntityType, true
                    ) as ChannelEntity.MyChannelsEntity
                })
            }, onError = {
                _errorMessage.value = "Ошибка при загрузке подписок"
            })
            .addTo(compositeDisposable)
    }


    private fun subscribeToDbUpdates() {
        channelsRepository.getMyChannelsFromDb()
            .map { dbList ->
                val dbResponse = dbList.map {
                    convertChannelFromDbToNetwork(it)
                }
                Pair(
                    dbResponse,
                    dbResponse.filter { !it.isTopic })
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = { (all, current) ->
                if (all.isNotEmpty()) {
                    allChannels.clear()
                    allChannels.addAll(current)
                    currentChannels = current
                    _isLoading.value = false
                }
            }, onError = {
            }).addTo(compositeDisposable)
    }

    override fun loadChannels() {
        channelsRepository.loadMyChannelsFromDb()
            .map { dbList ->
                Triple(dbList.map {
                    convertChannelFromDbToNetwork(it)
                }, dbList.map {
                    convertChannelFromDbToNetwork(it)
                }.filter { !it.isTopic }, dbList.map {
                    convertChannelFromDbToNetwork(it)
                }.filter { it.parentChannel == "\t" })
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = { (all, current, channelsProcessed) ->
                if (all.isNotEmpty()) {
                    allChannels.clear()
                    allChannels.addAll(current)
                    for (parent in channelsProcessed)
                        topics[parent.name] = all.filter { it.parentChannel == parent.name }
                    currentChannels = current
                    _isLoading.value = false
                } else {
                    _isLoading.value = true
                }
                if (isLoaded.value == false) {
                    queryGetChannels.onNext(Unit)
                    _isLoaded.value = true
                }
                subscribeToDbUpdates()
            }).addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}