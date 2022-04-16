package android.example.tinkoffproject.chat.model.db

import android.example.tinkoffproject.database.AppDatabase
import android.example.tinkoffproject.network.NetworkClient
import android.example.tinkoffproject.utils.convertMessageFromNetworkToDb
import android.example.tinkoffproject.utils.processMessagesFromNetwork
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxRemoteMediator
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

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
                remoteKeySingle = if (MESSAGE_ANCHOR_TO_UPDATE == 0)
                    Single.just(NEWEST_MESSAGE)
                else {
                    Single.just(MESSAGE_ANCHOR_TO_UPDATE)
                }
            }
            LoadType.PREPEND -> {
                return Single.just(MediatorResult.Success(true))
            }
            LoadType.APPEND -> {
                val nextAnchorMessageId = state.lastItemOrNull()?.messageId

                if (nextAnchorMessageId != null)
                    remoteKeySingle = Single.just(nextAnchorMessageId)
                else {
                    return if (!state.isEmpty())
                        Single.just(MediatorResult.Success(true))
                    else
                        Single.just(MediatorResult.Success(false))
                }
            }
        }
        return remoteKeySingle
            .subscribeOn(Schedulers.io())
            .flatMap<MediatorResult> { nextAnchorMessageId ->
                return@flatMap (if (nextAnchorMessageId !=
                    NEWEST_MESSAGE
                ) NetworkClient.client.getMessagesWithAnchor(
                    NetworkClient.makeJSONArray(
                        listOf(
                            Pair("stream", stream),
                            Pair("topic", topic)
                        )
                    ), numBefore = PAGE_SIZE, anchor = nextAnchorMessageId
                ) else
                    NetworkClient.client.getMessages(
                        NetworkClient.makeJSONArray(
                            listOf(
                                Pair("stream", stream),
                                Pair("topic", topic)
                            )
                        ), numBefore = PAGE_SIZE
                    ))
                    .map { messagesResponse ->
                        database.runInTransaction {
                            if (loadType == LoadType.REFRESH) {
                                database.messagesDAO().clearMessages()
                            }

                            val messagesProcessed =
                                processMessagesFromNetwork(messagesResponse.messges)

                            val messagesForDB =
                                (if (messagesProcessed.size == PAGE_SIZE + 1) messagesProcessed.subList(
                                    0, messagesProcessed.lastIndex
                                ) else
                                    messagesProcessed).map {
                                    convertMessageFromNetworkToDb(it, topic, stream)
                                }
                            if (MESSAGE_ANCHOR_TO_UPDATE == 0)
                                database.messagesDAO().insertMessages(messagesForDB).subscribe()
                            else {
                                database.messagesDAO().clearAndInsertInTransaction(
                                    listOf(
                                        convertMessageFromNetworkToDb(
                                            messagesProcessed.last(),
                                            topic,
                                            stream
                                        )
                                    ), stream, topic
                                )
                                MESSAGE_ANCHOR_TO_UPDATE = 0
                            }
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
        var MESSAGE_ANCHOR_TO_UPDATE = 0
        const val PAGE_SIZE = 20
        const val PREFETCH_SIZE = 5
        const val MAX_MESSAGES_TO_CACHE = 300
        const val INITIAL_LOAD_SIZE = 50
        const val NEWEST_MESSAGE = 1000000000
    }
}