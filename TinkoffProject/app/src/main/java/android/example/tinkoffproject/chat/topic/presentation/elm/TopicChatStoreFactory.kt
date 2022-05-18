package android.example.tinkoffproject.chat.topic.presentation.elm

import android.example.tinkoffproject.chat.topic.di.Chat
import vivid.money.elmslie.core.ElmStoreCompat
import javax.inject.Inject

@Chat
class TopicChatStoreFactory @Inject constructor(
    private val actor: TopicChatActor
) {

    var topics: List<String>? = null
    private val store: ElmStoreCompat<ChatEvent, ChatState, ChatEffect, ChatCommand> by lazy {
        ElmStoreCompat(
            initialState = ChatState(),
            reducer = TopicChatReducer(),
            actor = actor
        )
    }

    fun provide() = store
}