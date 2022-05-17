package android.example.tinkoffproject.chat.channel.data.db

import android.example.tinkoffproject.chat.topic.data.db.TopicMessageEntity
import androidx.paging.PagingSource
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface ChannelMessagesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(messages: List<ChannelMessageEntity>): Completable

    @Query("SELECT * FROM ${ChannelMessageEntity.TABLE_NAME} WHERE (message_channel=:channel) ORDER BY message_timestamp DESC")
    fun getAllMessages(channel: String): Single<List<ChannelMessageEntity>>

    @Query("SELECT * FROM ${ChannelMessageEntity.TABLE_NAME} WHERE (message_id=:id)")
    fun getMessage(id: Int): Single<ChannelMessageEntity>

    @Query("UPDATE ${ChannelMessageEntity.TABLE_NAME} SET message_text=:text WHERE message_id = :id")
    fun updateMessage(text: String, id: Int): Completable

    @Query("UPDATE ${TopicMessageEntity.TABLE_NAME} SET message_text=:text WHERE message_id = :id")
    fun updateMessageExternal(text: String, id: Int): Completable

    @Query("UPDATE ${ChannelMessageEntity.TABLE_NAME} SET message_topic=:topic WHERE message_id = :id")
    fun updateTopic(topic: String, id: Int): Completable

    @Query("UPDATE ${TopicMessageEntity.TABLE_NAME} SET message_topic=:topic WHERE message_id = :id")
    fun updateTopicExternal(topic: String, id: Int): Completable

    @Query(
        "DELETE FROM ${ChannelMessageEntity.TABLE_NAME} WHERE (message_channel = :channel) " +
                "AND message_id NOT IN " +
                "(SELECT message_id FROM ${ChannelMessageEntity.TABLE_NAME} WHERE (message_channel = :channel) " +
                "ORDER BY message_timestamp DESC LIMIT 50)"
    )
    fun clearCachedMessages(channel: String): Completable

    @Query(
        "DELETE FROM ${ChannelMessageEntity.TABLE_NAME} WHERE (message_channel = :channel) " +
                "AND message_id IN " +
                "(SELECT message_id FROM ${ChannelMessageEntity.TABLE_NAME} WHERE (message_channel = :channel AND NOT message_is_sent))"
    )
    fun clearUnsentMessages(channel: String): Completable

    @Transaction
    fun insertAndRemoveInTransaction(messages: List<ChannelMessageEntity>, channel: String) {
        clearRemovedMessages(messages.map {
            it.messageId
        }, channel).subscribe()
        insertMessages(messages).subscribe()
    }

    @Transaction
    fun clearAndInsertInTransaction(messages: List<ChannelMessageEntity>, channel: String) {
        insertMessages(messages).subscribe()
        clearUnsentMessages(channel).subscribe()
    }

    @Query("DELETE FROM ${ChannelMessageEntity.TABLE_NAME} WHERE message_channel=:channel AND message_id NOT IN (:messagesIds)")
    fun clearRemovedMessages(messagesIds: List<Int>, channel: String): Completable

    @Query("DELETE FROM ${ChannelMessageEntity.TABLE_NAME}")
    fun clearMessages(): Completable

    @Query("DELETE FROM ${ChannelMessageEntity.TABLE_NAME} WHERE message_id=:messageId")
    fun deleteMessage(messageId: Int): Completable

    @Query("DELETE FROM ${TopicMessageEntity.TABLE_NAME} WHERE message_id=:messageId")
    fun deleteMessageExternal(messageId: Int): Completable

    @Query("SELECT * FROM ${ChannelMessageEntity.TABLE_NAME} WHERE (message_channel=:channel) ORDER BY message_timestamp DESC")
    fun messagesFromChannelPagingSource(channel: String): PagingSource<Int, ChannelMessageEntity>
}