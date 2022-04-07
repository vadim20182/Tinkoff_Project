package android.example.tinkoffproject.contacts.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContactItem(
    @SerialName("user_id")
    val userId: Int,
    @SerialName("full_name")
    val name: String,
    @SerialName("email")
    val email: String,
    @SerialName("status")
    var status: String = "idle",
    @SerialName("is_bot")
    var isBot: Boolean = false,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)



