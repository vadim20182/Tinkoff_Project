package android.example.tinkoffproject.data

internal class UserReaction(
    private val reactions: MutableMap<String, Int>,
    private val selectedReactions: MutableMap<String, Boolean>
) {

    fun selectReaction(emoji: String) {
        selectedReactions[emoji] = true
    }

    fun unselectReaction(emoji: String) {
        selectedReactions[emoji] = false
    }

    fun increaseReactionCount(emoji: String) {
        reactions[emoji]?.let {
            reactions.put(emoji, it + 1)
        }
    }

    fun decreaseReactionCount(emoji: String) {
        reactions[emoji]?.let {
            reactions.put(emoji, it - 1)
        }
    }

    fun canReactionCountBeDecreased(emoji: String): Boolean = reactions[emoji]?.minus(1) != 0

    fun isReactionSelected(emoji: String): Boolean? = selectedReactions[emoji]

    fun isReactionAdded(emoji: String): Boolean = reactions[emoji] != null

    fun addReaction(emoji: String) {
        reactions[emoji] = 1
    }

    fun deleteReaction(emoji: String) {
        reactions.remove(emoji)
        selectedReactions.remove(emoji)
    }

}