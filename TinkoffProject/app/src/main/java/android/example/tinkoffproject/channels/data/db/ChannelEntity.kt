package android.example.tinkoffproject.channels.data.db

import android.example.tinkoffproject.channels.data.db.ChannelEntity.Companion.TABLE_NAME
import androidx.room.ColumnInfo
import androidx.room.Entity


@Entity(
    tableName = TABLE_NAME,
    primaryKeys = ["channel_name", "channel_parent", "channel_is_my"]
)
data class ChannelEntity(
    @ColumnInfo(name = "stream_id") val streamID: Int = 0,

    @ColumnInfo(name = "channel_name") val name: String = "",

    @ColumnInfo(name = "channel_is_topic")
    val isTopic: Boolean = false,
    @ColumnInfo(name = "channel_is_expanded")

    val isExpanded: Boolean = false,
    @ColumnInfo(name = "channel_parent")
    val parentChannel: String = NO_PARENT,
    @ColumnInfo(name = "channel_is_my")
    val isMy: Boolean = false
) {
    companion object {
        const val NO_PARENT = "\t"
        const val TABLE_NAME = "channels_and_topics"
    }
}


