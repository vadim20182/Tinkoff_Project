package android.example.tinkoffproject.screens.channels.tabviewmodels

import android.example.tinkoffproject.data.ChannelItem

class AllChannelsViewModel : BaseChannelsViewModel() {

    init {
        loadChannels()
    }

    override fun loadChannels() {
        _channels.value = mutableListOf()
        for (i in 0..19) {
            val channel = ChannelItem("#Channel ${i + 1}")
            addChannel(channel)
            if (topics[channel.name] == null)
                topics[channel.name] = mutableListOf()
            for (j in 0..5) {
                topics[channel.name]?.add(
                    ChannelItem(
                        "TOPIC ${j + 1}",
                        true,
                        parentChannel = channel
                    )
                )
            }
        }
    }
}