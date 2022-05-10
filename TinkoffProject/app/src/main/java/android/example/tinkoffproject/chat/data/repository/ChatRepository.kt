package android.example.tinkoffproject.chat.data.repository

import android.example.tinkoffproject.chat.data.db.MessageEntity
import androidx.paging.PagingData
import io.reactivex.Completable
import io.reactivex.Flowable
import okhttp3.MultipartBody

interface ChatRepository {
    val channel: String
    val topic: String
    fun getMessages(): Flowable<PagingData<MessageEntity>>

    fun getAllMessagesFromDb(channel: String, topic: String): Flowable<List<MessageEntity>>

    fun clearMessagesOnExit(channel: String, topic: String): Completable

    fun sendMessage(messageText: String): Flowable<String>

    fun sendMessagePlaceholder(messageText: String): Completable

    fun uploadFile(fileName: String, file: MultipartBody.Part): Flowable<String>

    fun reactionClicked(
        emoji_name: String,
        messageId: Int
    ): Flowable<String>


    fun addReaction(emoji_name: String, messageId: Int): Flowable<String>
}