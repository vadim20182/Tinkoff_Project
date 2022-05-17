package android.example.tinkoffproject.stub

import android.example.tinkoffproject.chat.topic.data.db.TopicMessageEntity
import android.example.tinkoffproject.chat.topic.data.db.TopicMessagesDAO
import androidx.paging.PagingSource
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class TopicMessagesDAOStub : TopicMessagesDAO {
    override fun insertMessages(messages: List<TopicMessageEntity>): Completable {
        TODO("Not yet implemented")
    }

    override fun updateMessage(text: String, id: Int): Completable {
        TODO("Not yet implemented")
    }

    override fun updateMessageExternal(text: String, id: Int): Completable {
        TODO("Not yet implemented")
    }

    override fun getAllMessages(channel: String, topic: String): Single<List<TopicMessageEntity>> {
        TODO("Not yet implemented")
    }

    override fun getMessage(id: Int): Single<TopicMessageEntity> {
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

    override fun updateTopic(topic: String, id: Int): Completable {
        TODO("Not yet implemented")
    }

    override fun updateTopicExternal(topic: String, id: Int): Completable {
        TODO("Not yet implemented")
    }

    override fun deleteMessage(messageId: Int): Completable {
        TODO("Not yet implemented")
    }

    override fun deleteMessageExternal(messageId: Int): Completable {
        TODO("Not yet implemented")
    }

    override fun clearRemovedMessages(
        messagesIds: List<Int>,
        channel: String,
        topic: String
    ): Completable {
        TODO("Not yet implemented")
    }

    override fun messagesPagingSource(
        channel: String,
        topic: String
    ): PagingSource<Int, TopicMessageEntity> {
        TODO("Not yet implemented")
    }
}