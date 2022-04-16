package android.example.tinkoffproject.chat.model

import android.example.tinkoffproject.chat.model.db.MessageEntity
import android.example.tinkoffproject.chat.model.db.MessagesDAO
import android.example.tinkoffproject.chat.model.db.MessagesRemoteMediator
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

@ExperimentalPagingApi
class ChatRepository(
    private val messagesDAO: MessagesDAO,
    private val remoteMediator: MessagesRemoteMediator
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

    fun updateMessages(messages: List<MessageEntity>): Disposable =
        messagesDAO.updateMessages(messages)
            .subscribeOn(Schedulers.io())
            .subscribe()

    fun loadMessagesFromDb(channel: String, topic: String) =
        messagesDAO.loadAllMessages(channel, topic)
            .subscribeOn(Schedulers.io())

    fun getAllMessagesFromDb(channel: String, topic: String) =
        messagesDAO.getAllMessages(channel, topic)
            .subscribeOn(Schedulers.io())


    fun clearUnsentMessages(channel: String, topic: String): Disposable =
        messagesDAO.clearUnsentMessages(channel, topic)
            .subscribeOn(Schedulers.io())
            .subscribe()

    fun insertMessagesReplace(messages: List<MessageEntity>): Disposable =
        messagesDAO.insertMessages(messages)
            .subscribeOn(Schedulers.io())
            .subscribe()

    fun clearMessagesOnExit(channel: String, topic: String): Disposable =
        messagesDAO.clearCachedMessages(channel, topic)
            .subscribeOn(Schedulers.io())
            .subscribe()
}