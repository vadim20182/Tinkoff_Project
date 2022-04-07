package android.example.tinkoffproject.channels.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChannelItem(
    @SerialName("name")
    val name: String,
    var isTopic: Boolean = false,
    var isExpanded: Boolean = false,
    var parentChannel: String? = null,
    @SerialName("stream_id")
    var streamID: Int = 0
)


