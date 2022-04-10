package android.example.tinkoffproject.chat.model

import kotlinx.serialization.*
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
    val date: Long = 100,
    @SerialName("id")
    val messageId: Int = Random.nextInt(10000),
    @Transient
    val isSent: Boolean = true
)