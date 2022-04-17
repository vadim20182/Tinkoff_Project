package android.example.tinkoffproject.channels.ui.all

import android.example.tinkoffproject.channels.model.ChannelsRepository
import android.example.tinkoffproject.channels.model.db.ChannelEntity
import android.example.tinkoffproject.channels.model.network.ChannelItem
import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel
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

class AllChannelsViewModel(channelsRepository: ChannelsRepository<ChannelEntity.AllChannelsEntity>) :
    BaseChannelsViewModel<ChannelEntity.AllChannelsEntity>(channelsRepository) {
    private val queryGetTopics: PublishSubject<Pair<Int, String>> by lazy { makePublishSubject<Pair<Int, String>>() }
    override val channelEntityType: Int = 1
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

    override fun subscribeGetTopics() {
        queryGetTopics
            .observeOn(Schedulers.io())
            .concatMap {
                Observable.just(it).delay(100, TimeUnit.MILLISECONDS)
            }
            .flatMapSingle { input ->
                client.getTopicsForStream(input.first)
                    .map { topicsResponse ->
                        val topicsProcessed = topicsResponse.channelsList.map {
                            it.copy(
                                isTopic = true,
                                parentChannel = input.second
                            )
                        }
                        topics[input.second] = topicsProcessed
                        channelsRepository.insertChannelsIgnore(topicsProcessed.map {
                            convertChannelFromNetworkToDb(
                                it,
                                channelEntityType
                            ) as ChannelEntity.AllChannelsEntity
                        })
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = {
                _errorMessage.value = "Ошибка при загрузке каналов"
            })
            .addTo(compositeDisposable)
    }

    override fun loadChannels() {
        channelsRepository.loadChannelsFromDb()
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
            }, onError = {
            }).addTo(compositeDisposable)
    }

    override fun subscribeGetChannels() {
        getChannelsObservable
            .switchMapSingle {
                client.getAllStreams()
                    .map { channels ->
                        allChannels.clear()
                        allChannels.addAll(channels.channelsList)
                        for (channel in channels.channelsList) {
                            queryGetTopics.onNext(
                                Pair(
                                    channel.streamID,
                                    channel.name
                                )
                            )
                        }
                    }
            }
            .map {
                channelsRepository.insertChannelsIgnore(allChannels.map {
                    convertChannelFromNetworkToDb(
                        it,
                        channelEntityType
                    ) as ChannelEntity.AllChannelsEntity
                })
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = {
                _errorMessage.value = "Ошибка при загрузке каналов"
            })
            .addTo(compositeDisposable)
    }

    private fun subscribeToDbUpdates() {
        channelsRepository.getChannelsFromDb()
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

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}