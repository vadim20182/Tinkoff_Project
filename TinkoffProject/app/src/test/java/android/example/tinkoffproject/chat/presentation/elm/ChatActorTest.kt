package android.example.tinkoffproject.chat.presentation.elm

import android.example.tinkoffproject.chat.data.db.MessagesDAO
import android.example.tinkoffproject.chat.data.repository.ChatRepository
import android.example.tinkoffproject.stub.ChatRepositoryStub
import android.example.tinkoffproject.stub.MessagesDAOStub
import android.example.tinkoffproject.utils.RxRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class ChatActorTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val rxRule = RxRule()

    @Test
    fun `command{ClearMessages} successfully completes`() {

        val spyChatRepository = spy(createChatRepository(messagesDAO = MessagesDAOStub()))
        val chatActor = createChatActor(spyChatRepository)

        chatActor.execute(ChatCommand.ClearMessages)
            .test()
            .assertResult(ChatEvent.Internal.MessagesCleared)
            .dispose()

        verify(spyChatRepository, times(1)).clearMessagesOnExit("default", "default")
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