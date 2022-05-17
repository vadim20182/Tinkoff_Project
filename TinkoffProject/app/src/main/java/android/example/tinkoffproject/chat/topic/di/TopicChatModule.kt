package android.example.tinkoffproject.chat.topic.di

import android.example.tinkoffproject.chat.topic.data.TopicMessagesRemoteMediator
import android.example.tinkoffproject.chat.topic.data.db.TopicMessagesDAO
import android.example.tinkoffproject.chat.topic.data.repository.TopicChatRepository
import android.example.tinkoffproject.chat.topic.data.repository.TopicChatRepositoryImpl
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

@Module(includes = [ChatModule.Bind::class])
class ChatModule {

    @Provides
    @Chat
    fun provideCoroutineScope(): CoroutineScope =
        CoroutineScope(Job() + Dispatchers.IO)


    @Provides
    @Chat
    fun provideChatRepository(
        topicMessagesDAO: TopicMessagesDAO,
        remoteMediator: TopicMessagesRemoteMediator,
        @Channel channel: String,
        @Topic topic: String,
        client: ApiService
    ): TopicChatRepositoryImpl =
        TopicChatRepositoryImpl(topicMessagesDAO, remoteMediator, channel, topic, client)

    @Provides
    @Chat
    fun provideMessagesDAO(database: AppDatabase): TopicMessagesDAO = database.messagesDAO()

    @Provides
    @Chat
    fun provideRemoteMediator(
        database: AppDatabase,
        @Channel channel: String,
        @Topic topic: String,
        client: ApiService
    ) =
        TopicMessagesRemoteMediator(database, channel, topic, client)

    @Module
    interface Bind {
        @Binds
        @Chat
        fun bindChatRepository(impl: TopicChatRepositoryImpl): TopicChatRepository
    }
}

@Qualifier
annotation class Channel

@Qualifier
annotation class Topic

@Scope
annotation class Chat