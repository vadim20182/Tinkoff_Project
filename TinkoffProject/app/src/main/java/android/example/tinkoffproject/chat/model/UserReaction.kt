package android.example.tinkoffproject.chat.model

internal class UserReaction(
    private val reactions: MutableMap<String, Int>,
    private val selectedReactions: MutableMap<String, Boolean>
) {

    fun selectReaction(emoji_name: String) {
        selectedReactions[emoji_name] = true
    }

    fun unselectReaction(emoji_name: String) {
        selectedReactions[emoji_name] = false
        reactions[emoji_name]?.let {
            reactions.put(emoji_name, it - 1)
        }
    }

    fun increaseReactionCount(emoji_name: String) {
        reactions[emoji_name]?.let {
            reactions.put(emoji_name, it + 1)
        }
    }

    fun canReactionCountBeDecreased(emoji_name: String): Boolean =
        reactions[emoji_name]?.minus(1) != 0

    fun isReactionSelected(emoji_name: String): Boolean? = selectedReactions[emoji_name]

    fun isReactionAdded(emoji_name: String): Boolean = reactions[emoji_name] != null

    fun addReaction(emoji_name: String) {
        reactions[emoji_name] = 1
    }

    fun deleteReaction(emoji_name: String) {
        reactions.remove(emoji_name)
        selectedReactions.remove(emoji_name)
    }

}