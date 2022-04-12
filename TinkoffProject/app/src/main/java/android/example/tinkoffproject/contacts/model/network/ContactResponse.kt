package android.example.tinkoffproject.contacts.model.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
class GetUsersResponse {
    @SerialName("members")
    val users: List<ContactItem> = emptyList()
}

@Serializable
data class UserResponse(
    @SerialName("user")
    val user: ContactItem
)

@Serializable
data class GetPresenceResponse(
    @SerialName("presence")
    val presence: Presence
)

@Serializable
data class Presence(
    @SerialName("aggregated")
    val clientType: UserStatus
)

@Serializable
data class UserStatus(
    @SerialName("status")
    val status: String
)