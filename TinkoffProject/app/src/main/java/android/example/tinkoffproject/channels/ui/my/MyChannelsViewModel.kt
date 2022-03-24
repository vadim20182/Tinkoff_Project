package android.example.tinkoffproject.channels.ui.my

import android.example.tinkoffproject.channels.model.ChannelItem
import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel

class MyChannelsViewModel : BaseChannelsViewModel() {

    init {
        loadChannels()
    }

    override fun loadChannels() {
        val list = mutableListOf<ChannelItem>()
        for (i in 0..19 step 2) {
            val channel = ChannelItem("#Channel ${i + 1}")
            list.add(channel)
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
        _channels.value = list
    }
}