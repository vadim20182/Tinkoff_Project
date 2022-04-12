package android.example.tinkoffproject.channels.model.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channel")
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "stream_id")
    val streamID: Int = 0,

    @ColumnInfo(name = "channel_name")
    val name: String,

    @ColumnInfo(name = "channel_is_topic")
    val isTopic: Boolean = false,

    @ColumnInfo(name = "channel_is_expanded")
    val isExpanded: Boolean = false,

    @ColumnInfo(name = "channel_parent")
    val parentChannel: String? = null,

    @ColumnInfo(name = "channel_is_my")
    val isMy: Boolean = false
)
