package android.example.tinkoffproject.screens.channels.tabviewmodels

import android.example.tinkoffproject.data.ChannelItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseChannelsViewModel : ViewModel() {

    protected val _channels: MutableLiveData<MutableList<ChannelItem>> =
        MutableLiveData<MutableList<ChannelItem>>()
    val channels: LiveData<out List<ChannelItem>> = _channels

    protected val topics: MutableMap<String, MutableList<ChannelItem>> = mutableMapOf()

    private val currentChannels: MutableList<ChannelItem>
        get() = _channels.value ?: mutableListOf()

    abstract fun loadChannels()

    fun showOrHideTopics(position: Int): Int {
        val count = if (currentChannels[position].isExpanded)
            collapseTopics(position)
        else
            expandTopics(position)
        currentChannels[position].isExpanded = !currentChannels[position].isExpanded
        return count
    }

    protected fun addChannel(channel: ChannelItem) {
        if (channel !in currentChannels)
            currentChannels.add(channel)
    }

    private fun expandTopics(position: Int): Int {
        var size = 0
        topics[currentChannels[position].name]?.let {
            currentChannels.addAll(position + 1, it)
            size = it.size
        }
        return size
    }

    private fun collapseTopics(position: Int): Int {
        val count = currentChannels.size
        currentChannels.removeAll {
            it.parentChannel == currentChannels[position]
        }
        return -(count - currentChannels.size)
    }

}