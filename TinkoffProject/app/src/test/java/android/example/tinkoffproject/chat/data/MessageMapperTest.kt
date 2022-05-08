package android.example.tinkoffproject.chat.data

import android.example.tinkoffproject.chat.data.network.UserMessage
import android.example.tinkoffproject.network.NetworkCommon
import android.example.tinkoffproject.utils.convertMessageFromNetworkToDb
import org.junit.Test
import org.junit.Assert.*

class MessageMapperTest {
    @Test
    fun `networkToDb by default return messageEntity`() {
        val networkMessage = createUserMessage()
        val messageDb = convertMessageFromNetworkToDb(networkMessage, "default", "default")

        assertEquals(1, messageDb.userId)
        assertEquals("Ivan", messageDb.name)
        assertEquals("Hello", messageDb.messageText)
        assertEquals(0L, messageDb.date)
        assertEquals(1, messageDb.messageId)
    }

    @Test
    fun `networkToDb for userId MY_USER_ID returns isMyMessage true`() {
        val networkMessage = createUserMessage(userId = NetworkCommon.MY_USER_ID)
        val messageDb = convertMessageFromNetworkToDb(networkMessage, "default", "default")

        assertEquals(true, messageDb.isMyMessage)
    }

    @Test
    fun `networkToDb for 2 different reactions returns map of reactions size 2`() {
        val networkMessage =
            createUserMessage(reactions = mutableMapOf(Pair("grinning", 1), Pair("smiley", 2)))
        val messageDb = convertMessageFromNetworkToDb(networkMessage, "default", "default")

        assertEquals(2, messageDb.reactions.size)
    }

    @Test
    fun `networkToDb by default returns empty map of reactions`() {
        val networkMessage = createUserMessage()
        val messageDb = convertMessageFromNetworkToDb(networkMessage, "default", "default")

        assertEquals(true, messageDb.reactions.isEmpty())
    }

    private fun createUserMessage(
        userId: Int = 1,
        name: String = "Ivan",
        messageText: String = "Hello",
        date: Long = 0L,
        messageId: Int = 1,
        reactions: MutableMap<String, Int> = mutableMapOf()
    ) = UserMessage(
        userId,
        name,
        messageText = messageText,
        date = date,
        messageId = messageId,
        reactions = reactions
    )
}