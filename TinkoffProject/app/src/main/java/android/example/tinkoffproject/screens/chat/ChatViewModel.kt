package android.example.tinkoffproject.screens.chat

import android.example.tinkoffproject.R
import android.example.tinkoffproject.customviews.ReactionCustomView
import android.example.tinkoffproject.data.UserMessage
import android.example.tinkoffproject.data.UserReaction
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {

    private val _users: MutableLiveData<MutableList<UserMessage>> =
        MutableLiveData<MutableList<UserMessage>>()
    val users: LiveData<out List<UserMessage>> = _users

    private val currentMessages: MutableList<UserMessage>
        get() = _users.value ?: mutableListOf()

    init {
        loadUsers()
    }

    fun addItem(userMessage: UserMessage) {
        currentMessages.add(userMessage)
    }

    fun reactionClicked(position: Int, emoji: String) {
        val userReaction = UserReaction(
            currentMessages[position].reactions,
            currentMessages[position].selectedReactions
        )

        if (userReaction.isReactionSelected(emoji) == true) {
            if (userReaction.canReactionCountBeDecreased(emoji)) {
                userReaction.unselectReaction(emoji)
            } else
                userReaction.deleteReaction(emoji)
        } else {
            userReaction.increaseReactionCount(emoji)
            userReaction.selectReaction(emoji)
        }
    }

    fun addReaction(position: Int, emoji: String) {
        val userReaction = UserReaction(
            currentMessages[position].reactions,
            currentMessages[position].selectedReactions
        )

        if (userReaction.isReactionSelected(emoji) != true) {
            userReaction.selectReaction(emoji)
            if (userReaction.isReactionAdded(emoji)) {
                userReaction.increaseReactionCount(emoji)
            } else
                userReaction.addReaction(emoji)
        }
    }

    private fun loadUsers() {
        _users.value = mutableListOf(
            UserMessage(0, "Username1", messageText = "Hi1! There!"),
            UserMessage(0, "Username2", messageText = "Hi2! There!"),
            UserMessage(2, "Username3", messageText = "Hi3! There!"),
            UserMessage(
                3,
                "Username4",
                R.drawable.send_btn,
                "Hi4! There!Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur eu quam sed diam lobortis lacinia sit amet in tellus. Sed hendrerit nisl in tellus maximus, non sollicitudin tortor scelerisque. Vivamus vel arcu in dui iaculis accumsan vitae sed erat. Mauris viverra arcu ex, id rhoncus libero pulvinar at. Morbi sem libero, tempor nec erat vitae, tincidunt vulputate dolor.",
                mutableMapOf(Pair(ReactionCustomView.EMOJI_LIST[0], 1))
            ),
            UserMessage(
                4, "Username5", messageText = "Hi5! There!", reactions = mutableMapOf(
                    Pair(ReactionCustomView.EMOJI_LIST[0], 1),
                    Pair(ReactionCustomView.EMOJI_LIST[1], 1),
                    Pair(ReactionCustomView.EMOJI_LIST[2], 1),
                    Pair(ReactionCustomView.EMOJI_LIST[3], 1)
                )
            ),
            UserMessage(5, "Username6", messageText = "Hi6! There!"),
            UserMessage(6, "Username7", messageText = "Hi7! There!"),
            UserMessage(
                7,
                "Username8",
                messageText = "Hi8! There!",
                reactions = mutableMapOf<String, Int>().apply {
                    put(ReactionCustomView.EMOJI_LIST[0], 1)
                    put(ReactionCustomView.EMOJI_LIST[1], 5)
                }
            ),
            UserMessage(8, "Username9", messageText = "Hi9! There!", date = "1 Feb"),
            UserMessage(8, "Username10", messageText = "Hi10! There!", date = "1 Feb"),
            UserMessage(8, "Username11", messageText = "Hi11! There!", date = "2 Feb")
        )
    }
}