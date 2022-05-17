package android.example.tinkoffproject.chat.channel.presentation.elm

import android.example.tinkoffproject.chat.channel.di.ChannelChat
import vivid.money.elmslie.core.ElmStoreCompat
import javax.inject.Inject

@ChannelChat
class ChannelChatStoreFactory @Inject constructor(
    private val actor: ChannelChatActor
) {
    var topics: List<String>? = null
    private val store: ElmStoreCompat<ChannelChatEvent, ChannelChatState, ChannelChatEffect, ChannelChatCommand> by lazy {
        ElmStoreCompat(
            initialState = ChannelChatState(),
            reducer = ChannelChatReducer(),
            actor = actor
        )
    }

    fun provide() = store
}