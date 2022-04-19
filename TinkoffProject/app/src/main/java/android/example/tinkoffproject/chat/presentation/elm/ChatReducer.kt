package android.example.tinkoffproject.chat.presentation.elm

import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer
import android.example.tinkoffproject.chat.presentation.elm.ChatEvent.Internal
import android.example.tinkoffproject.chat.presentation.elm.ChatEvent.Ui

class ChatReducer :
    ScreenDslReducer<ChatEvent, Ui, Internal, ChatState,
            ChatEffect, ChatCommand>(Ui::class, Internal::class) {

    override fun Result.internal(event: Internal) = when (event) {
        is Internal.InitLoaded -> {
            state { copy(isLoading = false) }
        }
        is Internal.InitLoading -> {
            state { copy(isLoading = true) }
        }
        is Internal.MessagePlaceholderIsSent -> {
            effects { +ChatEffect.MessagePlaceholderIsSent }
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
            commands { +ChatCommand.InitLoad }
        }
        is Ui.UploadFile -> {
            commands {
                +ChatCommand.SendMessagePlaceholder(event.fileName)
                +ChatCommand.UploadFile(event.fileName, event.file)
            }
        }
    }

}