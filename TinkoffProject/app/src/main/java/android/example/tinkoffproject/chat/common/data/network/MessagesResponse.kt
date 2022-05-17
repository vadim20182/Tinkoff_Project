package android.example.tinkoffproject.chat.common.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GetMessagesResponse {
    @SerialName("messages")
    var messages: List<UserMessage> = emptyList()
}

@Serializable
data class GetSingleMessageResponse(
    @SerialName("message")
    val userMessage: UserMessage
)

@Serializable
data class Reaction(
    @SerialName("emoji_name")
    val emoji_name: String,
    @SerialName("emoji_code")
    val emoji_code: String,
    @SerialName("user_id")
    val userId: Int,
)

@Serializable
data class FileResponse(
    @SerialName("msg")
    val msg: String,
    @SerialName("result")
    val result: String,
    @SerialName("uri")
    val uri: String,
)