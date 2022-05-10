package android.example.tinkoffproject.channels.data.repository

import android.example.tinkoffproject.channels.data.db.ChannelEntity
import android.example.tinkoffproject.channels.data.db.ChannelsDAO
import android.example.tinkoffproject.channels.data.network.ChannelItem
import android.example.tinkoffproject.channels.di.my.MyStreams
import android.example.tinkoffproject.channels.ui.MainChannelsViewModel
import android.example.tinkoffproject.network.ApiService
import android.example.tinkoffproject.network.NetworkCommon
import android.example.tinkoffproject.utils.convertChannelFromNetworkToDb
import android.example.tinkoffproject.utils.makePublishSubject
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@MyStreams
class MyChannelsRepositoryImpl @Inject constructor(
    private val channelsDAO: ChannelsDAO,
    private val client: ApiService
) : ChannelsRepository {
    override val searchObservable: Observable<String> by lazy { MainChannelsViewModel.querySearch }
    override val allChannels = mutableListOf<ChannelItem>()
    val queryGetChannels: PublishSubject<Unit> by lazy { makePublishSubject<Unit>() }
    private val queryGetTopics: PublishSubject<Pair<Int, String>> by lazy { makePublishSubject<Pair<Int, String>>() }
    override var currentChannels: List<ChannelItem> = emptyList()
    override val topics: MutableMap<String, List<ChannelItem>> = mutableMapOf()
    override val queryReset = PublishSubject.create<String>()
    override val queryChannelClick = PublishSubject.create<Int>()

    private val getChannelsObservable: Observable<Unit> by lazy {
        queryGetChannels
            .observeOn(Schedulers.io())
    }

    override val channelClickObservable: Observable<Pair<Int, List<ChannelItem>>> =
        queryChannelClick
            .observeOn(Schedulers.io())
            .switchMapSingle { position ->
                Single.fromCallable { showOrHideTopics(position) }
                    .subscribeOn(Schedulers.io())
            }

    override val resetSearchObservable: Observable<String> = queryReset
        .observeOn(Schedulers.io())

    val getMyTopicsObservable: Observable<Disposable> = queryGetTopics
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
                    insertChannelsReplace(channelsProcessed.map {
                        convertChannelFromNetworkToDb(
                            it, true
                        )
                    })
                }
        }


    fun getFindMyChannelsObservable(): Observable<Any> =
        Observable.fromIterable(allChannels.filter { it.parentChannel == ChannelEntity.NO_PARENT })
            .observeOn(Schedulers.io())
            .concatMap {
                Observable.just(it).delay(10, TimeUnit.MILLISECONDS)
            }
            .flatMapSingle { channel ->
                client.getSubscriptionStatus(
                    channel.streamID,
                    NetworkCommon.MY_USER_ID
                )
                    .map { status ->
                        if (!status.isSubscribed)
                            allChannels.remove(channel)
                        else
                            queryGetTopics.onNext(
                                Pair(
                                    channel.streamID,
                                    channel.name
                                )
                            )
                    }
            }


    val getMyChannelsObservable = getChannelsObservable
        .switchMapSingle {
            client.getAllStreams()
                .map { channels ->
                    allChannels.clear()
                    allChannels.addAll(channels.channelsList)
                }
        }


    fun getMyChannelsFromDb() =
        channelsDAO.getMyAllChannels()
            .subscribeOn(Schedulers.io())


    fun loadMyChannelsFromDb() =
        channelsDAO.loadMyAllChannels()
            .subscribeOn(Schedulers.io())

    fun insertChannelsReplace(channels: List<ChannelEntity>): Disposable =
        channelsDAO.insertChannelsReplace(channels)
            .subscribeOn(Schedulers.io())
            .subscribe()
}