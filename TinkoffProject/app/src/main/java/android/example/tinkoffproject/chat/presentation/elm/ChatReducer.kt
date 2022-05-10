package android.example.tinkoffproject.chat.presentation.elm

import android.example.tinkoffproject.chat.presentation.elm.ChatEvent.Internal
import android.example.tinkoffproject.chat.presentation.elm.ChatEvent.Ui
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer

class ChatReducer :
    ScreenDslReducer<ChatEvent, Ui, Internal, ChatState,
            ChatEffect, ChatCommand>(Ui::class, Internal::class) {
    private var messagePlaceholderIsSent = false

    override fun Result.internal(event: Internal) = when (event) {
        is Internal.InitLoaded -> {
            state { copy(isLoading = false) }
        }
        is Internal.InitLoading -> {}
        is Internal.MessagesCleared -> {}
        is Internal.AdapterUpdated -> {
            effects { +ChatEffect.AdapterUpdated(event.pagingData/*data*/) }
        }
        is Internal.MessagePlaceholderIsSent -> {
            messagePlaceholderIsSent = true
        }
        is Internal.MessageIsSent -> {
            effects { +ChatEffect.MessageIsSent }
        }
        is Internal.MessageUpdated -> {
            effects { +ChatEffect.MessageReactionUpdated(event.posToUpdate) }
        }
        is Internal.SomeError -> {
            effects { +ChatEffect.SomeError(event.error) }
        }
    }

    override fun Result.ui(event: Ui) = when (event) {
        is Ui.AdapterUpdated -> {
            if (messagePlaceholderIsSent)
                effects { +ChatEffect.MessagePlaceholderIsSent }
            messagePlaceholderIsSent = false
        }
        is Ui.AddReaction -> {
            commands {
                +ChatCommand.AddReaction(
                    event.emoji_name,
                    event.messageId
                )
            }
        }
        is Ui.ClickReaction -> {
            commands {
                +ChatCommand.ClickReaction(
                    event.emoji_name,
                    event.messageId
                )
            }
        }
        is Ui.SendMessage -> {
            commands {
                +ChatCommand.SendMessagePlaceholder(event.messageText)
                +ChatCommand.SendMessage(event.messageText)
            }
        }
        is Ui.InitLoad -> {
            state { copy(isLoading = true) }
            commands {
                +ChatCommand.InitAdapter
                +ChatCommand.InitLoad
            }
        }
        is Ui.UploadFile -> {
            commands {
                +ChatCommand.SendMessagePlaceholder(event.fileName)
                +ChatCommand.UploadFile(event.fileName, event.file)
            }
        }
        is Ui.ClearMessages -> {
            commands { +ChatCommand.ClearMessages }
        }
    }

}