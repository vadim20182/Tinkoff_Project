package android.example.tinkoffproject.di

import android.content.Context
import android.example.tinkoffproject.database.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDataBase(context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
}