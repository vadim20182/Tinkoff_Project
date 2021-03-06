package android.example.tinkoffproject.chat.topic.data

import android.example.tinkoffproject.chat.common.data.network.UserMessage
import android.example.tinkoffproject.chat.topic.data.db.TopicMessageEntity
import android.example.tinkoffproject.chat.topic.di.Chat
import android.example.tinkoffproject.database.AppDatabase
import android.example.tinkoffproject.network.ApiService
import android.example.tinkoffproject.network.NetworkCommon
import android.example.tinkoffproject.utils.convertMessageFromNetworkToTopicChatDb
import android.example.tinkoffproject.utils.processMessagesFromNetwork
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxRemoteMediator
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@Chat
class TopicMessagesRemoteMediator @Inject constructor(
    private val database: AppDatabase,
    private val stream: String,
    private val topic: String,
    private val client: ApiService
) : RxRemoteMediator<Int, TopicMessageEntity>() {

    override fun loadSingle(
        loadType: LoadType,
        state: PagingState<Int, TopicMessageEntity>
    ): Single<MediatorResult> {
        var remoteKey: Int? = null
        when (loadType) {
            LoadType.REFRESH -> {
                remoteKey = NEWEST_MESSAGE
            }
            LoadType.PREPEND -> {
                return Single.just(MediatorResult.Success(true))
            }
            LoadType.APPEND -> {
                val nextAnchorMessageId = state.lastItemOrNull()?.messageId

                if (nextAnchorMessageId != null)
                    remoteKey = nextAnchorMessageId
                else {
                    return if (!state.isEmpty())
                        Single.just(MediatorResult.Success(true))
                    else
                        Single.just(MediatorResult.Success(false))
                }
            }
        }
        return Single.just(remoteKey)
            .subscribeOn(Schedulers.io())
            .flatMap<MediatorResult> { nextAnchorMessageId ->
                return@flatMap (getResponseSingle(nextAnchorMessageId))
                    .map { messagesResponse ->
                        database.runInTransaction {
                            val messagesProcessed =
                                processMessagesFromNetwork(messagesResponse.messages)

                            val messagesForDB = processMessagesForDb(messagesProcessed)
                            database.messagesDAO().insertMessages(messagesForDB)
                                .subscribe()
                        }
                        MediatorResult.Success(endOfPaginationReached = messagesResponse.messages.size < PAGE_SIZE)
                    }
            }
            .onErrorResumeNext {
                return@onErrorResumeNext Single.just(MediatorResult.Error(it))
            }
    }

    override fun initializeSingle(): Single<InitializeAction> {
        return Single.just(InitializeAction.LAUNCH_INITIAL_REFRESH)
    }

    private fun getResponseSingle(key: Int) = if (key !=
        NEWEST_MESSAGE
    ) client.getMessagesWithAnchor(
        NetworkCommon.makeJSONArray(
            listOf(
                Pair("stream", stream),
                Pair("topic", topic)
            )
        ), numBefore = PAGE_SIZE, anchor = key
    ) else
        client.getMessages(
            NetworkCommon.makeJSONArray(
                listOf(
                    Pair("stream", stream),
                    Pair("topic", topic)
                )
            ), numBefore = PAGE_SIZE
        )

    private fun processMessagesForDb(messagesProcessed: List<UserMessage>) =
        messagesProcessed.map {
            convertMessageFromNetworkToTopicChatDb(it)
        }

    companion object {
        const val PAGE_SIZE = 20
        const val PREFETCH_SIZE = 5
        const val MAX_MESSAGES_TO_CACHE = 300
        const val INITIAL_LOAD_SIZE = 50
        const val NEWEST_MESSAGE = 1000000000
    }
}