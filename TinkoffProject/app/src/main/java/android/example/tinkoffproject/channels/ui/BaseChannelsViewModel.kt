package android.example.tinkoffproject.channels.ui

import android.example.tinkoffproject.channels.model.ChannelItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

abstract class BaseChannelsViewModel : ViewModel() {

    abstract val allChannels: MutableList<ChannelItem>
    protected var compositeDisposable = CompositeDisposable()
    protected val topics: MutableMap<String, MutableList<ChannelItem>> = mutableMapOf()

    protected val searchObservable: Observable<String> by lazy { MainChannelsViewModel.querySearch }
    private val queryChannelClick = PublishSubject.create<Int>()
    private val disposables = mutableMapOf<String, Disposable>()

    private val currentChannels: List<ChannelItem>
        get() = _uiState.value?.channels ?: emptyList()

    private val _itemToUpdate: MutableLiveData<Int> =
        MutableLiveData<Int>()
    val itemToUpdate: LiveData<Int> = _itemToUpdate

    protected val _uiState = MutableLiveData<ChannelsUiState>()
    val uiState: LiveData<ChannelsUiState>
        get() = _uiState

    data class ChannelsUiState(
        var channels: List<ChannelItem> = emptyList(),
        val isLoading: Boolean = false,
    )

    abstract fun loadChannels()

    private fun filterChannels(input: String) = allChannels.filter {
        (it.name.contains(input, true) && !it.isTopic) || (it.parentChannel?.contains(
            input,
            true
        ) == true)
    }

    protected fun subscribeToSearch() {
        searchObservable
            .map { query -> query.trim() }
            .scan { previous, current ->
                if (current.isBlank() && previous.isNotBlank())
                    resetSearch()
                current
            }
            .filter { it.isNotBlank() }
            .distinctUntilChanged()
            .doOnNext {
                _uiState.value = _uiState.value?.copy(isLoading = true)
            }
            .debounce(1500, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.io())
            .switchMapSingle { input ->
                Single.fromCallable { filterChannels(input) }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    _uiState.value = _uiState.value?.copy(channels = it, isLoading = false)
                })
            .addTo(compositeDisposable)
    }

    private fun resetSearch() {
        compositeDisposable.clear()
        _uiState.value = _uiState.value?.copy(channels = allChannels, isLoading = false)
        subscribeToSearch()
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
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    _uiState.value = _uiState.value?.copy(channels = it.second)
                    _itemToUpdate.value = it.first!!
                })
    }

    private fun showOrHideTopics(position: Int): Pair<Int, List<ChannelItem>> {
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
        const val KEY_CLICK_CHANNEL = "channel_click"
    }
}