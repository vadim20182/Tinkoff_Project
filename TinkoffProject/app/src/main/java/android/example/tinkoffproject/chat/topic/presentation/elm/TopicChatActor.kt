package android.example.tinkoffproject.chat.topic.presentation.elm

import android.example.tinkoffproject.chat.topic.data.repository.TopicChatRepository
import android.example.tinkoffproject.chat.topic.di.Chat
import android.example.tinkoffproject.chat.topic.presentation.elm.ChatEvent.Internal
import android.example.tinkoffproject.utils.MessagesFromDbInitLoad
import android.example.tinkoffproject.utils.convertTopicMessageFromDbToUi
import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.rxjava2.cachedIn
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import vivid.money.elmslie.core.ActorCompat
import javax.inject.Inject

@Chat
class TopicChatActor @Inject constructor(
    private val topicChatRepository: TopicChatRepository,
    private val coroutineScope: CoroutineScope
) :
    ActorCompat<ChatCommand, Internal> {

    override fun execute(
        command: ChatCommand
    ): Observable<Internal> = when (command) {
        is ChatCommand.SendMessagePlaceholder -> topicChatRepository.sendMessagePlaceholder(command.messageText)
            .mapSuccessEvent(
                Internal.MessagePlaceholderIsSent
            )

        is ChatCommand.SendMessage -> topicChatRepository.sendMessage(command.messageText)
            .toObservable()
            .mapEvents({ Internal.MessageIsSent },
                { error -> Internal.SomeError(error) })

        is ChatCommand.EditMessage -> topicChatRepository.editMessage(
            command.messageText,
            command.messageId
        )
            .toObservable()
            .mapEvents({ Internal.MessageUpdated },
                { error -> Internal.SomeError(error) })

        is ChatCommand.ChangeTopic -> topicChatRepository.changeTopic(
            command.topic,
            command.messageId
        )
            .toObservable()
            .mapEvents({ Internal.MessageIsMoved },
                { error -> Internal.SomeError(error) })

        is ChatCommand.DeleteMessage -> topicChatRepository.deleteMessage(command.messageId)
            .toObservable()
            .mapEvents({ Internal.MessageIsDeleted },
                { error -> Internal.SomeError(error) })

        is ChatCommand.UploadFile -> topicChatRepository.uploadFile(command.fileName, command.file)
            .toObservable()
            .mapEvents({ Internal.MessageIsSent }, { error -> Internal.SomeError(error) })

        is ChatCommand.ClickReaction -> topicChatRepository.reactionClicked(
            command.emoji_name,
            command.messageId
        )
            .toObservable()
            .mapEvents(
                { Internal.MessageUpdated },
                { error -> Internal.SomeError(error) })

        is ChatCommand.AddReaction -> topicChatRepository.addReaction(
            command.emoji_name,
            command.messageId
        )
            .toObservable()
            .mapEvents({ Internal.MessageUpdated },
                { error -> Internal.SomeError(error) })

        is ChatCommand.InitAdapter -> topicChatRepository.getMessages()
            .map { pagingData ->
                pagingData.map { convertTopicMessageFromDbToUi(it) }
            }
            .toObservable()
            .cachedIn(coroutineScope)
            .mapSuccessEvent {
                Internal.AdapterUpdated(it)
            }

        is ChatCommand.InitLoad -> topicChatRepository.getAllMessagesFromDb()
            .toObservable()
            .map { messages ->
                messages.map { convertTopicMessageFromDbToUi(it) }
            }
            .mapSuccessEvent {
                Internal.InitLoaded(MessagesFromDbInitLoad(it.isNotEmpty(), PagingData.from(it)))
            }

        is ChatCommand.InitNetwork -> topicChatRepository.loadFromNetwork()
            .mapSuccessEvent {
                Internal.NetworkLoaded
            }

        is ChatCommand.ClearMessages -> topicChatRepository.clearMessagesOnExit()
            .mapSuccessEvent(Internal.MessagesCleared)

    }
}