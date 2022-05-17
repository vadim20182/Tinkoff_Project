package android.example.tinkoffproject.chat.common.ui

sealed class UiModel {
    data class MessageItem(
        val messageId: Int,
        val channelName: String,
        val topicName: String? = null,
        val userId: Int,
        val name: String,
        val avatarUrl: String? = null,
        val messageText: String,
        val reactions: MutableMap<String, Int> = mutableMapOf(),
        val selectedReactions: MutableMap<String, Boolean> = mutableMapOf(),
        val date: Long,
        val isSent: Boolean = true,
        val fileLink: String? = null,
        val isMyMessage: Boolean = false
    ) : UiModel()

    data class SeparatorMessageItem(val topic: String, val date: Long) : UiModel()

}

fun getDate(uiModel: UiModel) = when (uiModel) {
    is UiModel.MessageItem -> uiModel.date
    is UiModel.SeparatorMessageItem -> uiModel.date
}

