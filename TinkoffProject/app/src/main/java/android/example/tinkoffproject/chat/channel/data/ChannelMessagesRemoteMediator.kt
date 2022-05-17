package android.example.tinkoffproject.chat.channel.data

import android.example.tinkoffproject.chat.channel.data.db.ChannelMessageEntity
import android.example.tinkoffproject.chat.channel.di.ChannelChat
import android.example.tinkoffproject.chat.common.data.network.UserMessage
import android.example.tinkoffproject.database.AppDatabase
import android.example.tinkoffproject.network.ApiService
import android.example.tinkoffproject.network.NetworkCommon
import android.example.tinkoffproject.utils.convertMessageFromNetworkToChannelChatDb
import android.example.tinkoffproject.utils.processMessagesFromNetwork
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxRemoteMediator
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@ChannelChat
class ChannelMessagesRemoteMediator @Inject constructor(
    private val database: AppDatabase,
    private val stream: String,
    private val client: ApiService
) : RxRemoteMediator<Int, ChannelMessageEntity>() {

    override fun loadSingle(
        loadType: LoadType,
        state: PagingState<Int, ChannelMessageEntity>
    ): Single<MediatorResult> {
        var remoteKey: Int? = null
        when (loadType) {
            LoadType.REFRESH -> {
                remoteKey =
                    NEWEST_MESSAGE
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
                            database.channelMessagesDAO().insertMessages(messagesForDB)
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
                Pair("stream", stream)
            )
        ), numBefore = PAGE_SIZE, anchor = key
    ) else
        client.getMessages(
            NetworkCommon.makeJSONArray(
                listOf(
                    Pair("stream", stream)
                )
            ), numBefore = PAGE_SIZE
        )

    private fun processMessagesForDb(messagesProcessed: List<UserMessage>) =
        messagesProcessed.map {
            convertMessageFromNetworkToChannelChatDb(it)
        }


    companion object {
        const val PAGE_SIZE = 20
        const val PREFETCH_SIZE = 5
        const val MAX_MESSAGES_TO_CACHE = 300
        const val INITIAL_LOAD_SIZE = 50
        const val NEWEST_MESSAGE = 1000000000
    }
}