package android.example.tinkoffproject.chat.presentation.elm

import android.example.tinkoffproject.chat.data.db.MessagesDAO
import android.example.tinkoffproject.chat.data.repository.ChatRepository
import android.example.tinkoffproject.stub.ChatRepositoryStub
import android.example.tinkoffproject.stub.MessagesDAOStub
import android.example.tinkoffproject.utils.RxRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.junit.Test

class ChatActorTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val rxRule = RxRule()

    @Test
    fun `command{ClearMessages} successfully completes`() {
        val messagesDAOStub = MessagesDAOStub()

        val chatRepository = createChatRepository(messagesDAO = messagesDAOStub)
        val chatActor = createChatActor(chatRepository)

        chatActor.execute(ChatCommand.ClearMessages)
            .test()
            .assertResult(ChatEvent.Internal.MessagesCleared)
            .dispose()
    }

    private fun createChatRepository(
        messagesDAO: MessagesDAO
    ) =
        ChatRepositoryStub(
            messagesDAO = messagesDAO
        )

    private fun createChatActor(
        chatRepository: ChatRepository
    ) =
        ChatActor(chatRepository)
}