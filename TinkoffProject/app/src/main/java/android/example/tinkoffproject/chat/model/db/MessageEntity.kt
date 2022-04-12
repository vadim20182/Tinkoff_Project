package android.example.tinkoffproject.chat.model.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "message")
@TypeConverters(SelectedReactionsConverter::class, ReactionsConverter::class)
data class MessageEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "message_id")
    val messageId: Int,
    @ColumnInfo(name = "message_channel")
    val channelName: String,
    @ColumnInfo(name = "message_topic")
    val topicName: String,
    @ColumnInfo(name = "message_sender_id")
    val userId: Int,
    @ColumnInfo(name = "message_sender_name")
    val name: String,
    @ColumnInfo(name = "message_avatar_url")
    val avatarUrl: String? = null,
    @ColumnInfo(name = "message_text")
    val messageText: String,
    @ColumnInfo(name = "message_reactions")
    val reactions: MutableMap<String, Int> = mutableMapOf(),
    @ColumnInfo(name = "message_selected_reactions")
    val selectedReactions: MutableMap<String, Boolean> = mutableMapOf(),
    @ColumnInfo(name = "message_timestamp")
    val date: Long,
    @ColumnInfo(name = "message_is_sent")
    val isSent: Boolean = true
)

@Entity(tableName = "message_remote_keys")
data class MessageRemoteKeysEntity(
    @PrimaryKey
    @ColumnInfo(name = "message_remote_key_id")
    val messageId: Int,
    @ColumnInfo(name = "message_remote_key_prev_key")
    val prevKey: Int?,
    @ColumnInfo(name = "message_remote_key_next_key")
    val nextKey: Int?,
    @ColumnInfo(name = "message_remote_key_stream")
    val stream: String,
    @ColumnInfo(name = "message_remote_key_topic")
    val topic: String
)
