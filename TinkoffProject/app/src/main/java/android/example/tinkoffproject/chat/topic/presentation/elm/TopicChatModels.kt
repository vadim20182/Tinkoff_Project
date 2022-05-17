package android.example.tinkoffproject.chat.topic.presentation.elm

import android.example.tinkoffproject.chat.common.ui.UiModel
import android.example.tinkoffproject.utils.MessagesFromDbInitLoad
import androidx.paging.PagingData
import okhttp3.MultipartBody

data class ChatState(
    val isLoading: Boolean = false,
    val pagingData: PagingData<UiModel>? = null
)

sealed class ChatEffect {

    object MessagePlaceholderIsSent : ChatEffect()

    data class SomeError(val error: Throwable) : ChatEffect()
}

sealed class ChatCommand {
    data class SendMessage(val messageText: String) : ChatCommand()

    data class SendMessagePlaceholder(val messageText: String) : ChatCommand()

    data class EditMessage(val messageText: String, val messageId: Int) : ChatCommand()

    data class ChangeTopic(val topic: String, val messageId: Int) : ChatCommand()

    data class DeleteMessage(val messageId: Int) : ChatCommand()

    data class UploadFile(
        val fileName: String, val file: MultipartBody.Part
    ) : ChatCommand()

    object InitLoad : ChatCommand()

    object InitNetwork : ChatCommand()

    object InitAdapter : ChatCommand()

    object ClearMessages : ChatCommand()

    data class ClickReaction(
        val emoji_name: String,
        val messageId: Int
    ) : ChatCommand()

    data class AddReaction(
        val emoji_name: String, val messageId: Int
    ) : ChatCommand()
}

sealed class ChatEvent {
    sealed class Internal : ChatEvent() {
        object MessageIsSent : Internal()

        object MessagePlaceholderIsSent : Internal()

        data class InitLoaded(val result: MessagesFromDbInitLoad) : Internal()

        object NetworkLoaded : Internal()

        object MessageIsDeleted : Internal()

        object MessageIsMoved : Internal()

        object MessagesCleared : Internal()

        data class AdapterUpdated(val pagingData: PagingData<UiModel>) :
            Internal()

        object MessageUpdated : Internal()

        data class SomeError(val error: Throwable) : Internal()
    }

    sealed class Ui : ChatEvent() {
        object InitLoad : Ui()

        object AdapterUpdated : Ui()

        data class AddReaction(
            val emoji_name: String, val messageId: Int
        ) : Ui()

        data class ClickReaction(
            val emoji_name: String, val messageId: Int
        ) : Ui()

        data class SendMessage(val messageText: String) : Ui()

        data class EditMessage(val messageText: String, val messageId: Int) : Ui()

        data class ChangeTopic(val topic: String, val messageId: Int) : Ui()

        data class DeleteMessage(val messageId: Int) : Ui()

        data class UploadFile(val fileName: String, val file: MultipartBody.Part) : Ui()
    }
}