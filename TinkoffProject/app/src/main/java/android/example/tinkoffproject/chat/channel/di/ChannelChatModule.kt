package android.example.tinkoffproject.chat.channel.di

import android.example.tinkoffproject.chat.channel.data.ChannelMessagesRemoteMediator
import android.example.tinkoffproject.chat.channel.data.db.ChannelMessagesDAO
import android.example.tinkoffproject.chat.channel.data.repository.ChannelChatRepository
import android.example.tinkoffproject.chat.channel.data.repository.ChannelChatRepositoryImpl
import android.example.tinkoffproject.database.AppDatabase
import android.example.tinkoffproject.network.ApiService
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Qualifier
import javax.inject.Scope

@Module(includes = [ChannelChatModule.Bind::class])
class ChannelChatModule {

    @Provides
    @ChannelChat
    fun provideCoroutineScope(): CoroutineScope =
        CoroutineScope(Job() + Dispatchers.IO)

    @Provides
    @ChannelChat
    fun provideChannelChatRepository(
        messagesDAO: ChannelMessagesDAO,
        remoteMediator: ChannelMessagesRemoteMediator,
        @Stream channel: String,
        client: ApiService
    ): ChannelChatRepositoryImpl =
        ChannelChatRepositoryImpl(messagesDAO, remoteMediator, channel, client)

    @Provides
    @ChannelChat
    fun provideMessagesDAO(database: AppDatabase): ChannelMessagesDAO =
        database.channelMessagesDAO()

    @Provides
    @ChannelChat
    fun provideRemoteMediator(
        database: AppDatabase,
        @Stream channel: String,
        client: ApiService
    ) =
        ChannelMessagesRemoteMediator(database, channel, client)

    @Module
    interface Bind {
        @Binds
        @ChannelChat
        fun bindChannelChatRepository(impl: ChannelChatRepositoryImpl): ChannelChatRepository
    }
}

@Qualifier
annotation class Stream

@Qualifier
annotation class StreamId

@Scope
annotation class ChannelChat