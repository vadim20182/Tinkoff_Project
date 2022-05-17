package android.example.tinkoffproject.chat.common.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random

@Serializable
data class UserMessage(
    @SerialName("sender_id")
    val userId: Int,
    @SerialName("sender_full_name")
    val name: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("content")
    val messageText: String,
    @Transient
    val reactions: MutableMap<String, Int> = mutableMapOf(),
    @SerialName("reactions")
    val allReactions: List<Reaction> = emptyList(),
    @Transient
    val selectedReactions: MutableMap<String, Boolean> = mutableMapOf(),
    @SerialName("timestamp")
    val date: Long,
    @SerialName("id")
    val messageId: Int = Random.nextInt(10000),
    @Transient
    val fileLink: String? = null,
    @Transient
    val isSent: Boolean = true,
    @SerialName("subject")
    val topic: String,
    @SerialName("display_recipient")
    val channel: String
)