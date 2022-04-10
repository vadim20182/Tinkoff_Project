package android.example.tinkoffproject.chat.ui

import android.accounts.NetworkErrorException
import android.example.tinkoffproject.message.customviews.ReactionCustomView
import android.example.tinkoffproject.chat.model.UserMessage
import android.example.tinkoffproject.chat.model.UserReaction
import android.example.tinkoffproject.network.NetworkClient
import android.example.tinkoffproject.network.NetworkClient.client
import android.example.tinkoffproject.network.NetworkClient.makeJSONArray
import android.example.tinkoffproject.utils.EMOJI_MAP
import android.example.tinkoffproject.utils.makePublishSubject
import android.text.Html
import androidx.annotation.MainThread
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

class ChatViewModel(private val stream: String, private val topic: String) : ViewModel() {

    private val querySendMessage: PublishSubject<String> by lazy { makePublishSubject<String>() }
    private val queryGetMessages: PublishSubject<Unit> by lazy { makePublishSubject<Unit>() }
    private val queryAddReaction: PublishSubject<Pair<Int, String>> by lazy { makePublishSubject<Pair<Int, String>>() }
    private val queryRemoveReaction: PublishSubject<Pair<Int, String>> by lazy { makePublishSubject<Pair<Int, String>>() }

    private val disposables = mutableMapOf<String, Disposable>()

    private val currentMessages: List<UserMessage>
        get() = _uiState.value?.topicMessages ?: emptyList()

    private val _itemToUpdate: MutableLiveData<Int> =
        MutableLiveData<Int>()
    val itemToUpdate: LiveData<Int> = _itemToUpdate

    private val _messagesCount: MutableLiveData<Int> =
        MutableLiveData<Int>()
    val messagesCount: LiveData<Int> = _messagesCount

    private val _errorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _uiState = MutableLiveData<ChatUiState>()
    val uiState: LiveData<ChatUiState>
        get() = _uiState

    data class ChatUiState(
        var topicMessages: List<UserMessage> = emptyList(),
        val isLoading: Boolean = false,
    )

    init {
        _uiState.value = ChatUiState(isLoading = true)
        subscribeGetMessages()
        subscribeSendMessage()
        subscribeAddReaction()
        subscribeRemoveReaction()
        loadMessages()
    }

    fun sendMessage(message: String) {
        querySendMessage.onNext(message)
    }

    private fun subscribeGetMessages() {
        disposables[KEY_GET_MESSAGE]?.dispose()
        disposables[KEY_GET_MESSAGE] = queryGetMessages
            .observeOn(Schedulers.io())
            .flatMapSingle {
                client.getMessages(
                    makeJSONArray(
                        listOf(
                            Pair("stream", stream),
                            Pair("topic", topic)
                        )
                    )
                ).map { messagesResponse ->
                    val messagesProcessed = messagesResponse.messges.map {
                        it.copy(
                            messageText =
                            Html.fromHtml(it.messageText, Html.FROM_HTML_MODE_COMPACT).toString()
                                .trim()
                        )
                    }
                    for (msg in messagesProcessed) {
                        for (reaction in msg.allReactions) {
                            if (!msg.reactions.containsKey(reaction.emoji_name))
                                msg.reactions[reaction.emoji_name] = msg.allReactions.count {
                                    it.emoji_name == reaction.emoji_name
                                }
                            if (!msg.selectedReactions.containsKey(reaction.emoji_name)) {
                                msg.selectedReactions[reaction.emoji_name] =
                                    msg.allReactions.filter { it.emoji_name == reaction.emoji_name }
                                        .find { it.userId == NetworkClient.MY_USER_ID } != null
                            }
                        }
                    }
                    messagesProcessed
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                _uiState.value = _uiState.value?.copy(topicMessages = it, isLoading = false)
            }, onError = {
                _errorMessage.value = "Ошибка при загрузке сообщений"
                subscribeGetMessages()
            })
    }

    private fun subscribeSendMessage() {
        disposables[KEY_SEND_MESSAGE]?.dispose()
        disposables[KEY_SEND_MESSAGE] =
            querySendMessage
                .doOnNext {
                    val newList = mutableListOf<UserMessage>()
                    newList.addAll(currentMessages)
                    newList.add(
                        UserMessage(
                            userId = NetworkClient.MY_USER_ID,
                            name = "Vadim",
                            messageText = it,
                            date = Date().time / 1000,
                            isSent = false
                        )
                    )
                    _uiState.value = _uiState.value?.copy(topicMessages = newList)
                    _messagesCount.value = currentMessages.size
                }
                .observeOn(Schedulers.io())
                .concatMapSingle { message ->
                    client.sendPublicMessage(message, stream, topic)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        queryGetMessages.onNext(Unit)
                    },
                    onError = {
                        _errorMessage.value = "Ошибка отправки сообщения"
                        subscribeSendMessage()
                    }
                )
    }

    private fun subscribeAddReaction() {
        disposables[KEY_ADD_REACTION]?.dispose()
        disposables[KEY_ADD_REACTION] = queryAddReaction
            .observeOn(Schedulers.io())
            .concatMapSingle { (position, emoji_name) ->
                client.addReaction(
                    currentMessages[position].messageId,
                    emoji_name,
                    EMOJI_MAP[emoji_name]?.toString(16)?.lowercase() ?: "-1"
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = {
                _errorMessage.value = "Ошибка отправки реакции"
                subscribeAddReaction()
            })
    }

    private fun subscribeRemoveReaction() {
        disposables[KEY_REMOVE_REACTION]?.dispose()
        disposables[KEY_REMOVE_REACTION] = queryRemoveReaction
            .observeOn(Schedulers.io())
            .concatMapSingle { (position, emoji_name) ->
                client.removeReaction(
                    currentMessages[position].messageId,
                    emoji_name,
                    EMOJI_MAP[emoji_name]?.toString(16)?.lowercase() ?: "-1"
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = {
                _errorMessage.value = "Ошибка удаления реакции"
                subscribeRemoveReaction()
            })
    }

    fun reactionClicked(position: Int, emoji_name: String) {
        val newList = mutableListOf<UserMessage>()

        newList.addAll(currentMessages)
        val userReaction = UserReaction(
            newList[position].reactions,
            newList[position].selectedReactions
        )
        if (userReaction.isReactionSelected(emoji_name) == true) {
            queryRemoveReaction.onNext(Pair(position, emoji_name))
            if (userReaction.canReactionCountBeDecreased(emoji_name))
                userReaction.unselectReaction(emoji_name)
            else
                userReaction.deleteReaction(emoji_name)
        } else {
            queryAddReaction.onNext(Pair(position, emoji_name))
            userReaction.increaseReactionCount(emoji_name)
            userReaction.selectReaction(emoji_name)
        }
        _uiState.value = _uiState.value?.copy(topicMessages = newList)
        _itemToUpdate.value = position
    }

    fun addReaction(position: Int, emoji_name: String) {
        val newList = mutableListOf<UserMessage>()

        newList.addAll(currentMessages)
        val userReaction = UserReaction(
            currentMessages[position].reactions,
            currentMessages[position].selectedReactions
        )
        if (userReaction.isReactionSelected(emoji_name) != true) {
            userReaction.selectReaction(emoji_name)
            if (userReaction.isReactionAdded(emoji_name))
                userReaction.increaseReactionCount(emoji_name)
            else
                userReaction.addReaction(emoji_name)
        }
        _uiState.value = _uiState.value?.copy(topicMessages = newList)
        _itemToUpdate.value = position
        queryAddReaction.onNext(Pair(position, emoji_name))
    }

    override fun onCleared() {
        for (key in disposables.keys)
            disposables[key]?.dispose()
    }

    private fun loadMessages() {
        queryGetMessages.onNext(Unit)
    }

    companion object {
        private const val KEY_GET_MESSAGE = "get message"
        private const val KEY_SEND_MESSAGE = "send message"
        private const val KEY_ADD_REACTION = "add reaction"
        private const val KEY_REMOVE_REACTION = "remove reaction"
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