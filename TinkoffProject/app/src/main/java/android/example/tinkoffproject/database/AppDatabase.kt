package android.example.tinkoffproject.database

import android.example.tinkoffproject.channels.data.db.ChannelEntity
import android.example.tinkoffproject.channels.data.db.ChannelsDAO
import android.example.tinkoffproject.chat.data.db.MessageEntity
import android.example.tinkoffproject.chat.data.db.MessagesDAO
import android.example.tinkoffproject.contacts.data.db.ContactEntity
import android.example.tinkoffproject.contacts.data.db.ContactsDAO
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ChannelEntity::class, MessageEntity::class, ContactEntity::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelsDAO(): ChannelsDAO

    abstract fun messagesDAO(): MessagesDAO

    abstract fun contactsDAO(): ContactsDAO
}