package android.example.tinkoffproject.chat.model

import android.example.tinkoffproject.R
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.decodeFromStream
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
    var messageText: String,
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