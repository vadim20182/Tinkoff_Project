package android.example.tinkoffproject.channels.data.repository

import android.example.tinkoffproject.channels.data.network.ChannelItem
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface ChannelsRepository {

    val searchObservable: Observable<String>
    val allChannels: MutableList<ChannelItem>
    var currentChannels: List<ChannelItem>
    val topics: MutableMap<String, List<ChannelItem>>
    val queryReset: PublishSubject<String>
    val queryChannelClick: PublishSubject<Int>

    val channelClickObservable: Observable<Pair<Int, List<ChannelItem>>>

    val resetSearchObservable: Observable<String>

    fun filterChannels(input: String) = allChannels.filter {
        (it.name.contains(
            Regex(
                input,
                RegexOption.IGNORE_CASE
            )
        ) && !it.isTopic) || (it.parentChannel.contains(
            Regex(input, RegexOption.IGNORE_CASE)
        ))
    }

    fun showOrHideTopics(position: Int): Pair<Int, List<ChannelItem>> {
        val newList =
            mutableListOf<ChannelItem>().apply { addAll(currentChannels) }
        if (currentChannels[position].isExpanded)
            collapseTopics(position, newList)
        else
            expandTopics(position, newList)
        newList[position] =
            newList[position].copy(isExpanded = !currentChannels[position].isExpanded)
        allChannels[allChannels.indexOf(currentChannels[position])] =
            allChannels[allChannels.indexOf(currentChannels[position])].copy(
                isExpanded = newList[position].isExpanded
            )
        return Pair(position, newList)
    }

    fun expandTopics(position: Int, newList: MutableList<ChannelItem>) {
        topics[currentChannels[position].name]?.let {
            newList.addAll(position + 1, it)
            allChannels.addAll(
                allChannels.indexOf(
                    currentChannels[position]
                ) + 1, it
            )
        }
    }

    fun collapseTopics(position: Int, newList: MutableList<ChannelItem>) {
        newList.removeAll {
            it.parentChannel == currentChannels[position].name
        }
        allChannels.removeAll {
            it.parentChannel == currentChannels[position].name
        }
    }
}