package android.example.tinkoffproject.stub

import android.example.tinkoffproject.chat.topic.data.db.TopicMessageEntity
import android.example.tinkoffproject.chat.topic.data.db.TopicMessagesDAO
import android.example.tinkoffproject.chat.topic.data.repository.TopicChatRepository
import androidx.paging.PagingData
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody

class TopicChatRepositoryStub(
    private val topicMessagesDAO: TopicMessagesDAO
) : TopicChatRepository {
    override val channel: String = "default"
    override val topic: String = "default"
    override val disposables: MutableMap<String, Disposable>
        get() = TODO("Not yet implemented")

    override fun editMessage(messageText: String, messageId: Int): Single<Unit> {
        TODO("Not yet implemented")
    }

    override fun changeTopic(topic: String, messageId: Int): Single<Unit> {
        TODO("Not yet implemented")
    }

    override fun loadFromNetwork(): Single<Unit> {
        TODO("Not yet implemented")
    }

    override fun deleteMessage(messageId: Int): Single<Unit> {
        TODO("Not yet implemented")
    }

    override fun getMessages(): Flowable<PagingData<TopicMessageEntity>> {
        TODO("Not yet implemented")
    }

    override fun getAllMessagesFromDb(
        channel: String,
        topic: String
    ): Single<List<TopicMessageEntity>> {
        TODO("Not yet implemented")
    }

    override fun clearMessagesOnExit(channel: String, topic: String): Completable =
        topicMessagesDAO.clearCachedMessages(channel, topic)
            .subscribeOn(Schedulers.io())

    override fun sendMessage(messageText: String): Single<Unit> {
        TODO("Not yet implemented")
    }

    override fun sendMessagePlaceholder(messageText: String): Completable {
        TODO("Not yet implemented")
    }

    override fun uploadFile(fileName: String, file: MultipartBody.Part): Single<Unit> {
        TODO("Not yet implemented")
    }

    override fun reactionClicked(emoji_name: String, messageId: Int): Single<Unit> {
        TODO("Not yet implemented")
    }

    override fun addReaction(emoji_name: String, messageId: Int): Single<Unit> {
        TODO("Not yet implemented")
    }
}