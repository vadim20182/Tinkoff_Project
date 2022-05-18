package android.example.tinkoffproject.chat.topic.data.repository

import android.example.tinkoffproject.channels.data.network.ChannelItem
import android.example.tinkoffproject.chat.topic.data.db.TopicMessageEntity
import androidx.paging.PagingData
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import okhttp3.MultipartBody

interface TopicChatRepository {
    val channel: String
    val topic: String
    val disposables: MutableMap<String, Disposable>

    fun getMessages(): Flowable<PagingData<TopicMessageEntity>>

    fun getAllMessagesFromDb(
        channel: String = this.channel,
        topic: String = this.topic
    ): Single<List<TopicMessageEntity>>

    fun loadFromNetwork(): Single<Unit>

    fun getTopicsForChannel(channel: String = this.channel): Single<List<ChannelItem>>

    fun clearMessagesOnExit(channel: String = this.channel, topic: String = this.topic): Completable

    fun sendMessage(messageText: String): Single<Unit>

    fun editMessage(messageText: String, messageId: Int): Single<Unit>

    fun changeTopic(topic: String, messageId: Int): Single<Unit>

    fun deleteMessage(messageId: Int): Single<Unit>

    fun sendMessagePlaceholder(messageText: String): Completable

    fun uploadFile(fileName: String, file: MultipartBody.Part): Single<Unit>

    fun reactionClicked(
        emoji_name: String,
        messageId: Int
    ): Single<Unit>

    fun addReaction(emoji_name: String, messageId: Int): Single<Unit>
}