package android.example.tinkoffproject.database

import android.content.Context
import android.example.tinkoffproject.channels.model.db.AllChannelsDAO
import android.example.tinkoffproject.channels.model.db.ChannelEntity
import android.example.tinkoffproject.channels.model.db.MyChannelsDAO
import android.example.tinkoffproject.chat.model.db.*
import android.example.tinkoffproject.contacts.model.db.ContactEntity
import android.example.tinkoffproject.contacts.model.db.ContactsDAO
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ChannelEntity.AllChannelsEntity::class, ChannelEntity.MyChannelsEntity::class, MessageEntity::class, ContactEntity::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun allChannelsDAO(): AllChannelsDAO

    abstract fun myChannelsDAO(): MyChannelsDAO

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