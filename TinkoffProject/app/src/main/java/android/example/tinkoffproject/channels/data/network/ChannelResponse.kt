package android.example.tinkoffproject.channels.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionStatus(@SerialName("is_subscribed") val isSubscribed: Boolean)

@Serializable
data class GetChannelsResponse(
    @SerialName("streams")
    val channelsList: List<ChannelItem>
)

@Serializable
data class GetSubscribedChannelsResponse(
    @SerialName("subscriptions")
    val channelsList: List<ChannelItem>
)

@Serializable
data class GetTopicsResponse(
    @SerialName("topics")
    val channelsList: List<ChannelItem>
)