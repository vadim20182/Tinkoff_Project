package android.example.tinkoffproject.chat.presentation.elm

import android.example.tinkoffproject.chat.di.Chat
import vivid.money.elmslie.core.ElmStoreCompat
import javax.inject.Inject

@Chat
class ChatStoreFactory @Inject constructor(
    private val actor: ChatActor
) {

    private val store: ElmStoreCompat<ChatEvent, ChatState, ChatEffect, ChatCommand> by lazy {
        ElmStoreCompat(
            initialState = ChatState(),
            reducer = ChatReducer(),
            actor = actor
        )
    }

    fun provide() = store
}