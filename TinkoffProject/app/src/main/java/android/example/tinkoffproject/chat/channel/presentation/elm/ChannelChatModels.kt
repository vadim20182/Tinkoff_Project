package android.example.tinkoffproject.chat.channel.presentation.elm

import android.example.tinkoffproject.chat.common.ui.UiModel
import android.example.tinkoffproject.utils.MessagesFromDbInitLoad
import androidx.paging.PagingData
import okhttp3.MultipartBody

data class ChannelChatState(
    val isLoading: Boolean = false,
    val pagingData: PagingData<UiModel>? = null
)

sealed class ChannelChatEffect {

    object MessagePlaceholderIsSent : ChannelChatEffect()

    data class TopicsLoaded(val topics: List<String>) : ChannelChatEffect()

    data class SomeError(val error: Throwable) : ChannelChatEffect()
}

sealed class ChannelChatCommand {
    data class SendMessage(val messageText: String, val topic: String) : ChannelChatCommand()

    data class SendMessagePlaceholder(val messageText: String, val topic: String) :
        ChannelChatCommand()

    data class EditMessage(val messageText: String, val messageId: Int) : ChannelChatCommand()

    data class ChangeTopic(val topic: String, val messageId: Int) : ChannelChatCommand()

    data class DeleteMessage(val messageId: Int) : ChannelChatCommand()

    data class UploadFile(
        val fileName: String, val file: MultipartBody.Part, val topic: String
    ) : ChannelChatCommand()

    object InitLoad : ChannelChatCommand()

    object InitNetwork : ChannelChatCommand()

    object InitAdapter : ChannelChatCommand()

    object InitTopics : ChannelChatCommand()

    object ClearMessages : ChannelChatCommand()

    data class ClickReaction(
        val emoji_name: String,
        val messageId: Int
    ) : ChannelChatCommand()

    data class AddReaction(
        val emoji_name: String, val messageId: Int
    ) : ChannelChatCommand()
}

sealed class ChannelChatEvent {
    sealed class Internal : ChannelChatEvent() {
        object MessageIsSent : Internal()

        object MessagePlaceholderIsSent : Internal()

        data class InitLoaded(val result: MessagesFromDbInitLoad) : Internal()

        object NetworkLoaded : Internal()

        object MessageIsDeleted : Internal()

        object MessageIsMoved : Internal()

        data class InitTopics(val topics: List<String>) : Internal()

        object MessagesCleared : Internal()

        data class AdapterUpdated(val pagingData: PagingData<UiModel>) :
            Internal()

        object MessageUpdated : Internal()

        data class SomeError(val error: Throwable) : Internal()
    }

    sealed class Ui : ChannelChatEvent() {
        object InitLoad : Ui()

        object AdapterUpdated : Ui()

        data class AddReaction(
            val emoji_name: String, val messageId: Int
        ) : Ui()

        data class ClickReaction(
            val emoji_name: String, val messageId: Int
        ) : Ui()

        data class SendMessage(val messageText: String, val topic: String) : Ui()

        data class EditMessage(val messageText: String, val messageId: Int) : Ui()

        data class ChangeTopic(val topic: String, val messageId: Int) : Ui()

        data class DeleteMessage(val messageId: Int) : Ui()

        data class UploadFile(
            val fileName: String,
            val file: MultipartBody.Part,
            val topic: String
        ) : Ui()
    }
}