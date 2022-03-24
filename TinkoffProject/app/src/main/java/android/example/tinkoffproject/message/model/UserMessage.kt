package android.example.tinkoffproject.message.model

import android.example.tinkoffproject.R
import androidx.annotation.DrawableRes
import kotlin.random.Random

data class UserMessage(
    val userId: Long,
    val name: String,
    @DrawableRes
    val avatar: Int = R.mipmap.avatar,
    val messageText: String,
    val reactions: MutableMap<String, Int> = mutableMapOf(),
    val selectedReactions: MutableMap<String, Boolean> = mutableMapOf(),
    val date: String = "1 Jan",
    val messageId: Long = Random.nextLong(10000)
)
