package android.example.tinkoffproject.chat.topic.data.db

import android.example.tinkoffproject.chat.common.data.db.ReactionsConverter
import android.example.tinkoffproject.chat.common.data.db.SelectedReactionsConverter
import android.example.tinkoffproject.chat.topic.data.db.TopicMessageEntity.Companion.TABLE_NAME
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = TABLE_NAME)
@TypeConverters(SelectedReactionsConverter::class, ReactionsConverter::class)
data class TopicMessageEntity(
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
    val isSent: Boolean = true,
    @ColumnInfo(name = "message_file_link")
    val fileLink: String? = null,
    @ColumnInfo(name = "message_is_my")
    val isMyMessage: Boolean = false
) {
    companion object {
        const val TABLE_NAME = "messages"
    }
}