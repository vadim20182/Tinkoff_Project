package android.example.tinkoffproject.channels.di.main

import android.example.tinkoffproject.channels.ui.MainChannelsFragment
import dagger.Subcomponent
import javax.inject.Scope

@MainStreams
@Subcomponent(
    modules = [MainChannelsModule::class, MainViewModelModule::class]
)
interface MainChannelsComponent {

    fun inject(mainChannelsFragment: MainChannelsFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainChannelsComponent
    }
}


@Scope
annotation class MainStreams