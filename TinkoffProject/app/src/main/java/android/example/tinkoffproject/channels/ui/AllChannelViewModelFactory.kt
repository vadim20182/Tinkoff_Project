package android.example.tinkoffproject.channels.ui

import android.example.tinkoffproject.channels.model.db.ChannelDAO
import android.example.tinkoffproject.channels.ui.all.AllChannelsViewModel
import android.example.tinkoffproject.channels.ui.my.MyChannelsViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AllChannelViewModelFactory
    (
    private val channelsDAO: ChannelDAO
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AllChannelsViewModel::class.java))
            return AllChannelsViewModel(channelsDAO) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}