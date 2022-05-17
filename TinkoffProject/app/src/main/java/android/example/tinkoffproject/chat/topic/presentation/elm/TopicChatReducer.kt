package android.example.tinkoffproject.chat.topic.presentation.elm

import android.example.tinkoffproject.chat.topic.presentation.elm.ChatEvent.Internal
import android.example.tinkoffproject.chat.topic.presentation.elm.ChatEvent.Ui
import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer

class TopicChatReducer :
    ScreenDslReducer<ChatEvent, Ui, Internal, ChatState,
            ChatEffect, ChatCommand>(Ui::class, Internal::class) {
    private var messagePlaceholderIsSent = false

    override fun Result.internal(event: Internal) = when (event) {
        is Internal.InitLoaded -> {
            if (event.result.isNotEmpty) {
                state { copy(isLoading = false, pagingData = event.result.pagingData) }
            }
            commands { +ChatCommand.InitNetwork }
        }
        is Internal.NetworkLoaded -> {
            commands {
                +ChatCommand.InitAdapter
            }
        }
        is Internal.MessagesCleared -> {}
        is Internal.AdapterUpdated -> {
            state { copy(isLoading = false, pagingData = event.pagingData) }
        }
        is Internal.MessagePlaceholderIsSent -> {
            messagePlaceholderIsSent = true
        }
        is Internal.MessageIsSent -> {}
        is Internal.MessageUpdated -> {}
        is Internal.MessageIsDeleted -> {}
        is Internal.MessageIsMoved -> {}
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
        is Ui.EditMessage -> {
            commands { +ChatCommand.EditMessage(event.messageText, event.messageId) }
        }
        is Ui.ChangeTopic -> {
            commands { +ChatCommand.ChangeTopic(event.topic, event.messageId) }
        }
        is Ui.DeleteMessage -> {
            commands { +ChatCommand.DeleteMessage(event.messageId) }
        }
        is Ui.InitLoad -> {
            state { copy(isLoading = true) }
            commands {
                if (state.pagingData == null)
                    +ChatCommand.InitLoad
                else +ChatCommand.InitAdapter
            }
        }
        is Ui.UploadFile -> {
            commands {
                +ChatCommand.SendMessagePlaceholder(event.fileName)
                +ChatCommand.UploadFile(event.fileName, event.file)
            }
        }
    }

}