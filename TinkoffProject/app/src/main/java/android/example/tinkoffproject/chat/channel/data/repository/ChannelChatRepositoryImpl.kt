package android.example.tinkoffproject.chat.channel.data.repository

import android.example.tinkoffproject.channels.data.network.ChannelItem
import android.example.tinkoffproject.chat.channel.data.ChannelMessagesRemoteMediator
import android.example.tinkoffproject.chat.channel.data.db.ChannelMessageEntity
import android.example.tinkoffproject.chat.channel.data.db.ChannelMessagesDAO
import android.example.tinkoffproject.chat.channel.di.ChannelChat
import android.example.tinkoffproject.network.ApiService
import android.example.tinkoffproject.network.NetworkCommon
import android.example.tinkoffproject.utils.EMOJI_MAP
import android.example.tinkoffproject.utils.convertMessageFromNetworkToChannelChatDb
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

@ChannelChat
class ChannelChatRepositoryImpl @Inject constructor(
    private val channelMessagesDAO: ChannelMessagesDAO,
    private val remoteMediator: ChannelMessagesRemoteMediator,
    override val channel: String,
    override val channelId: Int,
    private val client: ApiService
) : ChannelChatRepository {

    override val disposables: MutableMap<String, Disposable> = mutableMapOf()

    override fun getMessages(): Flowable<PagingData<ChannelMessageEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = ChannelMessagesRemoteMediator.PAGE_SIZE,
                enablePlaceholders = true,
                maxSize = ChannelMessagesRemoteMediator.MAX_MESSAGES_TO_CACHE,
                prefetchDistance = ChannelMessagesRemoteMediator.PREFETCH_SIZE,
                initialLoadSize = ChannelMessagesRemoteMediator.INITIAL_LOAD_SIZE
            ),
            remoteMediator = remoteMediator,
            pagingSourceFactory = {
                channelMessagesDAO
                    .messagesFromChannelPagingSource(channel)
            }
        ).flowable
            .share()
    }

    override fun getAllMessagesFromDb(channel: String): Single<List<ChannelMessageEntity>> =
        channelMessagesDAO.getAllMessages(channel)
            .subscribeOn(Schedulers.io())

    override fun loadFromNetwork(): Single<Unit> =
        client.getMessages(
            NetworkCommon.makeJSONArray(
                listOf(
                    Pair("stream", channel),
                )
            ), numBefore = 50
        )
            .map {
                disposables[KEY_CLEAR_DB_ON_START]?.dispose()
                disposables[KEY_CLEAR_DB_ON_START] =
                    channelMessagesDAO.clearRemovedMessages(it.messages.map { message ->
                        message.messageId
                    }, channel)
                        .subscribe()
            }

    override fun getTopicsForChannel(channelId: Int): Single<List<ChannelItem>> =
        client.getTopicsForStream(channelId)
            .map { topicsResponse ->
                topicsResponse.channelsList
            }

    override fun clearMessagesOnExit(channel: String): Completable =
        channelMessagesDAO.clearCachedMessages(channel)
            .subscribeOn(Schedulers.io())

    override fun sendMessage(messageText: String, topic: String): Single<Unit> =
        client.sendPublicMessage(messageText, channel, topic)
            .flatMap {
                client.getMessages(
                    NetworkCommon.makeJSONArray(
                        listOf(
                            Pair("stream", channel),
                        )
                    ), numBefore = 1
                )
            }
            .map {
                channelMessagesDAO.clearAndInsertInTransaction(processMessagesFromNetwork(it.messages).map { message ->
                    convertMessageFromNetworkToChannelChatDb(message)
                }, channel)
            }


    override fun editMessage(messageText: String, messageId: Int): Single<Unit> =
        client.editMessageText(messageId, messageText)
            .flatMap {
                client.getSingleMessage(messageId)
            }
            .map {
                disposables[KEY_UPDATE_MSG_TEXT]?.dispose()
                disposables[KEY_UPDATE_MSG_TEXT] =
                    channelMessagesDAO.updateMessage(messageText, messageId)
                        .subscribeOn(Schedulers.io())
                        .subscribe()
                disposables[KEY_UPDATE_MSG_TEXT_EXT]?.dispose()
                disposables[KEY_UPDATE_MSG_TEXT_EXT] =
                    channelMessagesDAO.updateMessageExternal(messageText, messageId)
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
                disposables[KEY_UPDATE_MSG] = channelMessagesDAO.updateTopic(topic, messageId)
                    .subscribeOn(Schedulers.io())
                    .subscribe()

                disposables[KEY_UPDATE_MSG_EXT]?.dispose()
                disposables[KEY_UPDATE_MSG_EXT] =
                    channelMessagesDAO.updateTopicExternal(topic, messageId)
                        .subscribeOn(Schedulers.io())
                        .subscribe()
            }

    override fun deleteMessage(messageId: Int): Single<Unit> =
        client.deleteMessage(messageId)
            .map {
                disposables[KEY_DELETE_MSG]?.dispose()
                disposables[KEY_DELETE_MSG] = channelMessagesDAO.deleteMessage(messageId)
                    .subscribeOn(Schedulers.io())
                    .subscribe()
                disposables[KEY_DELETE_MSG_EXT]?.dispose()
                disposables[KEY_DELETE_MSG_EXT] =
                    channelMessagesDAO.deleteMessageExternal(messageId)
                        .subscribeOn(Schedulers.io())
                        .subscribe()
            }

    override fun sendMessagePlaceholder(messageText: String, topic: String): Completable =
        channelMessagesDAO.insertMessages(
            listOf(
                ChannelMessageEntity(
                    Random.nextInt(-100, -1),
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

    override fun uploadFile(
        fileName: String,
        file: MultipartBody.Part,
        topic: String
    ): Single<Unit> =
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
                        )
                    ), numBefore = 1
                )
            }
            .map {
                channelMessagesDAO.clearAndInsertInTransaction(processMessagesFromNetwork(it.messages).map { message ->
                    convertMessageFromNetworkToChannelChatDb(message)
                }, channel)
            }

    override fun reactionClicked(emoji_name: String, messageId: Int): Single<Unit> =
        channelMessagesDAO.getMessage(messageId)
            .subscribeOn(Schedulers.io())
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
                channelMessagesDAO.insertMessages(processMessagesFromNetwork(listOf(it.userMessage)).map { message ->
                    convertMessageFromNetworkToChannelChatDb(message)
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
                channelMessagesDAO.insertMessages(processMessagesFromNetwork(listOf(it.userMessage)).map { message ->
                    convertMessageFromNetworkToChannelChatDb(message)
                })
                    .subscribe()
            }

    companion object {
        private const val KEY_DELETE_MSG = "delete msg"
        private const val KEY_DELETE_MSG_EXT = "delete msg ext"
        private const val KEY_UPDATE_MSG = "update msg"
        private const val KEY_UPDATE_MSG_EXT = "update msg ext"
        private const val KEY_UPDATE_MSG_TEXT = "update msg text"
        private const val KEY_UPDATE_MSG_TEXT_EXT = "update msg text ext"
        private const val KEY_CLEAR_DB_ON_START = "clear on start"
    }
}