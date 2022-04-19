package android.example.tinkoffproject.contacts.data.db

import android.example.tinkoffproject.contacts.data.db.ContactEntity.Companion.TABLE_NAME
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = TABLE_NAME)
data class ContactEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "contact_id")
    val userId: Int,
    @ColumnInfo(name = "contact_name")
    val name: String,
    @ColumnInfo(name = "contact_email")
    val email: String,
    @ColumnInfo(name = "contact_status")
    val status: String = "offline",
    @ColumnInfo(name = "contact_avatar_url")
    val avatarUrl: String? = null
){
    companion object {
        const val TABLE_NAME = "contacts"
    }
}
