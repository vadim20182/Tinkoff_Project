package android.example.tinkoffproject.chat.ui

import android.example.tinkoffproject.chat.model.ChatRepository
import android.example.tinkoffproject.chat.model.network.UserMessage
import android.example.tinkoffproject.chat.model.UserReaction
import android.example.tinkoffproject.chat.model.db.MessageEntity
import android.example.tinkoffproject.network.NetworkClient
import android.example.tinkoffproject.network.NetworkClient.client
import android.example.tinkoffproject.network.NetworkClient.makeJSONArray
import android.example.tinkoffproject.utils.*
import androidx.annotation.MainThread
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MultipartBody
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class ChatViewModel(
    private val stream: String,
    private val topic: String,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val querySendMessage: PublishSubject<String> by lazy { makePublishSubject<String>() }
    private val queryUploadFile: PublishSubject<MultipartBody.Part> by lazy { makePublishSubject<MultipartBody.Part>() }
    private val queryGetMessages: PublishSubject<Unit> by lazy { makePublishSubject<Unit>() }
    private val queryAddReaction: PublishSubject<Pair<Int, String>> by lazy { makePublishSubject<Pair<Int, String>>() }
    private val queryRemoveReaction: PublishSubject<Pair<Int, String>> by lazy { makePublishSubject<Pair<Int, String>>() }

    private val disposables = mutableMapOf<String, Disposable>()
    private val compositeDisposable = CompositeDisposable()

    private val currentMessages: List<UserMessage>
        get() = _uiState.value?.topicMessages ?: emptyList()

    private val _errorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    val _messageSent = SingleLiveEvent<Boolean?>()
    val messageSent: LiveData<Boolean?>
        get() = _messageSent

    private val _uiState = MutableLiveData<ChatUiState>()
    val uiState: LiveData<ChatUiState>
        get() = _uiState

    data class ChatUiState(
        var topicMessages: List<UserMessage> = emptyList(),
        val isLoading: Boolean = false,
    )

    private val _reactionUpdateMessageId = MutableLiveData<Int>()
    val reactionUpdateMessageId: LiveData<Int>
        get() = _reactionUpdateMessageId

    var messagePlaceholderSent: Boolean? = false
    private var messageIdToUpdate = 0
    var posToUpdate = 0


    init {
        subscribeGetMessages()
        subscribeSendMessage()
        subscribeAddReaction()
        subscribeRemoveReaction()
        subscribeUploadFile()
        loadMessages()
        _messageSent.value = false
    }

    fun setMessageSent(value: Boolean?) {
        _messageSent.value = value
    }

    fun getMessages(): Flowable<PagingData<MessageEntity>> {
        return chatRepository
            .getMessages()
            .cachedIn(viewModelScope)
    }

    fun sendMessage(message: String) {
        querySendMessage.onNext(message)
    }

    fun uploadFile(file: MultipartBody.Part) {
        queryUploadFile.onNext(file)
    }


    private fun subscribeGetMessages() {
        disposables[KEY_GET_MESSAGE]?.dispose()
        disposables[KEY_GET_MESSAGE] = queryGetMessages
            .observeOn(Schedulers.io())
            .switchMapSingle {
                (if (messageIdToUpdate != 0)
                    client.getMessagesWithAnchor(
                        makeJSONArray(
                            listOf(
                                Pair("stream", stream),
                                Pair("topic", topic)
                            )
                        ), anchor = messageIdToUpdate
                    )
                else client.getMessages(
                    makeJSONArray(
                        listOf(
                            Pair("stream", stream),
                            Pair("topic", topic)
                        )
                    ), numBefore = 1
                ))
                    .map { messagesResponse ->
                        processMessagesFromNetwork(messagesResponse.messges)
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = { messages ->
                if (messageIdToUpdate != 0) {
                    posToUpdate = messageIdToUpdate
                    messageIdToUpdate = 0
                    _reactionUpdateMessageId.value = 0
                } else if (_messageSent.value == false) {
                    _messageSent.value = null
                }

                _uiState.value = _uiState.value?.copy(topicMessages = messages, isLoading = false)
            }, onError = {
                _errorMessage.value = "Ошибка при загрузке сообщений"
            })
    }

    private fun subscribeSendMessage() {
        disposables[KEY_SEND_MESSAGE]?.dispose()
        disposables[KEY_SEND_MESSAGE] =
            querySendMessage
                .doOnNext {
                    chatRepository.insertMessagesReplace(
                        listOf(
                            MessageEntity(
                                Random.nextInt(-20, -1),
                                stream,
                                topic,
                                NetworkClient.MY_USER_ID,
                                "Vadim",
                                messageText = it,
                                date = Date().time / 1000,
                                isSent = false
                            )
                        )
                    )
                    messagePlaceholderSent = null
                }
                .observeOn(Schedulers.io())
                .concatMapSingle { message ->
                    client.sendPublicMessage(message, stream, topic)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        _messageSent.value = false
                        queryGetMessages.onNext(Unit)
                    },
                    onError = {
                        _errorMessage.value = "Ошибка отправки сообщения"
                    }
                )
    }

    private fun subscribeUploadFile() {
        disposables[KEY_UPLOAD_FILE]?.dispose()
        disposables[KEY_UPLOAD_FILE] =
            queryUploadFile
                .observeOn(Schedulers.io())
                .concatMapSingle { file ->
                    client.uploadFile(file)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        querySendMessage.onNext("This is a link to a new file: ${it.uri}")
                    },
                    onError = {
                        _errorMessage.value = "Ошибка отправки сообщения"
                    }
                )
    }

    private fun subscribeAddReaction() {
        disposables[KEY_ADD_REACTION]?.dispose()
        disposables[KEY_ADD_REACTION] = queryAddReaction
            .observeOn(Schedulers.io())
            .concatMapSingle { (position, emoji_name) ->
                messageIdToUpdate = currentMessages[position].messageId
                client.addReaction(
                    currentMessages[position].messageId,
                    emoji_name,
                    EMOJI_MAP[emoji_name]?.toString(16)?.lowercase() ?: "-1"
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                queryGetMessages.onNext(Unit)
            }, onError = {
                _errorMessage.value = "Ошибка отправки реакции"
                subscribeAddReaction()
            })
    }

    private fun subscribeRemoveReaction() {
        disposables[KEY_REMOVE_REACTION]?.dispose()
        disposables[KEY_REMOVE_REACTION] = queryRemoveReaction
            .observeOn(Schedulers.io())
            .concatMapSingle { (position, emoji_name) ->
                messageIdToUpdate = currentMessages[position].messageId
                client.removeReaction(
                    currentMessages[position].messageId,
                    emoji_name,
                    EMOJI_MAP[emoji_name]?.toString(16)?.lowercase() ?: "-1"
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                queryGetMessages.onNext(Unit)
            }, onError = {
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
        queryAddReaction.onNext(Pair(position, emoji_name))
    }

    private fun loadMessages() {
        chatRepository.getAllMessagesFromDb(stream, topic)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                if (it.isEmpty())
                    _uiState.value = ChatUiState(isLoading = true)
                else
                    _uiState.value =
                        _uiState.value?.copy(topicMessages = it.map { messageDb ->
                            convertMessageFromDbToNetwork(
                                messageDb
                            )
                        }, isLoading = false) ?: ChatUiState(isLoading = false)
            })
            .addTo(compositeDisposable)
    }

    override fun onCleared() {
        for (key in disposables.keys)
            disposables[key]?.dispose()
        chatRepository.clearMessagesOnExit(stream, topic)
    }

    companion object {
        private const val KEY_GET_MESSAGE = "get message"
        private const val KEY_SEND_MESSAGE = "send message"
        private const val KEY_ADD_REACTION = "add reaction"
        private const val KEY_REMOVE_REACTION = "remove reaction"
        private const val KEY_UPLOAD_FILE = "upload file"
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