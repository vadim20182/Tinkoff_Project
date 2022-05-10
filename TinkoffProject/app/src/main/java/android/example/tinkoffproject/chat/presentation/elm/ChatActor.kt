package android.example.tinkoffproject.chat.presentation.elm

import android.example.tinkoffproject.chat.data.repository.ChatRepository
import android.example.tinkoffproject.chat.di.Chat
import android.example.tinkoffproject.chat.presentation.elm.ChatEvent.Internal
import io.reactivex.Observable
import vivid.money.elmslie.core.ActorCompat
import javax.inject.Inject

@Chat
class ChatActor @Inject constructor(private val chatRepository: ChatRepository) :
    ActorCompat<ChatCommand, Internal> {

    override fun execute(
        command: ChatCommand
    ): Observable<Internal> = when (command) {
        is ChatCommand.SendMessagePlaceholder -> chatRepository.sendMessagePlaceholder(command.messageText)
            .mapSuccessEvent(
                Internal.MessagePlaceholderIsSent
            )

        is ChatCommand.SendMessage -> chatRepository.sendMessage(command.messageText)
            .toObservable()
            .mapEvents({ Internal.MessageIsSent },
                { error -> Internal.SomeError(error) })

        is ChatCommand.UploadFile -> chatRepository.uploadFile(command.fileName, command.file)
            .toObservable()
            .mapEvents({ Internal.MessageIsSent }, { error -> Internal.SomeError(error) })

        is ChatCommand.ClickReaction -> chatRepository.reactionClicked(
            command.emoji_name,
            command.messageId
        )
            .toObservable()
            .mapEvents(
                { Internal.MessageUpdated(command.messageId) },
                { error -> Internal.SomeError(error) })

        is ChatCommand.AddReaction -> chatRepository.addReaction(
            command.emoji_name,
            command.messageId
        )
            .toObservable()
            .mapEvents({ Internal.MessageUpdated(command.messageId) },
                { error -> Internal.SomeError(error) })
        is ChatCommand.InitAdapter -> {
            chatRepository.getMessages()
                .toObservable()
                .mapSuccessEvent {
                    Internal.AdapterUpdated(it)
                }
        }
        is ChatCommand.InitLoad -> {
            chatRepository.getAllMessagesFromDb(
                chatRepository.channel,
                chatRepository.topic
            )
                .toObservable()
                .mapSuccessEvent {
                    if (it.isNotEmpty())
                        Internal.InitLoaded
                    else
                        Internal.InitLoading
                }
        }
        is ChatCommand.ClearMessages -> chatRepository.clearMessagesOnExit(
            chatRepository.channel,
            chatRepository.topic
        )
            .mapSuccessEvent(Internal.MessagesCleared)

    }
}