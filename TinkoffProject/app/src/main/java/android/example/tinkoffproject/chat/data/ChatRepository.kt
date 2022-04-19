package android.example.tinkoffproject.chat.data

import android.example.tinkoffproject.chat.data.db.MessageEntity
import android.example.tinkoffproject.chat.data.db.MessagesDAO
import android.example.tinkoffproject.chat.data.db.MessagesRemoteMediator
import android.example.tinkoffproject.chat.data.network.UserMessage
import android.example.tinkoffproject.network.NetworkClient
import android.example.tinkoffproject.network.NetworkClient.client
import android.example.tinkoffproject.utils.EMOJI_MAP
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import java.util.*
import kotlin.random.Random

class ChatRepository(
    private val messagesDAO: MessagesDAO,
    private val remoteMediator: MessagesRemoteMediator,
    val channel: String,
    val topic: String
) {
    fun getMessages(): Flowable<PagingData<MessageEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = MessagesRemoteMediator.PAGE_SIZE,
                enablePlaceholders = true,
                maxSize = MessagesRemoteMediator.MAX_MESSAGES_TO_CACHE,
                prefetchDistance = MessagesRemoteMediator.PREFETCH_SIZE,
                initialLoadSize = MessagesRemoteMediator.INITIAL_LOAD_SIZE
            ),
            remoteMediator = remoteMediator,
            pagingSourceFactory = {
                messagesDAO
                    .messagesPagingSource(remoteMediator.stream, remoteMediator.topic)
            }
        ).flowable
    }

    fun getAllMessagesFromDb(channel: String, topic: String) =
        messagesDAO.getAllMessages(channel, topic)
            .subscribeOn(Schedulers.io())

    fun clearMessagesOnExit(channel: String, topic: String): Disposable =
        messagesDAO.clearCachedMessages(channel, topic)
            .subscribeOn(Schedulers.io())
            .subscribe()

    fun sendMessage(messageText: String): Flowable<String> =
        client.sendPublicMessage(messageText, channel, topic)
            .concatWith {
                client.getMessages(
                    NetworkClient.makeJSONArray(
                        listOf(
                            Pair("stream", channel),
                            Pair("topic", topic)
                        )
                    ), numBefore = 1
                )
            }

    fun sendMessagePlaceholder(messageText: String) = messagesDAO.insertMessages(
        listOf(
            MessageEntity(
                Random.nextInt(-20, -1),
                channel,
                topic,
                NetworkClient.MY_USER_ID,
                "Vadim",
                messageText = messageText,
                date = Date().time / 1000,
                isSent = false
            )
        )
    )
        .subscribeOn(Schedulers.io())

    fun uploadFile(fileName: String, file: MultipartBody.Part): Flowable<String> =
        client.uploadFile(file)
            .flatMap { response ->
                client.sendPublicMessage(
                    "[${fileName}](${response.uri})",
                    channel,
                    topic
                )
            }
            .concatWith {
                client.getMessages(
                    NetworkClient.makeJSONArray(
                        listOf(
                            Pair("stream", channel),
                            Pair("topic", topic)
                        )
                    ), numBefore = 1
                )
            }

    fun reactionClicked(
        emoji_name: String,
        messageId: Int
    ) = messagesDAO.getMessage(messageId)
        .flatMap {
            if (it.selectedReactions[emoji_name] == true) {
                client.removeReaction(
                    messageId,
                    emoji_name,
                    EMOJI_MAP[emoji_name]?.toString(16)?.lowercase() ?: "-1"
                )
            } else {
                client.addReaction(
                    messageId,
                    emoji_name,
                    EMOJI_MAP[emoji_name]?.toString(16)?.lowercase() ?: "-1"
                )
            }
        }
        .concatWith {
            client.getMessagesWithAnchor(
                NetworkClient.makeJSONArray(
                    listOf(
                        Pair("stream", channel),
                        Pair("topic", topic)
                    )
                ), anchor = messageId
            )
        }
        .subscribeOn(Schedulers.io())


    fun addReaction(emoji_name: String, messageId: Int): Flowable<String> =
        client.addReaction(
            messageId,
            emoji_name,
            EMOJI_MAP[emoji_name]?.toString(16)?.lowercase() ?: "-1"
        )
            .concatWith {
                client.getMessagesWithAnchor(
                    NetworkClient.makeJSONArray(
                        listOf(
                            Pair("stream", channel),
                            Pair("topic", topic)
                        )
                    ), anchor = messageId
                )
            }

    companion object {
        private const val KEY_GET_MESSAGE = "get message"
        private const val KEY_SEND_MESSAGE = "send message"
        private const val KEY_ADD_REACTION = "add reaction"
        private const val KEY_REMOVE_REACTION = "remove reaction"
        private const val KEY_UPLOAD_FILE = "upload file"
    }
}