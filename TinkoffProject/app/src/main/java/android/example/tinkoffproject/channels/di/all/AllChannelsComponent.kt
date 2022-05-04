package android.example.tinkoffproject.channels.di.all

import android.example.tinkoffproject.channels.ui.all.AllChannelsFragment
import dagger.Subcomponent
import javax.inject.Scope


@AllStreams
@Subcomponent(
    modules = [AllChannelsModule::class, AllViewModelModule::class]
)
interface AllChannelsComponent {

    fun inject(allChannelsFragment: AllChannelsFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(): AllChannelsComponent
    }
}

@Scope
annotation class AllStreams