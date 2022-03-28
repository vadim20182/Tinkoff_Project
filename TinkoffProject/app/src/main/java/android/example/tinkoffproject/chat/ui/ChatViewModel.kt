package android.example.tinkoffproject.chat.ui

import android.accounts.NetworkErrorException
import android.example.tinkoffproject.R
import android.example.tinkoffproject.message.customviews.ReactionCustomView
import android.example.tinkoffproject.message.model.UserMessage
import android.example.tinkoffproject.message.model.UserReaction
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class ChatViewModel : ViewModel() {

    private val _topicMessages: MutableLiveData<List<UserMessage>> =
        MutableLiveData<List<UserMessage>>()
    val topicMessages: LiveData<out List<UserMessage>> = _topicMessages

    private val compositeDisposable = CompositeDisposable()
    private val disposables = mutableMapOf<String, Disposable>()
    private var querySend: PublishSubject<UserMessage> = PublishSubject.create()


    private val currentMessages: List<UserMessage>
        get() = _topicMessages.value ?: emptyList()

    private val _isAsyncTaskCompleted: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>()
    val isAsyncTaskCompleted: LiveData<Boolean> = _isAsyncTaskCompleted

    private val _itemToUpdate: MutableLiveData<Int> =
        MutableLiveData<Int>()
    val itemToUpdate: LiveData<Int> = _itemToUpdate

    private val _messagesCount: MutableLiveData<Int> =
        MutableLiveData<Int>()
    val messagesCount: LiveData<Int> = _messagesCount

    private val _errorMessage: MutableLiveData<String> =
        MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        loadUsers()
        subscribeSendMessage()
    }

    fun resetErrorMessage() {
        _errorMessage.value = ""
    }

    private fun appendMessage(message: UserMessage): List<UserMessage> {
        val newList = mutableListOf<UserMessage>().apply { addAll(currentMessages) }
        newList.add(message)
        return newList
    }

    fun sendMessage(userMessage: UserMessage) {
        querySend.onNext(userMessage)
    }

    private fun subscribeSendMessage() {
        disposables[KEY_SEND_MESSAGE]?.dispose()
        disposables[KEY_SEND_MESSAGE] = querySend
            .subscribeOn(Schedulers.io())
            .doOnNext {
                if (Random.nextInt(4) == 2)
                    throw NetworkErrorException("Ошибка отправки")
            }
            .concatMapSingle { message ->
                Single.fromCallable { appendMessage(message) }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    _topicMessages.value = it
                    _messagesCount.value = currentMessages.size
                    _isAsyncTaskCompleted.value = true
                },
                onError = {
                    _errorMessage.value = it.message
                    subscribeSendMessage()
                }
            )

    }

    fun reactionClicked(position: Int, emoji: String) {
        val newList = mutableListOf<UserMessage>()

        disposables[KEY_CLICK_REACTION]?.dispose()
        disposables[KEY_CLICK_REACTION] = Completable.fromCallable { }
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                newList.addAll(currentMessages)
                val userReaction = UserReaction(
                    newList[position].reactions,
                    newList[position].selectedReactions
                )
                if (userReaction.isReactionSelected(emoji) == true) {
                    if (userReaction.canReactionCountBeDecreased(emoji)) {
                        userReaction.unselectReaction(emoji)
                    } else
                        userReaction.deleteReaction(emoji)
                } else {
                    userReaction.increaseReactionCount(emoji)
                    userReaction.selectReaction(emoji)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    _topicMessages.value = newList
                    _itemToUpdate.value = position
                }
            )
    }

    fun addReaction(position: Int, emoji: String) {
        val newList = mutableListOf<UserMessage>()

        disposables[KEY_ADD_REACTION]?.dispose()
        disposables[KEY_ADD_REACTION] = Completable.fromCallable { }
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                newList.addAll(currentMessages)
                val userReaction = UserReaction(
                    newList[position].reactions,
                    newList[position].selectedReactions
                )
                if (userReaction.isReactionSelected(emoji) != true) {
                    userReaction.selectReaction(emoji)
                    if (userReaction.isReactionAdded(emoji)) {
                        userReaction.increaseReactionCount(emoji)
                    } else
                        userReaction.addReaction(emoji)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    _topicMessages.value = newList
                    _itemToUpdate.value = position
                }
            )
    }

    override fun onCleared() {
        for (key in disposables.keys)
            disposables[key]?.dispose()
        compositeDisposable.clear()
    }

    private fun loadUsers() {
        _isAsyncTaskCompleted.value = false
        val list = mutableListOf<UserMessage>()

        Completable.fromCallable { }
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                list.addAll(listOf(UserMessage(0, "Username1", messageText = "Hi1! There!"),
                    UserMessage(0, "Username2", messageText = "Hi2! There!"),
                    UserMessage(2, "Username3", messageText = "Hi3! There!"),
                    UserMessage(
                        3,
                        "Username4",
                        R.drawable.send_btn,
                        "Hi4! There!Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur eu quam sed diam lobortis lacinia sit amet in tellus. Sed hendrerit nisl in tellus maximus, non sollicitudin tortor scelerisque. Vivamus vel arcu in dui iaculis accumsan vitae sed erat. Mauris viverra arcu ex, id rhoncus libero pulvinar at. Morbi sem libero, tempor nec erat vitae, tincidunt vulputate dolor.",
                        mutableMapOf(Pair(ReactionCustomView.EMOJI_LIST[0], 1))
                    ),
                    UserMessage(
                        4, "Username5", messageText = "Hi5! There!", reactions = mutableMapOf(
                            Pair(ReactionCustomView.EMOJI_LIST[0], 1),
                            Pair(ReactionCustomView.EMOJI_LIST[1], 1),
                            Pair(ReactionCustomView.EMOJI_LIST[2], 1),
                            Pair(ReactionCustomView.EMOJI_LIST[3], 1)
                        )
                    ),
                    UserMessage(5, "Username6", messageText = "Hi6! There!"),
                    UserMessage(6, "Username7", messageText = "Hi7! There!"),
                    UserMessage(
                        7,
                        "Username8",
                        messageText = "Hi8! There!",
                        reactions = mutableMapOf<String, Int>().apply {
                            put(ReactionCustomView.EMOJI_LIST[0], 1)
                            put(ReactionCustomView.EMOJI_LIST[1], 5)
                        }
                    ),
                    UserMessage(8, "Username9", messageText = "Hi9! There!", date = "1 Feb"),
                    UserMessage(8, "Username10", messageText = "Hi10! There!", date = "1 Feb"),
                    UserMessage(8, "Username11", messageText = "Hi11! There!", date = "2 Feb")))
            }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    _topicMessages.value = list
                    _isAsyncTaskCompleted.value = true
                }
            )
            .addTo(compositeDisposable)
    }

    companion object {
        const val KEY_SEND_MESSAGE = "send_message"
        const val KEY_ADD_REACTION = "add_reaction"
        const val KEY_CLICK_REACTION = "reaction_clicked"
    }
}