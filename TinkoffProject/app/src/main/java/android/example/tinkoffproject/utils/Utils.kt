package android.example.tinkoffproject.utils

import android.example.tinkoffproject.channels.data.db.ChannelEntity
import android.example.tinkoffproject.channels.data.network.ChannelItem
import android.example.tinkoffproject.chat.channel.data.db.ChannelMessageEntity
import android.example.tinkoffproject.chat.common.data.network.UserMessage
import android.example.tinkoffproject.chat.common.ui.UiModel
import android.example.tinkoffproject.chat.topic.data.db.TopicMessageEntity
import android.example.tinkoffproject.contacts.data.db.ContactEntity
import android.example.tinkoffproject.contacts.data.network.ContactItem
import android.example.tinkoffproject.network.NetworkCommon
import androidx.annotation.MainThread
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.PagingData
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import java.util.concurrent.atomic.AtomicBoolean


fun makeSearchObservable(
    querySearch: Observable<String>,
    resetSearch: () -> Unit
): Observable<String> = querySearch
    .map { query -> query.trim() }
    .scan { previous, current ->
        if (current.isBlank() && previous.isNotBlank())
            resetSearch()
        current
    }
    .filter { it.isNotBlank() }
    .distinctUntilChanged()

fun <T> makePublishSubject() = PublishSubject.create<T>()

fun displayErrorMessage(error: Throwable, defaultMessage: String? = null): String =
    if (error is HttpException)
        when (error.code()) {
            400 -> "Reaction already exists"
            429 -> "Too many requests"
            else -> defaultMessage ?: error.localizedMessage
        }
    else defaultMessage ?: error.localizedMessage

fun convertChannelFromNetworkToDb(
    channelItem: ChannelItem, isMy: Boolean = false
): ChannelEntity {
    return ChannelEntity(
        channelItem.streamID,
        channelItem.name,
        channelItem.isTopic,
        false,
        channelItem.parentChannel,
        isMy
    )
}


fun convertChannelFromDbToNetwork(channelEntity: ChannelEntity): ChannelItem {
    return ChannelItem(
        streamID = channelEntity.streamID,
        name = channelEntity.name,
        isTopic = channelEntity.isTopic,
        isExpanded = false,
        parentChannel = channelEntity.parentChannel
    )
}

fun convertMessageFromNetworkToTopicChatDb(
    userMessage: UserMessage
): TopicMessageEntity {
    return TopicMessageEntity(
        name = userMessage.name,
        topicName = userMessage.topic,
        channelName = userMessage.channel,
        selectedReactions = userMessage.selectedReactions,
        reactions = userMessage.reactions,
        isSent = userMessage.isSent,
        messageId = userMessage.messageId,
        date = userMessage.date,
        messageText = userMessage.messageText,
        avatarUrl = userMessage.avatarUrl,
        userId = userMessage.userId,
        fileLink = userMessage.fileLink,
        isMyMessage = userMessage.userId == NetworkCommon.MY_USER_ID
    )
}

fun convertMessageFromNetworkToChannelChatDb(
    userMessage: UserMessage
): ChannelMessageEntity {
    return ChannelMessageEntity(
        name = userMessage.name,
        topicName = userMessage.topic,
        channelName = userMessage.channel,
        selectedReactions = userMessage.selectedReactions,
        reactions = userMessage.reactions,
        isSent = userMessage.isSent,
        messageId = userMessage.messageId,
        date = userMessage.date,
        messageText = userMessage.messageText,
        avatarUrl = userMessage.avatarUrl,
        userId = userMessage.userId,
        fileLink = userMessage.fileLink,
        isMyMessage = userMessage.userId == NetworkCommon.MY_USER_ID
    )
}

fun convertChannelMessageFromDbToUi(channelMessageEntity: ChannelMessageEntity): UiModel.MessageItem {
    return UiModel.MessageItem(
        name = channelMessageEntity.name,
        topicName = channelMessageEntity.topicName,
        channelName = channelMessageEntity.channelName,
        selectedReactions = channelMessageEntity.selectedReactions,
        reactions = channelMessageEntity.reactions,
        isSent = channelMessageEntity.isSent,
        messageId = channelMessageEntity.messageId,
        date = channelMessageEntity.date,
        messageText = channelMessageEntity.messageText,
        avatarUrl = channelMessageEntity.avatarUrl,
        userId = channelMessageEntity.userId,
        fileLink = channelMessageEntity.fileLink,
        isMyMessage = channelMessageEntity.isMyMessage
    )
}

fun convertTopicMessageFromDbToUi(topicMessageEntity: TopicMessageEntity): UiModel {
    return UiModel.MessageItem(
        name = topicMessageEntity.name,
        topicName = topicMessageEntity.topicName,
        channelName = topicMessageEntity.channelName,
        selectedReactions = topicMessageEntity.selectedReactions,
        reactions = topicMessageEntity.reactions,
        isSent = topicMessageEntity.isSent,
        messageId = topicMessageEntity.messageId,
        date = topicMessageEntity.date,
        messageText = topicMessageEntity.messageText,
        avatarUrl = topicMessageEntity.avatarUrl,
        userId = topicMessageEntity.userId,
        fileLink = topicMessageEntity.fileLink,
        isMyMessage = topicMessageEntity.isMyMessage
    )
}

fun convertContactFromNetworkToDb(
    contactItem: ContactItem
): ContactEntity {
    return ContactEntity(
        contactItem.userId,
        contactItem.name,
        contactItem.email,
        contactItem.status,
        contactItem.avatarUrl
    )
}

fun convertContactFromDbToNetwork(
    contactEntity: ContactEntity
): ContactItem {
    return ContactItem(
        contactEntity.userId,
        contactEntity.name,
        contactEntity.email,
        contactEntity.status,
        false,
        contactEntity.avatarUrl
    )
}

fun processMessagesFromNetwork(messagesResponse: List<UserMessage>): List<UserMessage> {
    val messagesProcessed = messagesResponse.map {
        if (!it.messageText.contains("<a href="))
            it.copy(
                messageText =
                HtmlCompat.fromHtml(it.messageText, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
                    .trim()
            )
        else {
            it.copy(
                messageText =
                HtmlCompat.fromHtml(it.messageText, HtmlCompat.FROM_HTML_MODE_COMPACT)
                    .toString()
                    .trim(),
                fileLink = "https://tinkoff-android-spring-2022.zulipchat.com" + it.messageText.substringAfter(
                    "href=\""
                ).substringBefore(
                    "\">"
                )
            )
        }
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
                        .find { it.userId == NetworkCommon.MY_USER_ID } != null
            }
        }
    }
    return messagesProcessed
}

class MessagesFromDbInitLoad(val isNotEmpty: Boolean, val pagingData: PagingData<UiModel>)

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
