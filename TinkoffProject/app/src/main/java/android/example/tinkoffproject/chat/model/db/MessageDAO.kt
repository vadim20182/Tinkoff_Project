package android.example.tinkoffproject.chat.model.db

import androidx.paging.PagingSource
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface MessageDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(messages: List<MessageEntity>)//: Completable

    @Update
    fun updateMessages(messages: List<MessageEntity>)//: Completable

    @Delete
    fun deleteMessages(messages: List<MessageEntity>)//: Completable

    @Query("DELETE FROM message")
    fun clearMessages()

    @Query("SELECT * FROM message ORDER BY message_timestamp ASC")
    fun getAllMessages(): List<MessageEntity>//Single<List<MessageEntity>>

//    @Query("SELECT * FROM message WHERE ((mes LIKE :name) OR (channel_parent LIKE :name AND channel_is_topic))")
//    fun findMessagesByName(name: String): Single<List<MessageEntity?>>

    @Query("SELECT * FROM message WHERE (message_channel=:channel AND message_topic = :topic) ORDER BY message_id DESC")
    fun messagesPagingSource(channel: String, topic: String): PagingSource<Int, MessageEntity>
}