package android.example.tinkoffproject.stub

import android.example.tinkoffproject.chat.data.db.MessageEntity
import android.example.tinkoffproject.chat.data.db.MessagesDAO
import android.example.tinkoffproject.chat.data.repository.ChatRepository
import androidx.paging.PagingData
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody

class ChatRepositoryStub(
    private val messagesDAO: MessagesDAO
) : ChatRepository {
    override val channel: String = "default"
    override val topic: String = "default"

    override fun getMessages(): Flowable<PagingData<MessageEntity>> {
        TODO("Not yet implemented")
    }

    override fun getAllMessagesFromDb(
        channel: String,
        topic: String
    ): Flowable<List<MessageEntity>> {
        TODO("Not yet implemented")
    }

    override fun clearMessagesOnExit(channel: String, topic: String): Completable =
        messagesDAO.clearCachedMessages(channel, topic)
            .subscribeOn(Schedulers.io())

    override fun sendMessage(messageText: String): Flowable<String> {
        TODO("Not yet implemented")
    }

    override fun sendMessagePlaceholder(messageText: String): Completable {
        TODO("Not yet implemented")
    }

    override fun uploadFile(fileName: String, file: MultipartBody.Part): Flowable<String> {
        TODO("Not yet implemented")
    }

    override fun reactionClicked(emoji_name: String, messageId: Int): Flowable<String> {
        TODO("Not yet implemented")
    }

    override fun addReaction(emoji_name: String, messageId: Int): Flowable<String> {
        TODO("Not yet implemented")
    }
}