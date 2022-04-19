package android.example.tinkoffproject.chat.presentation.elm

import okhttp3.MultipartBody


data class ChatState(
    val isLoading: Boolean = false
)

sealed class ChatEffect {
    object MessagePlaceholderIsSent : ChatEffect()

    object MessageIsSent : ChatEffect()

    data class MessageReactionUpdated(val posToUpdate: Int) : ChatEffect()

    data class SomeError(val error: Throwable) : ChatEffect()
}

sealed class ChatCommand {
    data class SendMessage(val messageText: String) : ChatCommand()

    data class SendMessagePlaceholder(val messageText: String) : ChatCommand()

    data class UploadFile(
        val fileName: String, val file: MultipartBody.Part
    ) : ChatCommand()

    object InitLoad : ChatCommand()

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

        object InitLoaded : Internal()

        object InitLoading : Internal()

        data class MessageUpdated(val posToUpdate: Int) : Internal()

        data class SomeError(val error: Throwable) : Internal()
    }

    sealed class Ui : ChatEvent() {
        object InitLoad : Ui()

        data class AddReaction(
            val emoji_name: String, val messageId: Int
        ) : Ui()

        data class ClickReaction(
            val emoji_name: String, val messageId: Int
        ) : Ui()

        data class SendMessage(val messageText: String) : Ui()

        data class UploadFile(val fileName: String, val file: MultipartBody.Part) : Ui()
    }
}