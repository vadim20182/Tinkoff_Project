package android.example.tinkoffproject.di

import android.content.Context
import android.example.tinkoffproject.database.AppDatabase
import androidx.room.Room
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDataBase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, "Tinkoff_Project.db"
        )
            .build()
    }
}