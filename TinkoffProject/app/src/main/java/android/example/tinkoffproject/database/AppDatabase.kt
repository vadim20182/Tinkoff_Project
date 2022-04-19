package android.example.tinkoffproject.database

import android.content.Context
import android.example.tinkoffproject.channels.data.db.ChannelEntity
import android.example.tinkoffproject.channels.data.db.ChannelsDAO
import android.example.tinkoffproject.chat.data.db.*
import android.example.tinkoffproject.contacts.data.db.ContactEntity
import android.example.tinkoffproject.contacts.data.db.ContactsDAO
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ChannelEntity::class, MessageEntity::class, ContactEntity::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelsDAO(): ChannelsDAO

    abstract fun messagesDAO(): MessagesDAO

    abstract fun contactsDAO(): ContactsDAO

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "Tinkoff_Project.db"
            )
                .build()
    }

}