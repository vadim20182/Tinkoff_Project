package android.example.tinkoffproject.chat.model.db

import android.example.tinkoffproject.database.AppDatabase
import android.example.tinkoffproject.network.NetworkClient
import android.text.Html
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.rxjava2.RxRemoteMediator
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception


@ExperimentalPagingApi
class MessagesRemoteMediator(
    private val database: AppDatabase,
    val stream: String,
    val topic: String
) : RxRemoteMediator<Int, MessageEntity>() {

    override fun loadSingle(
        loadType: LoadType,
        state: PagingState<Int, MessageEntity>
    ): Single<MediatorResult> {
        var remoteKeySingle: Single<Int>? = null;
        when (loadType) {
            LoadType.REFRESH -> {
                remoteKeySingle = Single.just(NEWEST_MESSAGE)
            }
            LoadType.PREPEND -> {
                return Single.just(MediatorResult.Success(true))
            }
            LoadType.APPEND -> {
                val key =
                    database.messageRemoteKeysDAO().getRemoteKeys(stream, topic)
                        .lastOrNull()

                if (key?.prevKey != null)
                    remoteKeySingle = Single.just(key.prevKey)
                else if (database.messageDAO().getAllMessages().isNotEmpty())
                    return Single.just(MediatorResult.Success(true))
                else
                    return Single.just(MediatorResult.Success(false))
            }

        }
        return remoteKeySingle
            .subscribeOn(Schedulers.io())
            .flatMap<MediatorResult> { remoteKey ->
                return@flatMap (if (remoteKey !=
                    NEWEST_MESSAGE
                ) NetworkClient.client.getMessagesWithAnchor(
                    NetworkClient.makeJSONArray(
                        listOf(
                            Pair("stream", stream),
                            Pair("topic", topic)
                        )
                    ), numBefore = PAGE_SIZE, anchor = remoteKey
                ) else NetworkClient.client.getMessages(
                    NetworkClient.makeJSONArray(
                        listOf(
                            Pair("stream", stream),
                            Pair("topic", topic)
                        )
                    ), numBefore = PAGE_SIZE + 1
                ))
                    .map { messagesResponse ->
                        database.runInTransaction {
                            if (loadType == LoadType.REFRESH) {
                                database.messageDAO().clearMessages()
                                database.messageRemoteKeysDAO().clearRemoteKeys()
                            }

                            val res = messagesResponse.messges
                            if (res.size == PAGE_SIZE + 1 || remoteKey == NEWEST_MESSAGE)
                                database.messageRemoteKeysDAO().insertAll(
                                    listOf(
                                        MessageRemoteKeysEntity(
                                            res.last().messageId,
                                            res.first().messageId,
                                            null,
                                            stream,
                                            topic
                                        )
                                    )
                                )
                            else
                                database.messageRemoteKeysDAO().insertAll(
                                    listOf(
                                        MessageRemoteKeysEntity(
                                            res.last().messageId,
                                            null,
                                            null,
                                            stream,
                                            topic
                                        )
                                    )
                                )
                            val messagesProcessed = messagesResponse.messges.map {
                                it.copy(
                                    messageText =
                                    Html.fromHtml(it.messageText, Html.FROM_HTML_MODE_COMPACT)
                                        .toString()
                                        .trim()
                                )
                            }
                            for (msg in messagesProcessed) {
                                for (reaction in msg.allReactions) {
                                    if (!msg.reactions.containsKey(reaction.emoji_name))
                                        msg.reactions[reaction.emoji_name] =
                                            msg.allReactions.count {
                                                it.emoji_name == reaction.emoji_name
                                            }
                                    if (!msg.selectedReactions.containsKey(reaction.emoji_name)) {
                                        msg.selectedReactions[reaction.emoji_name] =
                                            msg.allReactions.filter { it.emoji_name == reaction.emoji_name }
                                                .find { it.userId == NetworkClient.MY_USER_ID } != null
                                    }
                                }
                            }

                            val messagesForDB =
                                (if (messagesProcessed.size == PAGE_SIZE + 1) messagesProcessed.subList(
                                    1, messagesProcessed.lastIndex + 1
                                ) else
                                    messagesProcessed).map {
                                    MessageEntity(
                                        userId = it.userId,
                                        name = it.name,
                                        avatarUrl = it.avatarUrl,
                                        messageText = it.messageText,
                                        date = it.date,
                                        messageId = it.messageId,
                                        isSent = it.isSent,
                                        reactions = it.reactions,
                                        selectedReactions = it.selectedReactions,
                                        channelName = stream,
                                        topicName = topic
                                    )
                                }
                            database.messageDAO().insertMessages(messagesForDB)
                        }
                        MediatorResult.Success(endOfPaginationReached = messagesResponse.messges.size < PAGE_SIZE)
                    }
            }
            .onErrorResumeNext {
                return@onErrorResumeNext Single.just(MediatorResult.Error(it))
            }
    }

    override fun initializeSingle(): Single<InitializeAction> {
        return Single.just(InitializeAction.LAUNCH_INITIAL_REFRESH)
    }

    companion object {
        const val PAGE_SIZE = 20
        const val PREFETCH_SIZE = 5
        const val INITIAL_LOAD_SIZE = 50
        const val NEWEST_MESSAGE = 1000000000
    }
}