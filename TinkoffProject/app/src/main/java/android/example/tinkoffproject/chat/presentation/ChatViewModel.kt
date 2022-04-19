package android.example.tinkoffproject.chat.presentation

import android.example.tinkoffproject.chat.data.ChatRepository
import android.example.tinkoffproject.chat.data.db.MessageEntity
import androidx.annotation.MainThread
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import io.reactivex.Flowable
import java.util.concurrent.atomic.AtomicBoolean

class ChatViewModel(
    private val stream: String,
    private val topic: String,
    private val chatRepository: ChatRepository
) : ViewModel() {

    fun getMessages(): Flowable<PagingData<MessageEntity>> {
        return chatRepository
            .getMessages()
            .cachedIn(viewModelScope)
    }
    override fun onCleared() {
        chatRepository.clearMessagesOnExit(stream, topic)
    }

}

/**
 * A lifecycle-aware observable that sends only new updates after subscription, used for events like
 * navigation and Snackbar messages.
 *
 *
 * This avoids a common problem with events: on configuration change (like rotation) an update
 * can be emitted if the observer is active. This LiveData only calls the observable if there's an
 * explicit call to setValue() or call().
 *
 *
 * Note that only one observer is going to be notified of changes.
 */
class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val mPending: AtomicBoolean = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        // Observe the internal MutableLiveData
        super.observe(owner) { value ->
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(value)
            }
        }
    }

    @MainThread
    override fun setValue(t: T?) {
        mPending.set(true)
        super.setValue(t)
    }
}