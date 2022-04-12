package android.example.tinkoffproject.channels.ui

import android.example.tinkoffproject.channels.model.db.ChannelDAO
import android.example.tinkoffproject.channels.ui.all.AllChannelsViewModel
import android.example.tinkoffproject.channels.ui.my.MyChannelsViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MyChannelViewModelFactory(
    private val channelsDAO: ChannelDAO
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyChannelsViewModel::class.java))
            return MyChannelsViewModel(channelsDAO) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}