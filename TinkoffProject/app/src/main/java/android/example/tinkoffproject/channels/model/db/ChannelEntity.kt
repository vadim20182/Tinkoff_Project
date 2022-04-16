package android.example.tinkoffproject.channels.model.db

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
open class ChannelEntity(
    @ColumnInfo(name = "stream_id") open val streamID: Int = 0,

    @ColumnInfo(name = "channel_name") open val name: String = "",

    @ColumnInfo(name = "channel_is_topic")
    open val isTopic: Boolean = false,
    @ColumnInfo(name = "channel_is_expanded")

    open val isExpanded: Boolean = false,
    @ColumnInfo(name = "channel_parent")

    open val parentChannel: String = "\t"
) {
    @Entity(
        tableName = MyChannelsEntity.TABLE_NAME,
        primaryKeys = ["channel_name", "channel_parent"]
    )
    class MyChannelsEntity(
        @ColumnInfo(name = "stream_id") override val streamID: Int = 0,

        @ColumnInfo(name = "channel_name") override val name: String = "",

        @ColumnInfo(name = "channel_is_topic")
        override val isTopic: Boolean = false,
        @ColumnInfo(name = "channel_is_expanded")

        override val isExpanded: Boolean = false,
        @ColumnInfo(name = "channel_parent")

        override val parentChannel: String = "\t",
        @ColumnInfo(name = "channel_is_my")
        val isMy: Boolean = false
    ) : ChannelEntity() {
        companion object {
            const val TABLE_NAME = "my_channels"
        }
    }

    @Entity(
        tableName = AllChannelsEntity.TABLE_NAME,
        primaryKeys = ["channel_name", "channel_parent"]
    )
    class AllChannelsEntity(
        @ColumnInfo(name = "stream_id") override val streamID: Int = 0,

        @ColumnInfo(name = "channel_name") override val name: String = "",

        @ColumnInfo(name = "channel_is_topic")
        override val isTopic: Boolean = false,
        @ColumnInfo(name = "channel_is_expanded")

        override val isExpanded: Boolean = false,
        @ColumnInfo(name = "channel_parent")
        override val parentChannel: String = "\t",

        ) : ChannelEntity() {
        companion object {
            const val TABLE_NAME = "all_channels"
        }
    }

}
