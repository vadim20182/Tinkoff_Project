package android.example.tinkoffproject.chat.topic.presentation

import android.example.tinkoffproject.chat.topic.di.TopicChatComponent
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.cancel

class TopicChatViewModel : ViewModel() {
    var topicChatComponent: TopicChatComponent? = null

    override fun onCleared() {
        topicChatComponent?.getCoroutineScope()?.cancel()
        topicChatComponent?.getTopicChatRepository()?.clearMessagesOnExit()?.subscribe()
        topicChatComponent?.getTopicChatRepository()?.let {
            for (key in it.disposables.keys)
                it.disposables[key]?.dispose()
        }
    }
}