package android.example.tinkoffproject.data

import android.example.tinkoffproject.R
import androidx.annotation.DrawableRes

data class UserMessage(
    val userId: Long,
    val name: String,
    @DrawableRes
    val avatar: Int = R.mipmap.avatar,
    val messageText: String,
    val reactions: MutableMap<String, Int> = mutableMapOf(),
    val selectedReactions: MutableMap<String, Boolean> = mutableMapOf(),
    val date: String = "1 Jan"
)
