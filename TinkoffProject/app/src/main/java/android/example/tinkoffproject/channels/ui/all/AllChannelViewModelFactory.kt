package android.example.tinkoffproject.channels.ui.all

import android.example.tinkoffproject.channels.model.ChannelsRepository
import android.example.tinkoffproject.channels.model.db.ChannelEntity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AllChannelViewModelFactory
    (
    private val channelsRepository: ChannelsRepository<ChannelEntity.AllChannelsEntity>
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AllChannelsViewModel::class.java))
            return AllChannelsViewModel(channelsRepository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}