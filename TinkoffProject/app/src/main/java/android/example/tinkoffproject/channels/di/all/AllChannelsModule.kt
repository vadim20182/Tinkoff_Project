package android.example.tinkoffproject.channels.di.all

import android.example.tinkoffproject.channels.data.db.ChannelsDAO
import android.example.tinkoffproject.channels.data.repository.tabs.AllChannelsRepositoryImpl
import android.example.tinkoffproject.channels.data.repository.tabs.ChannelsRepository
import android.example.tinkoffproject.channels.presentation.all.AllChannelsViewModel
import android.example.tinkoffproject.database.AppDatabase
import android.example.tinkoffproject.di.ViewModelKey
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap


@Module(includes = [AllChannelsModule.Bind::class])
class AllChannelsModule {

    @Provides
    @AllStreams
    fun provideChannelsDAO(database: AppDatabase): ChannelsDAO = database.channelsDAO()

    @Module
    interface Bind {
        @Binds
        @AllStreams
        fun bindChannelsRepository(impl: AllChannelsRepositoryImpl): ChannelsRepository
    }
}

@Module
abstract class AllViewModelModule {
    @AllStreams
    @Binds
    @IntoMap
    @ViewModelKey(AllChannelsViewModel::class)
    abstract fun bindViewModel(viewModel: AllChannelsViewModel): ViewModel
}