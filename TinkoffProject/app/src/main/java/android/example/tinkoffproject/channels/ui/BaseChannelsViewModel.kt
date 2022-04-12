package android.example.tinkoffproject.channels.ui

import android.example.tinkoffproject.channels.model.db.ChannelDAO
import android.example.tinkoffproject.channels.model.db.ChannelEntity
import android.example.tinkoffproject.channels.model.network.ChannelItem
import android.example.tinkoffproject.chat.ui.SingleLiveEvent
import android.example.tinkoffproject.network.NetworkClient.client
import android.example.tinkoffproject.utils.makePublishSubject
import android.example.tinkoffproject.utils.makeSearchObservable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

abstract class BaseChannelsViewModel(protected val channelsDAO: ChannelDAO) : ViewModel() {

    protected abstract val allChannels: MutableList<ChannelItem>
    protected abstract val queryGetChannels: PublishSubject<Unit>
    protected abstract var getChannelsDisposable: Disposable?
    private val searchObservable: Observable<String> by lazy { MainChannelsViewModel.querySearch }
    private val queryGetTopics: PublishSubject<Pair<Int, String>> by lazy { makePublishSubject<Pair<Int, String>>() }
    private var compositeDisposable = CompositeDisposable()
    private val disposables = mutableMapOf<String, Disposable>().apply {
        this[KEY_INPUT] = searchObservable
            .map { query -> query.trim() }
            .distinctUntilChanged()
            .filter { it.isNotBlank() }
            .subscribeBy(onNext = {
                if (this[KEY_SEARCH]?.isDisposed == true) {
                    subscribeToSearch()
                    compositeDisposable.clear()
                }
            })
    }

    protected val topics: MutableMap<String, List<ChannelItem>> = mutableMapOf()
    private val queryReset = PublishSubject.create<String>()
    private val queryChannelClick = PublishSubject.create<Int>()

    protected val getChannelsObservable: Observable<Unit> by lazy {
        queryGetChannels
            .doOnNext { allChannels.clear() }
            .observeOn(Schedulers.io())
            .switchMapSingle {
                client.getAllStreams()
                    .map { channels ->
                        allChannels.apply { addAll(channels.channelsList) }
                        channelsDAO.insertChannels(channels.channelsList.map {
                            ChannelEntity(
                                it.streamID, it.name, it.isTopic, it.isExpanded, it.parentChannel
                            )
                        }).subscribe()
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
    }

    private val _isChannelClicked = SingleLiveEvent<Boolean>()
    val isChannelClicked: LiveData<Boolean>
        get() = _isChannelClicked

    var currentChannels: List<ChannelItem> = emptyList()

    private val _itemToUpdate: MutableLiveData<Int> =
        MutableLiveData<Int>()
    val itemToUpdate: LiveData<Int> = _itemToUpdate

    protected val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    protected val _isLoaded = SingleLiveEvent<Boolean>()
    val isLoaded: LiveData<Boolean>
        get() = _isLoaded

    protected val _errorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    abstract fun loadChannels()

    abstract fun subscribeGetChannels()

    fun subscribeGetTopics() {
        disposables[KEY_GET_TOPICS]?.dispose()
        disposables[KEY_GET_TOPICS] = queryGetTopics
            .observeOn(Schedulers.io())
            .concatMap {
                Observable.just(it).delay(30, TimeUnit.MILLISECONDS)
            }
            .toFlowable(BackpressureStrategy.BUFFER)
            .onBackpressureBuffer(3000)
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
                        channelsDAO.insertChannels(topicsProcessed.map {
                            ChannelEntity(
                                it.streamID,
                                it.name,
                                it.isTopic,
                                it.isExpanded,
                                it.parentChannel
                            )
                        }).subscribe()
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = {
                _errorMessage.value = "Ошибка при загрузке каналов"
                subscribeGetTopics()
            })
    }

    private fun filterChannels(input: String) = allChannels.filter {
        (it.name.contains(
            Regex(
                input,
                RegexOption.IGNORE_CASE
            )
        ) && !it.isTopic) || (it.parentChannel?.contains(
            Regex(input, RegexOption.IGNORE_CASE)
        ) == true)
    }

    protected fun subscribeToSearch() {
        disposables[KEY_SEARCH] = makeSearchObservable(searchObservable) { resetSearch() }
            .doOnNext { _isLoading.value = true }
            .debounce(600, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.io())
            .switchMapSingle { input ->
                Single.fromCallable { filterChannels(input) }
                    .subscribeOn(Schedulers.io())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    currentChannels = it
                    _isLoading.value = false
                }, onError = {
                })
    }

    private fun subscribeReset() {
        queryReset
            .observeOn(Schedulers.io())
            .switchMapSingle { input ->
                Single.fromCallable { filterChannels(input) }
                    .subscribeOn(Schedulers.io())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    currentChannels = it
                    _isLoading.value = false
                    subscribeToSearch()
                    compositeDisposable.clear()
                }, onError = {
                })
            .addTo(compositeDisposable)
    }

    private fun resetSearch() {
        disposables[KEY_SEARCH]?.dispose()
        subscribeReset()
        queryReset.onNext(".")
    }

    fun clickChannel(position: Int) {
        queryChannelClick.onNext(position)
    }

    fun subscribeChannelClick() {
        disposables[KEY_CLICK_CHANNEL]?.dispose()
        disposables[KEY_CLICK_CHANNEL] = queryChannelClick
            .observeOn(Schedulers.io())
            .switchMapSingle { position ->
                Single.fromCallable { showOrHideTopics(position) }
                    .subscribeOn(Schedulers.io())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    currentChannels = it.second
                    _itemToUpdate.value = it.first!!
                    _isChannelClicked.value = true
                }, onError = {})
    }

    private fun showOrHideTopics(position: Int): Pair<Int, List<ChannelItem>> {
        val newList = mutableListOf<ChannelItem>().apply { addAll(currentChannels) }
        if (currentChannels[position].isExpanded)
            collapseTopics(position, newList)
        else
            expandTopics(position, newList)
        newList[position] =
            newList[position].copy(isExpanded = !currentChannels[position].isExpanded)
        allChannels[allChannels.indexOf(currentChannels[position])] =
            allChannels[allChannels.indexOf(currentChannels[position])].copy(isExpanded = newList[position].isExpanded)
        return Pair(position, newList)
    }

    private fun expandTopics(position: Int, newList: MutableList<ChannelItem>) {
        topics[currentChannels[position].name]?.let {
            newList.addAll(position + 1, it)
            allChannels.addAll(allChannels.indexOf(currentChannels[position]) + 1, it)
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

    override fun onCleared() {
        for (key in disposables.keys)
            disposables[key]?.dispose()
        getChannelsDisposable?.dispose()
        compositeDisposable.clear()
    }

    companion object {
        private const val KEY_CLICK_CHANNEL = "channel_click"
        private const val KEY_GET_TOPICS = "get topics"
        private const val KEY_SEARCH = "search"
        private const val KEY_INPUT = "input"
    }
}