package android.example.tinkoffproject.channels.di.my

import android.example.tinkoffproject.channels.data.db.ChannelsDAO
import android.example.tinkoffproject.channels.data.repository.ChannelsRepository
import android.example.tinkoffproject.channels.data.repository.MyChannelsRepositoryImpl
import android.example.tinkoffproject.channels.presentation.my.MyChannelsViewModel
import android.example.tinkoffproject.database.AppDatabase
import android.example.tinkoffproject.di.ViewModelKey
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module(includes = [MyChannelsModule.Bind::class])
class MyChannelsModule {

    @Provides
    @MyStreams
    fun provideChannelsDAO(database: AppDatabase): ChannelsDAO = database.channelsDAO()

    @Module
    interface Bind {
        @Binds
        @MyStreams
        fun bindChannelsRepository(impl: MyChannelsRepositoryImpl): ChannelsRepository
    }
}

@Module
abstract class MyViewModelModule {
    @MyStreams
    @Binds
    @IntoMap
    @ViewModelKey(MyChannelsViewModel::class)
    abstract fun bindViewModel(viewModel: MyChannelsViewModel): ViewModel
}