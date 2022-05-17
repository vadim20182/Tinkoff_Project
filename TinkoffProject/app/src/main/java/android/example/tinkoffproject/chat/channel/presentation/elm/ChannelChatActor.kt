package android.example.tinkoffproject.chat.channel.presentation.elm

import android.example.tinkoffproject.chat.channel.data.repository.ChannelChatRepository
import android.example.tinkoffproject.chat.channel.di.ChannelChat
import android.example.tinkoffproject.chat.common.ui.UiModel
import android.example.tinkoffproject.utils.MessagesFromDbInitLoad
import android.example.tinkoffproject.utils.convertChannelMessageFromDbToUi
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.paging.rxjava2.cachedIn
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import vivid.money.elmslie.core.ActorCompat
import javax.inject.Inject

@ChannelChat
class ChannelChatActor @Inject constructor(
    private val channelChatRepository: ChannelChatRepository,
    private val coroutineScope: CoroutineScope
) :
    ActorCompat<ChannelChatCommand, ChannelChatEvent.Internal> {

    override fun execute(
        command: ChannelChatCommand
    ): Observable<ChannelChatEvent.Internal> = when (command) {
        is ChannelChatCommand.SendMessagePlaceholder -> channelChatRepository.sendMessagePlaceholder(
            command.messageText,
            command.topic
        )
            .mapSuccessEvent(
                ChannelChatEvent.Internal.MessagePlaceholderIsSent
            )

        is ChannelChatCommand.SendMessage -> channelChatRepository.sendMessage(
            command.messageText,
            command.topic
        )
            .toObservable()
            .mapEvents({ ChannelChatEvent.Internal.MessageIsSent },
                { error -> ChannelChatEvent.Internal.SomeError(error) })

        is ChannelChatCommand.EditMessage -> channelChatRepository.editMessage(
            command.messageText,
            command.messageId
        )
            .toObservable()
            .mapEvents({ ChannelChatEvent.Internal.MessageUpdated },
                { error -> ChannelChatEvent.Internal.SomeError(error) })

        is ChannelChatCommand.ChangeTopic -> channelChatRepository.changeTopic(
            command.topic,
            command.messageId
        )
            .toObservable()
            .mapEvents({ ChannelChatEvent.Internal.MessageIsMoved },
                { error -> ChannelChatEvent.Internal.SomeError(error) })

        is ChannelChatCommand.DeleteMessage -> channelChatRepository.deleteMessage(command.messageId)
            .toObservable()
            .mapEvents({ ChannelChatEvent.Internal.MessageIsDeleted },
                { error -> ChannelChatEvent.Internal.SomeError(error) })

        is ChannelChatCommand.UploadFile -> channelChatRepository.uploadFile(
            command.fileName,
            command.file,
            command.topic
        )
            .toObservable()
            .mapEvents(
                { ChannelChatEvent.Internal.MessageIsSent },
                { error -> ChannelChatEvent.Internal.SomeError(error) })

        is ChannelChatCommand.ClickReaction -> channelChatRepository.reactionClicked(
            command.emoji_name,
            command.messageId
        )
            .toObservable()
            .mapEvents(
                { ChannelChatEvent.Internal.MessageUpdated },
                { error -> ChannelChatEvent.Internal.SomeError(error) })

        is ChannelChatCommand.AddReaction -> channelChatRepository.addReaction(
            command.emoji_name,
            command.messageId
        )
            .toObservable()
            .mapEvents({ ChannelChatEvent.Internal.MessageUpdated },
                { error -> ChannelChatEvent.Internal.SomeError(error) })

        is ChannelChatCommand.InitAdapter -> channelChatRepository.getMessages()
            .toObservable()
            .map { pagingData ->
                pagingData.map { convertChannelMessageFromDbToUi(it) }
            }
            .map {
                it.insertSeparators { before, after ->
                    if (before == null) {
                        // we're at the beginning of the list
                        return@insertSeparators null
                    }

                    if (after == null) {
                        // we're at the end of the list
                        return@insertSeparators UiModel.SeparatorMessageItem(
                            before.topicName ?: "uf",
                            before.date
                        )
                    }
                    // check between 2 items
                    if (before.topicName != after.topicName) {
                        UiModel.SeparatorMessageItem(before.topicName ?: "ef", before.date)
                    } else {
                        // no separator
                        null
                    }
                }
            }
            .cachedIn(coroutineScope)
            .mapSuccessEvent {
                ChannelChatEvent.Internal.AdapterUpdated(it)
            }

        is ChannelChatCommand.InitLoad -> channelChatRepository.getAllMessagesFromDb()
            .toObservable()
            .map { messages ->
                MessagesFromDbInitLoad(messages.isNotEmpty(), PagingData.from(messages.map {
                    convertChannelMessageFromDbToUi(it)
                }).insertSeparators { before, after ->
                    if (before == null) {
                        // we're at the beginning of the list
                        return@insertSeparators null
                    }

                    if (after == null) {
                        // we're at the end of the list
                        return@insertSeparators UiModel.SeparatorMessageItem(
                            before.topicName ?: "uf",
                            before.date
                        )
                    }
                    // check between 2 items
                    if (before.topicName != after.topicName) {
                        UiModel.SeparatorMessageItem(before.topicName ?: "ef", before.date)
                    } else {
                        // no separator
                        null
                    }
                })

            }
            .mapSuccessEvent {
                ChannelChatEvent.Internal.InitLoaded(it)
            }

        is ChannelChatCommand.InitNetwork -> channelChatRepository.loadFromNetwork()
            .mapSuccessEvent {
                ChannelChatEvent.Internal.NetworkLoaded
            }

        is ChannelChatCommand.InitTopics -> channelChatRepository.getTopicsForChannel()
            .mapEvents({ topics ->
                val topicNames = mutableListOf<String>()
                for (item in topics)
                    topicNames.add(item.name)
                ChannelChatEvent.Internal.InitTopics(topicNames)
            }, { error -> ChannelChatEvent.Internal.SomeError(error) })

        is ChannelChatCommand.ClearMessages -> channelChatRepository.clearMessagesOnExit()
            .mapSuccessEvent(ChannelChatEvent.Internal.MessagesCleared)
    }
}