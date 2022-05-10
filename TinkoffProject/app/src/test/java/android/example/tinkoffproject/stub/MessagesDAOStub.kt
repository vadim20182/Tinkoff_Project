package android.example.tinkoffproject.stub

import android.example.tinkoffproject.chat.data.db.MessageEntity
import android.example.tinkoffproject.chat.data.db.MessagesDAO
import androidx.paging.PagingSource
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class MessagesDAOStub : MessagesDAO {
    override fun insertMessages(messages: List<MessageEntity>): Completable {
        TODO("Not yet implemented")
    }

    override fun getAllMessages(channel: String, topic: String): Flowable<List<MessageEntity>> {
        TODO("Not yet implemented")
    }

    override fun getMessage(id: Int): Single<MessageEntity> {
        TODO("Not yet implemented")
    }

    override fun clearCachedMessages(channel: String, topic: String) =
        Completable.fromCallable { "Cleared" }

    override fun clearUnsentMessages(channel: String, topic: String): Completable {
        TODO("Not yet implemented")
    }

    override fun clearMessages(): Completable {
        TODO("Not yet implemented")
    }

    override fun messagesPagingSource(
        channel: String,
        topic: String
    ): PagingSource<Int, MessageEntity> {
        TODO("Not yet implemented")
    }
}