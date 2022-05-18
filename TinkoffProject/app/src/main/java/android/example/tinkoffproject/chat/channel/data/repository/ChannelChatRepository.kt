package android.example.tinkoffproject.chat.channel.data.repository

import android.example.tinkoffproject.channels.data.network.ChannelItem
import android.example.tinkoffproject.chat.channel.data.db.ChannelMessageEntity
import androidx.paging.PagingData
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import okhttp3.MultipartBody

interface ChannelChatRepository {
    val channel: String
    val disposables: MutableMap<String, Disposable>

    fun getMessages(): Flowable<PagingData<ChannelMessageEntity>>

    fun getAllMessagesFromDb(
        channel: String = this.channel
    ): Single<List<ChannelMessageEntity>>

    fun loadFromNetwork(): Single<Unit>

    fun getTopicsForChannel(channel: String = this.channel): Single<List<ChannelItem>>

    fun clearMessagesOnExit(channel: String = this.channel): Completable

    fun sendMessage(messageText: String, topic: String): Single<Unit>

    fun editMessage(messageText: String, messageId: Int): Single<Unit>

    fun changeTopic(topic: String, messageId: Int): Single<Unit>

    fun deleteMessage(messageId: Int): Single<Unit>

    fun sendMessagePlaceholder(messageText: String, topic: String): Completable

    fun uploadFile(fileName: String, file: MultipartBody.Part, topic: String): Single<Unit>

    fun reactionClicked(
        emoji_name: String,
        messageId: Int
    ): Single<Unit>

    fun addReaction(emoji_name: String, messageId: Int): Single<Unit>
}