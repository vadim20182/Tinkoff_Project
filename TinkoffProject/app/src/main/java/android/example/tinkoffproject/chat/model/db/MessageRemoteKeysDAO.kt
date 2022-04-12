package android.example.tinkoffproject.chat.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageRemoteKeysDAO {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(remoteKey: List<MessageRemoteKeysEntity>)

    @Query("SELECT * FROM message_remote_keys WHERE message_remote_key_id = :messageId")
    fun remoteKeysByMessageId(messageId: Int): MessageRemoteKeysEntity

    @Query("SELECT * FROM message_remote_keys WHERE (message_remote_key_stream = :stream AND message_remote_key_topic=:topic) ORDER BY message_remote_key_id DESC")
    fun getRemoteKeys(stream: String, topic: String): List<MessageRemoteKeysEntity>

    @Query("DELETE FROM message_remote_keys")
    fun clearRemoteKeys()

}