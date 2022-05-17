package android.example.tinkoffproject.chat.topic.data.db

import android.example.tinkoffproject.chat.channel.data.db.ChannelMessageEntity
import androidx.paging.PagingSource
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TopicMessagesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(messages: List<TopicMessageEntity>): Completable

    @Query("SELECT * FROM ${TopicMessageEntity.TABLE_NAME} WHERE (message_channel=:channel AND message_topic = :topic) ORDER BY message_timestamp DESC")
    fun getAllMessages(channel: String, topic: String): Single<List<TopicMessageEntity>>

    @Query("SELECT * FROM ${TopicMessageEntity.TABLE_NAME} WHERE (message_id=:id)")
    fun getMessage(id: Int): Single<TopicMessageEntity>

    @Query("UPDATE ${TopicMessageEntity.TABLE_NAME} SET message_text=:text WHERE message_id = :id")
    fun updateMessage(text: String, id: Int): Completable

    @Query("UPDATE ${ChannelMessageEntity.TABLE_NAME} SET message_text=:text WHERE message_id = :id")
    fun updateMessageExternal(text: String, id: Int): Completable

    @Query("UPDATE ${TopicMessageEntity.TABLE_NAME} SET message_topic=:topic WHERE message_id = :id")
    fun updateTopic(topic: String, id: Int): Completable

    @Query("UPDATE ${ChannelMessageEntity.TABLE_NAME} SET message_topic=:topic WHERE message_id = :id")
    fun updateTopicExternal(topic: String, id: Int): Completable

    @Query(
        "DELETE FROM ${TopicMessageEntity.TABLE_NAME} WHERE (message_channel = :channel AND message_topic=:topic ) " +
                "AND message_id NOT IN " +
                "(SELECT message_id FROM ${TopicMessageEntity.TABLE_NAME} WHERE (message_channel = :channel AND message_topic=:topic ) " +
                "ORDER BY message_timestamp DESC LIMIT 50)"
    )
    fun clearCachedMessages(channel: String, topic: String): Completable

    @Query(
        "DELETE FROM ${TopicMessageEntity.TABLE_NAME} WHERE (message_channel = :channel AND message_topic=:topic ) " +
                "AND message_id IN " +
                "(SELECT message_id FROM ${TopicMessageEntity.TABLE_NAME} WHERE (message_channel = :channel AND message_topic=:topic AND NOT message_is_sent))"
    )
    fun clearUnsentMessages(channel: String, topic: String): Completable

    @Transaction
    fun insertAndRemoveInTransaction(
        messages: List<TopicMessageEntity>,
        channel: String,
        topic: String
    ) {
        clearRemovedMessages(messages.map {
            it.messageId
        }, channel, topic).subscribe()
        insertMessages(messages).subscribe()
    }

    @Transaction
    fun clearAndInsertInTransaction(messages: List<TopicMessageEntity>, channel: String, topic: String) {
        insertMessages(messages).subscribe()
        clearUnsentMessages(channel, topic).subscribe()
    }

    @Query("DELETE FROM ${TopicMessageEntity.TABLE_NAME} WHERE message_channel=:channel AND message_topic=:topic AND message_id NOT IN (:messagesIds)")
    fun clearRemovedMessages(messagesIds: List<Int>, channel: String, topic: String): Completable

    @Query("DELETE FROM ${TopicMessageEntity.TABLE_NAME}")
    fun clearMessages(): Completable

    @Query("DELETE FROM ${TopicMessageEntity.TABLE_NAME} WHERE message_id=:messageId")
    fun deleteMessage(messageId: Int): Completable

    @Query("DELETE FROM ${ChannelMessageEntity.TABLE_NAME} WHERE message_id=:messageId")
    fun deleteMessageExternal(messageId: Int): Completable

    @Query("SELECT * FROM ${TopicMessageEntity.TABLE_NAME} WHERE (message_channel=:channel AND message_topic = :topic) ORDER BY message_timestamp DESC")
    fun messagesPagingSource(channel: String, topic: String): PagingSource<Int, TopicMessageEntity>

}