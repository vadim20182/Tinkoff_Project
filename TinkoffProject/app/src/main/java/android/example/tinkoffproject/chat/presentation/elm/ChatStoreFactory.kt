package android.example.tinkoffproject.chat.presentation.elm

import vivid.money.elmslie.core.ElmStoreCompat

class ChatStoreFactory(
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