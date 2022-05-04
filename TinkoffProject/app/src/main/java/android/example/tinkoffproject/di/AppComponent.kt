package android.example.tinkoffproject.di

import android.content.Context
import android.example.tinkoffproject.MainActivity
import android.example.tinkoffproject.channels.di.all.AllChannelsComponent
import android.example.tinkoffproject.channels.di.my.MyChannelsComponent
import android.example.tinkoffproject.contacts.di.ContactsComponent
import android.example.tinkoffproject.database.AppDatabase
import android.example.tinkoffproject.network.ApiService
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import javax.inject.Singleton

@Singleton
@Component(
    modules = [NetworkModule::class, DatabaseModule::class,
        ViewModelBuilderModule::class, SubcomponentsModule::class]
)
interface AppComponent {
    fun inject(activity: MainActivity)

    fun getDatabase(): AppDatabase

    fun getClient(): ApiService

    fun myChannelsComponent(): MyChannelsComponent.Factory
    fun allChannelsComponent(): AllChannelsComponent.Factory
    fun contactsComponent(): ContactsComponent.Factory

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance
            context: Context
        ): AppComponent
    }
}

@Module(
    subcomponents = [
        MyChannelsComponent::class,
        AllChannelsComponent::class,
        ContactsComponent::class
    ]
)
object SubcomponentsModule