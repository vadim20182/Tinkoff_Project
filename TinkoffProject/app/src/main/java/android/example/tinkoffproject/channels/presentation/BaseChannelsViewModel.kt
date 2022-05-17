package android.example.tinkoffproject.channels.presentation

import android.example.tinkoffproject.channels.data.network.ChannelItem
import android.example.tinkoffproject.channels.data.repository.tabs.ChannelsRepository
import android.example.tinkoffproject.utils.SingleLiveEvent
import android.example.tinkoffproject.utils.displayErrorMessage
import android.example.tinkoffproject.utils.makeSearchObservable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

abstract class BaseChannelsViewModel(val channelsRepository: ChannelsRepository) :
    ViewModel() {
    private var compositeDisposable = CompositeDisposable()
    protected val disposables by lazy {
        mutableMapOf<String, Disposable>().apply {
            this[KEY_INPUT] = channelsRepository.searchObservable
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
    }

    private val _isChannelClicked = SingleLiveEvent<Boolean>()
    val isChannelClicked: LiveData<Boolean>
        get() = _isChannelClicked

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

    protected abstract fun subscribeGetChannels()

    protected abstract fun subscribeGetTopics()

    protected fun subscribeAddChannel() {
        disposables[KEY_ADD_CHANNEL]?.dispose()
        disposables[KEY_ADD_CHANNEL] = channelsRepository.addChannelObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                _isLoaded.value = false
                loadChannels()
            })
    }

    protected fun subscribeRefresh() {
        disposables[KEY_REFRESH]?.dispose()
        disposables[KEY_REFRESH] = channelsRepository.refreshObservable
            .throttleFirst(4000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                subscribeGetChannels()
                subscribeGetTopics()
                _isLoaded.value = false
                loadChannels()
            }, onError = {
                _errorMessage.value = displayErrorMessage(it, "Ошибка при обновлении списка")
            })
    }

    protected fun subscribeToSearch() {
        disposables[KEY_SEARCH] =
            makeSearchObservable(channelsRepository.searchObservable) { resetSearch() }
                .doOnNext { _isLoading.value = true }
                .debounce(600, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .switchMapSingle { input ->
                    Single.fromCallable { channelsRepository.filterChannels(input) }
                        .subscribeOn(Schedulers.io())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        channelsRepository.currentChannels = it
                        _isLoading.value = false
                    }, onError = {})
    }

    private fun subscribeReset() {
        channelsRepository.resetSearchObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    channelsRepository.currentChannels = channelsRepository.allChannels
                    _isLoading.value = false
                    subscribeToSearch()
                    compositeDisposable.clear()
                }, onError = {})
            .addTo(compositeDisposable)
    }

    private fun resetSearch() {
        disposables[KEY_SEARCH]?.dispose()
        subscribeReset()
        channelsRepository.queryReset.onNext(".")
    }

    fun clickChannel(position: Int) {
        channelsRepository.queryChannelClick.onNext(position)
    }

    fun subscribeChannelClick() {
        disposables[KEY_CLICK_CHANNEL]?.dispose()
        disposables[KEY_CLICK_CHANNEL] = channelsRepository.channelClickObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    channelsRepository.currentChannels = it.second
                    _itemToUpdate.value = it.first!!
                    _isChannelClicked.value = true
                }, onError = {})
    }

    override fun onCleared() {
        for (key in disposables.keys)
            disposables[key]?.dispose()
        compositeDisposable.clear()
    }

    companion object {
        private const val KEY_CLICK_CHANNEL = "channel_click"
        private const val KEY_SEARCH = "search"
        private const val KEY_INPUT = "input"
        private const val KEY_ADD_CHANNEL = "add_channel"
        private const val KEY_REFRESH = "refresh"
        const val KEY_GET_MY_TOPICS = "get my topics"
        const val KEY_GET_MY_CHANNELS = "get my channels"
        const val KEY_LOAD_MY_CHANNELS = "load my channels"
        const val KEY_LOAD_ALL_CHANNELS = "load all channels"
        const val KEY_GET_ALL_TOPICS = "get all topics"
        const val KEY_GET_ALL_CHANNELS = "get all channels"
        const val KEY_ALL_SUBSCRIBE_TO_DB = "subscribe to db all"
        const val KEY_MY_SUBSCRIBE_TO_DB = "subscribe to db my"
    }

    protected class DbResult(val all: List<ChannelItem>, val channels: List<ChannelItem>)
}