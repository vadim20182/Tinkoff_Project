package android.example.tinkoffproject.chat.model.db

import androidx.paging.PagingSource
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.http.DELETE

@Dao
interface MessagesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(messages: List<MessageEntity>): Completable

    @Update
    fun updateMessages(messages: List<MessageEntity>): Completable

    @Delete
    fun deleteMessages(messages: List<MessageEntity>): Completable

    @Query("SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE (message_channel=:channel AND message_topic = :topic) ORDER BY message_timestamp DESC")
    fun getAllMessages(channel: String, topic: String): Flowable<List<MessageEntity>>

    @Query("SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE (message_channel=:channel AND message_topic = :topic) ORDER BY message_timestamp DESC")
    fun loadAllMessages(channel: String, topic: String): Single<List<MessageEntity>>

    @Query(
        "DELETE FROM ${MessageEntity.TABLE_NAME} WHERE (message_channel = :channel AND message_topic=:topic ) " +
                "AND message_id NOT IN " +
                "(SELECT message_id FROM ${MessageEntity.TABLE_NAME} WHERE (message_channel = :channel AND message_topic=:topic ) " +
                "ORDER BY message_timestamp DESC LIMIT 50)"
    )
    fun clearCachedMessages(channel: String, topic: String): Completable

    @Query(
        "DELETE FROM ${MessageEntity.TABLE_NAME} WHERE (message_channel = :channel AND message_topic=:topic ) " +
                "AND message_id IN " +
                "(SELECT message_id FROM ${MessageEntity.TABLE_NAME} WHERE (message_channel = :channel AND message_topic=:topic AND NOT message_is_sent))"
    )
    fun clearUnsentMessages(channel: String, topic: String): Completable


    @Query("DELETE FROM ${MessageEntity.TABLE_NAME}")
    fun clearMessages(): Completable

    @Query("SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE (message_channel=:channel AND message_topic = :topic) ORDER BY message_timestamp DESC")
    fun messagesPagingSource(channel: String, topic: String): PagingSource<Int, MessageEntity>
}