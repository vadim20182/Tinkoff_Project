package android.example.tinkoffproject.data

data class ChannelItem(
    val name: String,
    val isTopic: Boolean = false,
    var isExpanded: Boolean = false,
    val parentChannel: ChannelItem? = null
)
