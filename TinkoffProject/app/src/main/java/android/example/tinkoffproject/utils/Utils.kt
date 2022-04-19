package android.example.tinkoffproject.utils

import android.example.tinkoffproject.channels.data.db.ChannelEntity
import android.example.tinkoffproject.channels.data.network.ChannelItem
import android.example.tinkoffproject.chat.data.db.MessageEntity
import android.example.tinkoffproject.chat.data.network.UserMessage
import android.example.tinkoffproject.contacts.data.db.ContactEntity
import android.example.tinkoffproject.contacts.data.network.ContactItem
import android.example.tinkoffproject.network.NetworkClient
import androidx.core.text.HtmlCompat
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


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

fun convertMessageFromNetworkToDb(
    userMessage: UserMessage,
    topic: String,
    channel: String
): MessageEntity {
    return MessageEntity(
        name = userMessage.name,
        topicName = topic,
        channelName = channel,
        selectedReactions = userMessage.selectedReactions,
        reactions = userMessage.reactions,
        isSent = userMessage.isSent,
        messageId = userMessage.messageId,
        date = userMessage.date,
        messageText = userMessage.messageText,
        avatarUrl = userMessage.avatarUrl,
        userId = userMessage.userId,
        fileLink = userMessage.fileLink
    )
}

fun convertMessageFromDbToNetwork(
    messageEntity: MessageEntity
): UserMessage {
    return UserMessage(
        name = messageEntity.name,
        selectedReactions = messageEntity.selectedReactions,
        reactions = messageEntity.reactions,
        isSent = messageEntity.isSent,
        messageId = messageEntity.messageId,
        date = messageEntity.date,
        messageText = messageEntity.messageText,
        avatarUrl = messageEntity.avatarUrl,
        userId = messageEntity.userId,
        fileLink = messageEntity.fileLink
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
                        .find { it.userId == NetworkClient.MY_USER_ID } != null
            }
        }
    }
    return messagesProcessed
}