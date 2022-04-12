package android.example.tinkoffproject.channels.model.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChannelItem(
    @SerialName("name")
    val name: String,
    val isTopic: Boolean = false,
    val isExpanded: Boolean = false,
    val parentChannel: String? = null,
    @SerialName("stream_id")
    val streamID: Int = 0
)


