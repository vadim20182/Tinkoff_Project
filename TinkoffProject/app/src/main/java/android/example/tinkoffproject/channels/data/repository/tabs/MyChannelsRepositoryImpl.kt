package android.example.tinkoffproject.channels.data.repository.tabs

import android.example.tinkoffproject.channels.data.db.ChannelEntity
import android.example.tinkoffproject.channels.data.db.ChannelsDAO
import android.example.tinkoffproject.channels.data.network.ChannelItem
import android.example.tinkoffproject.channels.di.my.MyStreams
import android.example.tinkoffproject.channels.presentation.MainChannelsViewModel
import android.example.tinkoffproject.network.ApiService
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
    override val addChannelObservable: Observable<Unit> by lazy { MainChannelsViewModel.queryNewChannelCreated }
    override val refreshObservable: Observable<Unit> by lazy { MainChannelsViewModel.queryRefresh }
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
        .concatMap {
            Observable.just(it).delay(2, TimeUnit.MILLISECONDS)
        }
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

    val getMyChannelsObservable: Observable<Unit> = getChannelsObservable
        .switchMapSingle {
            client.getSubscribedStreams()
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
            insertAndRemoveOld(allChannels.map {
                    convertChannelFromNetworkToDb(
                        it, true
                    )
            })
        }

    fun getMyChannelsFromDb() =
        channelsDAO.getMyAllChannels()
            .subscribeOn(Schedulers.io())

    fun loadMyChannelsFromDb() =
        channelsDAO.loadMyAllChannels()
            .subscribeOn(Schedulers.io())
    
    fun insertAndRemoveOld(channels: List<ChannelEntity>) =
        channelsDAO.insertAndRemoveInTransactionMy(channels)

    fun insertChannelsReplace(channels: List<ChannelEntity>): Disposable =
        channelsDAO.insertChannelsReplace(channels)
            .subscribeOn(Schedulers.io())
            .subscribe()
}