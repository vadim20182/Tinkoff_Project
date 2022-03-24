package android.example.tinkoffproject.channels.model

data class ChannelItem(
    val name: String,
    val isTopic: Boolean = false,
    var isExpanded: Boolean = false,
    val parentChannel: String? = null
)
