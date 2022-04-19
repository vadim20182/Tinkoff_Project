package android.example.tinkoffproject.chat.presentation

import android.example.tinkoffproject.chat.data.ChatRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChatViewModelFactory(
    private val stream: String,
    private val topic: String,
    private val repository: ChatRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java))
            return ChatViewModel(stream, topic, repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}