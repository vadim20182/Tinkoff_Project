package android.example.tinkoffproject.chat.channel.presentation

import android.example.tinkoffproject.chat.channel.di.ChannelChatComponent
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.cancel

class ChannelChatViewModel : ViewModel() {
    var channelChatComponent: ChannelChatComponent? = null

    override fun onCleared() {
        channelChatComponent?.getCoroutineScope()?.cancel()
        channelChatComponent?.getChannelChatRepository()?.clearMessagesOnExit()?.subscribe()
        channelChatComponent?.getChannelChatRepository()?.let {
            for (key in it.disposables.keys)
                it.disposables[key]?.dispose()
        }
    }
}