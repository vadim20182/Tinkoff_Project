package android.example.tinkoffproject.channels.ui.my

import android.example.tinkoffproject.channels.model.ChannelsRepository
import android.example.tinkoffproject.channels.model.db.ChannelEntity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MyChannelViewModelFactory(
    private val channelsRepository: ChannelsRepository<ChannelEntity.MyChannelsEntity>
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyChannelsViewModel::class.java))
            return MyChannelsViewModel(channelsRepository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}