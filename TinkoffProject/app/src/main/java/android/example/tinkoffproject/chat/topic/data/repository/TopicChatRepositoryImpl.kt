package android.example.tinkoffproject.chat.topic.data.repository

import android.example.tinkoffproject.channels.data.network.ChannelItem
import android.example.tinkoffproject.chat.topic.data.TopicMessagesRemoteMediator
import android.example.tinkoffproject.chat.topic.data.db.TopicMessageEntity
import android.example.tinkoffproject.chat.topic.data.db.TopicMessagesDAO
import android.example.tinkoffproject.chat.topic.di.Chat
import android.example.tinkoffproject.network.ApiService
import android.example.tinkoffproject.network.NetworkCommon
import android.example.tinkoffproject.utils.EMOJI_MAP
import android.example.tinkoffproject.utils.convertMessageFromNetworkToTopicChatDb
import android.example.tinkoffproject.utils.processMessagesFromNetwork
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@Chat
class TopicChatRepositoryImpl @Inject constructor(
    private val topicMessagesDAO: TopicMessagesDAO,
    private val remoteMediator: TopicMessagesRemoteMediator,
    override val channel: String,
    override val topic: String,
    private val client: ApiService
) : TopicChatRepository {

    override val disposables: MutableMap<String, Disposable> = mutableMapOf()

    override fun getMessages(): Flowable<PagingData<TopicMessageEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = TopicMessagesRemoteMediator.PAGE_SIZE,
                enablePlaceholders = true,
                maxSize = TopicMessagesRemoteMediator.MAX_MESSAGES_TO_CACHE,
                prefetchDistance = TopicMessagesRemoteMediator.PREFETCH_SIZE,
                initialLoadSize = TopicMessagesRemoteMediator.INITIAL_LOAD_SIZE
            ),
            remoteMediator = remoteMediator,
            pagingSourceFactory = {
                topicMessagesDAO
                    .messagesPagingSource(channel, topic)
            }
        ).flowable
            .share()
    }

    override fun loadFromNetwork(): Single<Unit> =
        client.getMessages(
            NetworkCommon.makeJSONArray(
                listOf(
                    Pair("stream", channel),
                    Pair("topic", topic)
                )
            ), numBefore = 50
        )
            .map {
                disposables[KEY_CLEAR_DB_ON_START]?.dispose()
                disposables[KEY_CLEAR_DB_ON_START] =
                    topicMessagesDAO.clearRemovedMessages(
                        it.messages.map { it.messageId },
                        channel,
                        topic
                    )
                        .subscribe()
            }

    override fun getTopicsForChannel(channel: String): Single<List<ChannelItem>> =
        client.getStreamId(channel)
            .flatMap {
                client.getTopicsForStream(it.channelId)
            }
            .map { topicsResponse ->
                topicsResponse.channelsList
            }

    override fun getAllMessagesFromDb(channel: String, topic: String) =
        topicMessagesDAO.getAllMessages(channel, topic)
            .subscribeOn(Schedulers.io())

    override fun clearMessagesOnExit(channel: String, topic: String): Completable =
        topicMessagesDAO.clearCachedMessages(channel, topic)
            .subscribeOn(Schedulers.io())

    override fun sendMessage(messageText: String): Single<Unit> =
        client.sendPublicMessage(messageText, channel, topic)
            .flatMap {
                client.getMessages(
                    NetworkCommon.makeJSONArray(
                        listOf(
                            Pair("stream", channel),
                            Pair("topic", topic)
                        )
                    ), numBefore = 1
                )
            }
            .map {
                topicMessagesDAO.clearAndInsertInTransaction(processMessagesFromNetwork(it.messages).map { message ->
                    convertMessageFromNetworkToTopicChatDb(message)
                }, channel, topic)
            }

    override fun editMessage(messageText: String, messageId: Int): Single<Unit> =
        client.editMessageText(messageId, messageText)
            .flatMap {
                client.getSingleMessage(messageId)
            }
            .map {
                topicMessagesDAO.updateMessage(messageText, messageId)
                    .subscribeOn(Schedulers.io())
                    .subscribe()
                topicMessagesDAO.updateMessageExternal(messageText, messageId)
                    .subscribeOn(Schedulers.io())
                    .subscribe()
            }

    override fun changeTopic(topic: String, messageId: Int): Single<Unit> =
        client.changeMessageTopic(topic = topic, msgID = messageId)
            .flatMap {
                client.getSingleMessage(messageId)
            }
            .map {
                disposables[KEY_UPDATE_MSG]?.dispose()
                disposables[KEY_UPDATE_MSG] = topicMessagesDAO.updateTopic(topic, messageId)
                    .subscribeOn(Schedulers.io())
                    .subscribe()

                disposables[KEY_UPDATE_MSG_EXT]?.dispose()
                disposables[KEY_UPDATE_MSG_EXT] =
                    topicMessagesDAO.updateTopicExternal(topic, messageId)
                        .subscribeOn(Schedulers.io())
                        .subscribe()
            }

    override fun deleteMessage(messageId: Int): Single<Unit> =
        client.deleteMessage(messageId)
            .map {
                disposables[KEY_DELETE_MSG]?.dispose()
                disposables[KEY_DELETE_MSG] = topicMessagesDAO.deleteMessage(messageId)
                    .subscribeOn(Schedulers.io())
                    .subscribe()

                disposables[KEY_DELETE_MSG_EXT]?.dispose()
                disposables[KEY_DELETE_MSG_EXT] = topicMessagesDAO.deleteMessageExternal(messageId)
                    .subscribeOn(Schedulers.io())
                    .subscribe()
            }

    override fun sendMessagePlaceholder(messageText: String) = topicMessagesDAO.insertMessages(
        listOf(
            TopicMessageEntity(
                Random.nextInt(-20, -1),
                channel,
                topic,
                NetworkCommon.MY_USER_ID,
                "Vadim",
                messageText = messageText,
                date = Date().time / 1000,
                isSent = false,
                isMyMessage = true
            )
        )
    )
        .subscribeOn(Schedulers.io())

    override fun uploadFile(fileName: String, file: MultipartBody.Part): Single<Unit> =
        client.uploadFile(file)
            .flatMap { response ->
                client.sendPublicMessage(
                    "[${fileName}](${response.uri})",
                    channel,
                    topic
                )
            }
            .flatMap {
                client.getMessages(
                    NetworkCommon.makeJSONArray(
                        listOf(
                            Pair("stream", channel),
                            Pair("topic", topic)
                        )
                    ), numBefore = 1
                )
            }
            .map {
                topicMessagesDAO.clearAndInsertInTransaction(processMessagesFromNetwork(it.messages).map { message ->
                    convertMessageFromNetworkToTopicChatDb(message)
                }, channel, topic)
            }

    override fun reactionClicked(
        emoji_name: String,
        messageId: Int
    ): Single<Unit> = topicMessagesDAO.getMessage(messageId)
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
        .flatMap {
            client.getSingleMessage(messageId)
        }
        .map {
            topicMessagesDAO.insertMessages(processMessagesFromNetwork(listOf(it.userMessage)).map { message ->
                convertMessageFromNetworkToTopicChatDb(message)
            })
                .subscribe()
        }


    override fun addReaction(emoji_name: String, messageId: Int): Single<Unit> =
        client.addReaction(
            messageId,
            emoji_name,
            EMOJI_MAP[emoji_name]?.toString(16)?.lowercase() ?: "-1"
        )
            .flatMap {
                client.getSingleMessage(messageId)
            }
            .map {
                topicMessagesDAO.insertMessages(processMessagesFromNetwork(listOf(it.userMessage)).map { message ->
                    convertMessageFromNetworkToTopicChatDb(message)
                })
                    .subscribe()
            }

    companion object {
        private const val KEY_DELETE_MSG = "delete msg"
        private const val KEY_DELETE_MSG_EXT = "delete msg ext"
        private const val KEY_UPDATE_MSG = "update msg"
        private const val KEY_UPDATE_MSG_EXT = "update msg ext"
        private const val KEY_CLEAR_DB_ON_START = "clear db on start"
    }
}