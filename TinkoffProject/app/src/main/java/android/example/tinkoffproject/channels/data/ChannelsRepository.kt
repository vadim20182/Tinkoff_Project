package android.example.tinkoffproject.channels.data

import android.example.tinkoffproject.channels.data.db.ChannelEntity
import android.example.tinkoffproject.channels.data.db.ChannelsDAO
import android.example.tinkoffproject.channels.data.network.ChannelItem
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

class ChannelsRepository @Inject constructor(
    private val channelsDAO: ChannelsDAO,
    private val client: ApiService
) {

    val searchObservable: Observable<String> by lazy { MainChannelsViewModel.querySearch }
    val allChannels = mutableListOf<ChannelItem>()
    val queryGetChannels: PublishSubject<Unit> by lazy { makePublishSubject<Unit>() }
    private val queryGetTopics: PublishSubject<Pair<Int, String>> by lazy { makePublishSubject<Pair<Int, String>>() }
    var currentChannels: List<ChannelItem> = emptyList()
    val topics: MutableMap<String, List<ChannelItem>> = mutableMapOf()
    val queryReset = PublishSubject.create<String>()
    val queryChannelClick = PublishSubject.create<Int>()

    val getChannelsObservable: Observable<Unit> by lazy {
        queryGetChannels
            .observeOn(Schedulers.io())
    }

    val channelClickObservable: Observable<Pair<Int, List<ChannelItem>>> = queryChannelClick
        .observeOn(Schedulers.io())
        .switchMapSingle { position ->
            Single.fromCallable { showOrHideTopics(position) }
                .subscribeOn(Schedulers.io())
        }

    val resetSearchObservable: Observable<String> = queryReset
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

    val getAllTopicsObservable: Observable<Disposable> = queryGetTopics
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
                    insertChannelsIgnore(topicsProcessed.map {
                        convertChannelFromNetworkToDb(
                            it
                        )
                    })
                }
        }

    fun getFindMyChannelsObservable(): Observable<Any> =
        Observable.fromIterable(allChannels.filter { it.parentChannel == ChannelEntity.NO_PARENT })
            .observeOn(Schedulers.io())
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


    val getAllChannelsObservable: Observable<Disposable> = getChannelsObservable
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
            insertChannelsIgnore(allChannels.map {
                convertChannelFromNetworkToDb(
                    it
                )
            })
        }

    val getMyChannelsObservable = getChannelsObservable
        .switchMapSingle {
            client.getAllStreams()
                .map { channels ->
                    allChannels.clear()
                    allChannels.addAll(channels.channelsList)
                }
        }


    fun getChannelsFromDb() =
        channelsDAO.getAllChannels()
            .subscribeOn(Schedulers.io())

    fun getMyChannelsFromDb() =
        channelsDAO.getMyAllChannels()
            .subscribeOn(Schedulers.io())

    fun loadChannelsFromDb() =
        channelsDAO.loadAllChannels()
            .subscribeOn(Schedulers.io())

    fun loadMyChannelsFromDb() =
        channelsDAO.loadMyAllChannels()
            .subscribeOn(Schedulers.io())

    private fun insertChannelsIgnore(channels: List<ChannelEntity>): Disposable =
        channelsDAO.insertChannelsIgnore(channels)
            .subscribeOn(Schedulers.io())
            .subscribe()

    fun insertChannelsReplace(channels: List<ChannelEntity>): Disposable =
        channelsDAO.insertChannelsReplace(channels)
            .subscribeOn(Schedulers.io())
            .subscribe()

    fun filterChannels(input: String) = allChannels.filter {
        (it.name.contains(
            Regex(
                input,
                RegexOption.IGNORE_CASE
            )
        ) && !it.isTopic) || (it.parentChannel.contains(
            Regex(input, RegexOption.IGNORE_CASE)
        ))
    }

    private fun showOrHideTopics(position: Int): Pair<Int, List<ChannelItem>> {
        val newList =
            mutableListOf<ChannelItem>().apply { addAll(currentChannels) }
        if (currentChannels[position].isExpanded)
            collapseTopics(position, newList)
        else
            expandTopics(position, newList)
        newList[position] =
            newList[position].copy(isExpanded = !currentChannels[position].isExpanded)
        allChannels[allChannels.indexOf(currentChannels[position])] =
            allChannels[allChannels.indexOf(currentChannels[position])].copy(
                isExpanded = newList[position].isExpanded
            )
        return Pair(position, newList)
    }

    private fun expandTopics(position: Int, newList: MutableList<ChannelItem>) {
        topics[currentChannels[position].name]?.let {
            newList.addAll(position + 1, it)
            allChannels.addAll(
                allChannels.indexOf(
                    currentChannels[position]
                ) + 1, it
            )
        }
    }

    private fun collapseTopics(position: Int, newList: MutableList<ChannelItem>) {
        newList.removeAll {
            it.parentChannel == currentChannels[position].name
        }
        allChannels.removeAll {
            it.parentChannel == currentChannels[position].name
        }
    }
}