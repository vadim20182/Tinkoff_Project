package android.example.tinkoffproject.contacts.model

import android.example.tinkoffproject.R
import androidx.annotation.DrawableRes

data class ContactItem(
    val contactId: Long,
    val name: String, val email: String,
    @DrawableRes
    val avatarId: Int = R.mipmap.avatar
)
