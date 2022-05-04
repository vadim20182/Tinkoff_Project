package android.example.tinkoffproject.channels.di.my

import android.example.tinkoffproject.channels.ui.my.MyChannelsFragment
import dagger.Subcomponent
import javax.inject.Scope

@MyStreams
@Subcomponent(
    modules = [MyChannelsModule::class, MyViewModelModule::class]
)
interface MyChannelsComponent {

    fun inject(myChannelsFragment: MyChannelsFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(): MyChannelsComponent
    }
}

@Scope
annotation class MyStreams