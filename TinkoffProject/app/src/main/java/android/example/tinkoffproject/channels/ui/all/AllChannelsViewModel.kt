package android.example.tinkoffproject.channels.ui.all

import android.example.tinkoffproject.channels.model.ChannelItem
import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel

class AllChannelsViewModel : BaseChannelsViewModel() {

    init {
        loadChannels()
    }

    override fun loadChannels() {
        val newList = mutableListOf<ChannelItem>()
        for (i in 0..19) {
            val channel = ChannelItem("#Channel ${i + 1}")
            newList.add(channel)
            if (topics[channel.name] == null)
                topics[channel.name] = mutableListOf()
            for (j in 0..5) {
                topics[channel.name]?.add(
                    ChannelItem(
                        "TOPIC ${j + 1}",
                        true,
                        parentChannel = channel.name
                    )
                )
            }
        }
        _channels.value = newList
    }
}