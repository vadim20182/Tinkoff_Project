package android.example.tinkoffproject.chat.channel.presentation.elm

import vivid.money.elmslie.core.store.dsl_reducer.ScreenDslReducer

class ChannelChatReducer :
    ScreenDslReducer<ChannelChatEvent, ChannelChatEvent.Ui, ChannelChatEvent.Internal, ChannelChatState,
            ChannelChatEffect, ChannelChatCommand>(
        ChannelChatEvent.Ui::class,
        ChannelChatEvent.Internal::class
    ) {
    private var messagePlaceholderIsSent = 0

    override fun Result.internal(event: ChannelChatEvent.Internal) = when (event) {
        is ChannelChatEvent.Internal.InitLoaded -> {
            if (event.result.isNotEmpty) {
                state { copy(isLoading = false, pagingData = event.result.pagingData) }
            }
            commands { +ChannelChatCommand.InitNetwork }
        }
        is ChannelChatEvent.Internal.NetworkLoaded -> {
            commands {
                +ChannelChatCommand.InitAdapter
            }
        }
        is ChannelChatEvent.Internal.InitTopics -> {
            effects { +ChannelChatEffect.TopicsLoaded(event.topics) }
        }
        is ChannelChatEvent.Internal.MessagesCleared -> {}
        is ChannelChatEvent.Internal.AdapterUpdated -> {
            state { copy(isLoading = false, pagingData = event.pagingData) }
        }
        is ChannelChatEvent.Internal.MessagePlaceholderIsSent -> {
            messagePlaceholderIsSent += 1
        }
        is ChannelChatEvent.Internal.MessageIsSent -> {}
        is ChannelChatEvent.Internal.MessageUpdated -> {}
        is ChannelChatEvent.Internal.MessageIsDeleted -> {}
        is ChannelChatEvent.Internal.MessageIsMoved -> {}
        is ChannelChatEvent.Internal.SomeError -> {
            effects { +ChannelChatEffect.SomeError(event.error) }
        }
    }

    override fun Result.ui(event: ChannelChatEvent.Ui) = when (event) {
        is ChannelChatEvent.Ui.AdapterUpdated -> {
            if (messagePlaceholderIsSent == 2) {
                effects { +ChannelChatEffect.MessagePlaceholderIsSent }
                messagePlaceholderIsSent = 0
            }
            if (messagePlaceholderIsSent > 0)
                messagePlaceholderIsSent += 1
            else { }
        }
        is ChannelChatEvent.Ui.AddReaction -> {
            commands {
                +ChannelChatCommand.AddReaction(
                    event.emoji_name,
                    event.messageId
                )
            }
        }
        is ChannelChatEvent.Ui.ClickReaction -> {
            commands {
                +ChannelChatCommand.ClickReaction(
                    event.emoji_name,
                    event.messageId
                )
            }
        }
        is ChannelChatEvent.Ui.SendMessage -> {
            commands {
                +ChannelChatCommand.SendMessagePlaceholder(event.messageText, event.topic)
                +ChannelChatCommand.SendMessage(event.messageText, event.topic)
            }
        }
        is ChannelChatEvent.Ui.EditMessage -> {
            commands { +ChannelChatCommand.EditMessage(event.messageText, event.messageId) }
        }
        is ChannelChatEvent.Ui.ChangeTopic -> {
            commands { +ChannelChatCommand.ChangeTopic(event.topic, event.messageId) }
        }
        is ChannelChatEvent.Ui.DeleteMessage -> {
            commands { +ChannelChatCommand.DeleteMessage(event.messageId) }
        }
        is ChannelChatEvent.Ui.InitLoad -> {
            state { copy(isLoading = true) }
            commands {
                if (state.pagingData == null)
                    +ChannelChatCommand.InitLoad
                else +ChannelChatCommand.InitAdapter
                +ChannelChatCommand.InitTopics
            }
        }
        is ChannelChatEvent.Ui.UploadFile -> {
            commands {
                +ChannelChatCommand.SendMessagePlaceholder(event.fileName, event.topic)
                +ChannelChatCommand.UploadFile(event.fileName, event.file, event.topic)
            }
        }
    }

}