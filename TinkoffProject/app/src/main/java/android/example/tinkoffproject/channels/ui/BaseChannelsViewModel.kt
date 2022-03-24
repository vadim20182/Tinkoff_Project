package android.example.tinkoffproject.channels.ui

import android.example.tinkoffproject.channels.model.ChannelItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseChannelsViewModel : ViewModel() {

    protected val _channels: MutableLiveData<List<ChannelItem>> =
        MutableLiveData<List<ChannelItem>>()
    val channels: LiveData<out List<ChannelItem>> = _channels

    protected val topics: MutableMap<String, MutableList<ChannelItem>> = mutableMapOf()

    private val currentChannels: List<ChannelItem>
        get() = _channels.value ?: emptyList()

    abstract fun loadChannels()

    private fun updateData(newList: List<ChannelItem>) {
        _channels.value = newList
    }

    fun showOrHideTopics(position: Int) {
        val newList = mutableListOf<ChannelItem>().apply { addAll(currentChannels) }
        if (currentChannels[position].isExpanded)
            collapseTopics(position, newList)
        else
            expandTopics(position, newList)
        newList[position].isExpanded = !currentChannels[position].isExpanded
        updateData(newList)
    }

    private fun expandTopics(position: Int, newList: MutableList<ChannelItem>) {
        topics[currentChannels[position].name]?.let {
            newList.addAll(position + 1, it)
        }
    }

    private fun collapseTopics(position: Int, newList: MutableList<ChannelItem>) {
        newList.removeAll {
            it.parentChannel == currentChannels[position].name
        }
    }
}