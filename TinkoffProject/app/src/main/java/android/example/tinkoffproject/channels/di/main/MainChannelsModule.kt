package android.example.tinkoffproject.channels.di.main

import android.example.tinkoffproject.channels.data.repository.main.MainChannelsRepository
import android.example.tinkoffproject.channels.data.repository.main.MainChannelsRepositoryImpl
import android.example.tinkoffproject.channels.presentation.MainChannelsViewModel
import android.example.tinkoffproject.di.ViewModelKey
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module(includes = [MainChannelsModule.Bind::class])
class MainChannelsModule {

    @Module
    interface Bind {
        @Binds
        @MainStreams
        fun bindChannelsRepository(impl: MainChannelsRepositoryImpl): MainChannelsRepository
    }
}

@Module
abstract class MainViewModelModule {
    @MainStreams
    @Binds
    @IntoMap
    @ViewModelKey(MainChannelsViewModel::class)
    abstract fun bindViewModel(viewModel: MainChannelsViewModel): ViewModel
}