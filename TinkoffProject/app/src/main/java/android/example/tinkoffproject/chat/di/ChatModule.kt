package android.example.tinkoffproject.chat.di

import android.example.tinkoffproject.chat.data.db.MessagesDAO
import android.example.tinkoffproject.chat.data.db.MessagesRemoteMediator
import android.example.tinkoffproject.chat.data.repository.ChatRepository
import android.example.tinkoffproject.chat.data.repository.ChatRepositoryImpl
import android.example.tinkoffproject.database.AppDatabase
import android.example.tinkoffproject.network.ApiService
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier
import javax.inject.Scope

@Module(includes = [ChatModule.Bind::class])
class ChatModule {
    @Provides
    @Chat
    fun provideChatRepository(
        messagesDAO: MessagesDAO,
        remoteMediator: MessagesRemoteMediator,
        @Channel channel: String,
        @Topic topic: String,
        client: ApiService
    ): ChatRepositoryImpl =
        ChatRepositoryImpl(messagesDAO, remoteMediator, channel, topic, client)

    @Provides
    @Chat
    fun provideMessagesDAO(database: AppDatabase): MessagesDAO = database.messagesDAO()

    @Provides
    @Chat
    fun provideRemoteMediator(
        database: AppDatabase,
        @Channel channel: String,
        @Topic topic: String,
        client: ApiService
    ) =
        MessagesRemoteMediator(database, channel, topic, client)

    @Module
    interface Bind {
        @Binds
        @Chat
        fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository
    }
}

@Qualifier
annotation class Channel

@Qualifier
annotation class Topic

@Scope
annotation class Chat