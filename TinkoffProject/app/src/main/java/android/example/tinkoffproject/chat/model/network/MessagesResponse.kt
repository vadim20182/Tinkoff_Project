package android.example.tinkoffproject.chat.model.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GetMessagesResponse {
    @SerialName("messages")
    var messges: List<UserMessage> = emptyList()
}

@Serializable
data class Reaction(
    @SerialName("emoji_name")
    val emoji_name: String,
    @SerialName("emoji_code")
    val emoji_code: String,
    @SerialName("user_id")
    val userId: Int,
)