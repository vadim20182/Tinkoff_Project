package android.example.tinkoffproject.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.ExperimentalPagingApi
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class ChatViewModelFactory(
    private val stream: String,
    private val topic: String,
    private val repository: MessagesRxRemoteRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java))
            return ChatViewModel(stream, topic, repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}