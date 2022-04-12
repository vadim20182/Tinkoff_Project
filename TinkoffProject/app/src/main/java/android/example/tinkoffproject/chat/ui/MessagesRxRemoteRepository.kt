package android.example.tinkoffproject.chat.ui

import android.example.tinkoffproject.chat.model.db.MessageEntity
import android.example.tinkoffproject.chat.model.db.MessagesRemoteMediator
import android.example.tinkoffproject.database.AppDatabase
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import io.reactivex.Flowable

@ExperimentalPagingApi
class MessagesRxRemoteRepository(
    private val database: AppDatabase,
    private val remoteMediator: MessagesRemoteMediator
) {
    fun getMessages(): Flowable<PagingData<MessageEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = MessagesRemoteMediator.PAGE_SIZE,
                enablePlaceholders = true,
                maxSize = 300,
                prefetchDistance = MessagesRemoteMediator.PREFETCH_SIZE,
                initialLoadSize = MessagesRemoteMediator.INITIAL_LOAD_SIZE
            ),
            remoteMediator = remoteMediator,
            pagingSourceFactory = {
                database.messageDAO()
                    .messagesPagingSource(remoteMediator.stream, remoteMediator.topic)
            }
        ).flowable
    }


}