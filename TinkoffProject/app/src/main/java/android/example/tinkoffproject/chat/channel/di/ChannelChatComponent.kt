package android.example.tinkoffproject.chat.channel.di

import android.example.tinkoffproject.chat.channel.data.repository.ChannelChatRepository
import android.example.tinkoffproject.chat.channel.ui.ChannelChatFragment
import android.example.tinkoffproject.di.AppComponent
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineScope


@ChannelChat
@Component(modules = [ChannelChatModule::class], dependencies = [AppComponent::class])
interface ChannelChatComponent {

    fun inject(channelChatFragment: ChannelChatFragment)

    @ChannelChat
    fun getCoroutineScope(): CoroutineScope

    fun getChannelChatRepository(): ChannelChatRepository

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance
            @Stream channel: String,
            appComponent: AppComponent
        ): ChannelChatComponent
    }
}