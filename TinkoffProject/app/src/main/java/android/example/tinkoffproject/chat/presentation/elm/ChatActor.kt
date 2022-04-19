package android.example.tinkoffproject.chat.presentation.elm

import android.example.tinkoffproject.chat.data.ChatRepository
import android.example.tinkoffproject.chat.presentation.elm.ChatEvent.Internal
import io.reactivex.Observable
import vivid.money.elmslie.core.ActorCompat
import java.util.concurrent.TimeUnit

class ChatActor(private val chatRepository: ChatRepository) : ActorCompat<ChatCommand, Internal> {

    override fun execute(
        command: ChatCommand
    ): Observable<Internal> = when (command) {
        is ChatCommand.SendMessagePlaceholder -> chatRepository.sendMessagePlaceholder(command.messageText)
            .mapSuccessEvent(
                Internal.MessagePlaceholderIsSent
            )
            .delay(
                70,
                TimeUnit.MILLISECONDS
            )// задержка для того, чтобы адаптер успел обновиться для скролла в начало сообщений

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
        is ChatCommand.InitLoad -> chatRepository.getAllMessagesFromDb(
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
}