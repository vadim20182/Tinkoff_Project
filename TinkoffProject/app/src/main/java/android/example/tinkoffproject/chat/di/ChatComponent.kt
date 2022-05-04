package android.example.tinkoffproject.chat.di

import android.example.tinkoffproject.chat.ui.ChatFragment
import android.example.tinkoffproject.di.AppComponent
import dagger.BindsInstance
import dagger.Component

@Chat
@Component(modules = [ChatModule::class], dependencies = [AppComponent::class])
interface ChatComponent {

    fun inject(chatFragment: ChatFragment)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance
            @Channel
            channel: String,
            @BindsInstance
            @Topic
            topic: String,
            appComponent: AppComponent
        ): ChatComponent
    }
}

