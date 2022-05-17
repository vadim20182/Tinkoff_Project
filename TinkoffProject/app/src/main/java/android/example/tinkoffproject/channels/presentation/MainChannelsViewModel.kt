package android.example.tinkoffproject.channels.presentation

import android.example.tinkoffproject.channels.data.repository.main.MainChannelsRepository
import android.example.tinkoffproject.utils.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class MainChannelsViewModel @Inject constructor(private val mainChannelsRepository: MainChannelsRepository) :
    ViewModel() {
    private val disposables by lazy { mutableMapOf<String, Disposable>() }

    private val _errorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    fun search(input: String) {
        querySearch.onNext(input)
    }

    fun addChannel(channelName: String, description: String?) {
        disposables[KEY_ADD_CHANNEL]?.dispose()
        disposables[KEY_ADD_CHANNEL] = mainChannelsRepository.createChannel(
            channelName,
            description
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    queryNewChannelCreated.onNext(Unit)
                },
                onError = { _errorMessage.value = "Ошибка при создании канала" }
            )
    }

    fun refresh() {
        queryRefresh.onNext(Unit)
    }

    override fun onCleared() {
        for (key in disposables.keys)
            disposables[key]?.dispose()
    }

    companion object {
        val querySearch = PublishSubject.create<String>()
        val queryRefresh = PublishSubject.create<Unit>()
        val queryNewChannelCreated = PublishSubject.create<Unit>()
        private const val KEY_ADD_CHANNEL = "add channel"
    }
}