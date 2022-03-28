package android.example.tinkoffproject.channels.ui

import android.example.tinkoffproject.channels.model.ChannelItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

abstract class BaseChannelsViewModel : ViewModel() {

    abstract val allChannels: MutableList<ChannelItem>
    protected var compositeDisposable = CompositeDisposable()
    protected val topics: MutableMap<String, MutableList<ChannelItem>> = mutableMapOf()

    protected val _channels: MutableLiveData<List<ChannelItem>> =
        MutableLiveData<List<ChannelItem>>()
    val channels: LiveData<out List<ChannelItem>> = _channels

    protected val _isAsyncTaskCompleted: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>()
    val isAsyncTaskCompleted: LiveData<Boolean> = _isAsyncTaskCompleted

    private val querySearch = PublishSubject.create<String>()
    private val queryChannelClick = PublishSubject.create<Int>()
    private val disposables = mutableMapOf<String, Disposable>()

    private val currentChannels: List<ChannelItem>
        get() = _channels.value ?: emptyList()

    private val _itemToUpdate: MutableLiveData<Int> =
        MutableLiveData<Int>()
    val itemToUpdate: LiveData<Int> = _itemToUpdate

    private var _currentSearch = ""
    val currentSearch
        get() = _currentSearch

    abstract fun loadChannels()

    fun resetSearch() {
        _currentSearch = ""
        disposables[KEY_SEARCH_ACTION]?.dispose()
        _isAsyncTaskCompleted.value = true
        _channels.value = allChannels
        subscribeSearch()
    }

    private fun filterChannels(input: String) = allChannels.filter {
        (it.name.contains(input, true) && !it.isTopic) || (it.parentChannel?.contains(
            input,
            true
        ) == true)
    }

    fun searchChannels(input: String) {
        _currentSearch = input
        _isAsyncTaskCompleted.value = false
        querySearch.onNext(input)
    }

    private fun subscribeSearch() {
        disposables[KEY_SEARCH_ACTION]?.dispose()
        disposables[KEY_SEARCH_ACTION] = querySearch
            .map { query -> query.trim() }
            .debounce(1500, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.io())
            .switchMapSingle { input ->
                Single.fromCallable { filterChannels(input) }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    _channels.value = it
                    _isAsyncTaskCompleted.value = true
                })
    }

    fun clickChannel(position: Int) {
        queryChannelClick.onNext(position)
    }

    private fun subscribeChannelClick() {
        disposables[KEY_CLICK_CHANNEL]?.dispose()
        disposables[KEY_CLICK_CHANNEL] = queryChannelClick
            .observeOn(Schedulers.io())
            .switchMapSingle { position ->
                Single.fromCallable { showOrHideTopics(position) }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    _channels.value = it.second!!
                    _itemToUpdate.value = it.first!!
                })
    }

    fun subscribe() {
        subscribeChannelClick()
        subscribeSearch()
    }

    private fun showOrHideTopics(position: Int): Pair<Int, MutableList<ChannelItem>> {
        val newList = mutableListOf<ChannelItem>().apply { addAll(currentChannels) }
        if (currentChannels[position].isExpanded)
            collapseTopics(position, newList)
        else
            expandTopics(position, newList)
        newList[position].isExpanded = !currentChannels[position].isExpanded
        allChannels[allChannels.indexOf(currentChannels[position])].isExpanded =
            newList[position].isExpanded
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
        compositeDisposable.clear()
    }

    companion object {
        const val KEY_SEARCH_ACTION = "search"
        const val KEY_CLICK_CHANNEL = "channel_click"
    }
}