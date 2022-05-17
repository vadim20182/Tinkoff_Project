package android.example.tinkoffproject.chat.topic.di

import android.example.tinkoffproject.chat.topic.data.repository.TopicChatRepository
import android.example.tinkoffproject.chat.topic.ui.TopicChatFragment
import android.example.tinkoffproject.di.AppComponent
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineScope

@Chat
@Component(modules = [ChatModule::class], dependencies = [AppComponent::class])
interface TopicChatComponent {

    fun inject(topicChatFragment: TopicChatFragment)

    @Chat
    fun getCoroutineScope(): CoroutineScope

    fun getTopicChatRepository(): TopicChatRepository

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance
            @Channel channel: String,
            @BindsInstance
            @Topic topic: String,
            appComponent: AppComponent
        ): TopicChatComponent
    }
}

