package android.example.tinkoffproject

import android.example.tinkoffproject.customviews.ReactionCustomView
import android.example.tinkoffproject.data.UserMessage
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {

    private val users: MutableLiveData<MutableList<UserMessage>> =
        MutableLiveData<MutableList<UserMessage>>()

    init {
        loadUsers()
    }

    fun addItem(userMessage: UserMessage) {
        users.value!!.add(userMessage)
    }

    fun reactionAdded(position: Int, emoji: String) {
        if (users.value!![position].selectedReactions[emoji] == true) {
            if (users.value!![position].reactions[emoji]!!.minus(1) != 0) {
                users.value!![position].reactions[emoji] =
                    users.value!![position].reactions[emoji]!!.minus(1)
                users.value!![position].selectedReactions[emoji] = false
            } else {
                users.value!![position].reactions.remove(emoji)
                users.value!![position].selectedReactions.remove(emoji)
            }
        } else {
            users.value!![position].reactions[emoji] =
                users.value!![position].reactions[emoji]!!.plus(1)
            users.value!![position].selectedReactions[emoji] = true
        }
    }

    fun reactionClicked(position: Int, emoji: String) {
        if (users.value!![position].selectedReactions[emoji] ==
            false || users.value!![position].selectedReactions[emoji] ==
            null
        ) {
            users.value!![position].selectedReactions[emoji] =
                true
            if (users.value!![position].reactions[emoji] == null)
                users.value!![position].reactions[emoji] = 1
            else
                users.value!![position].reactions[emoji] =
                    users.value!![position].reactions[emoji]!!.plus(
                        1
                    )
        }
    }

    fun getUsers(): LiveData<MutableList<UserMessage>> {
        return users
    }

    private fun loadUsers() {
        users.value = mutableListOf(
            UserMessage(0, "Username1", messageText = "Hi1! There!"),
            UserMessage(0, "Username2", messageText = "Hi2! There!"),
            UserMessage(2, "Username3", messageText = "Hi3! There!"),
            UserMessage(
                3,
                "Username4",
                R.mipmap.send_btn,
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
            UserMessage(8, "Username11", messageText = "Hi11! There!", date = "2 Feb"))
    }
}
