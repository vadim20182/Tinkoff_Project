package android.example.tinkoffproject.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChatViewModelFactory(private val stream: String, private val topic: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java))
            return ChatViewModel(stream, topic) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}