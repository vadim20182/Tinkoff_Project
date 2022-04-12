package android.example.tinkoffproject.database

import android.content.Context
import android.example.tinkoffproject.channels.model.db.ChannelEntity
import android.example.tinkoffproject.channels.model.db.ChannelDAO
import android.example.tinkoffproject.chat.model.db.*
import android.example.tinkoffproject.contacts.model.db.ContactEntity
import android.example.tinkoffproject.contacts.model.db.ContactsDAO
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ChannelEntity::class, MessageEntity::class, ContactEntity::class, MessageRemoteKeysEntity::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDAO(): ChannelDAO

    abstract fun messageDAO(): MessageDAO

    abstract fun contactsDAO(): ContactsDAO

    abstract fun messageRemoteKeysDAO(): MessageRemoteKeysDAO

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
            ).allowMainThreadQueries()
                .build()
    }

}