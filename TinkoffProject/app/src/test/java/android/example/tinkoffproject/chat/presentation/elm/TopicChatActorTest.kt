package android.example.tinkoffproject.chat.presentation.elm

import android.example.tinkoffproject.chat.topic.data.db.TopicMessagesDAO
import android.example.tinkoffproject.chat.topic.data.repository.TopicChatRepository
import android.example.tinkoffproject.chat.topic.presentation.elm.TopicChatActor
import android.example.tinkoffproject.chat.topic.presentation.elm.ChatCommand
import android.example.tinkoffproject.chat.topic.presentation.elm.ChatEvent
import android.example.tinkoffproject.stub.TopicChatRepositoryStub
import android.example.tinkoffproject.stub.TopicMessagesDAOStub
import android.example.tinkoffproject.utils.RxRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class TopicChatActorTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val rxRule = RxRule()

    @Test
    fun `command{ClearMessages} successfully completes`() {

        val spyChatRepository = spy(createChatRepository(topicMessagesDAO = TopicMessagesDAOStub()))
        val chatActor = createChatActor(spyChatRepository)

        chatActor.execute(ChatCommand.ClearMessages)
            .test()
            .assertResult(ChatEvent.Internal.MessagesCleared)
            .dispose()

        verify(spyChatRepository, times(1)).clearMessagesOnExit("default", "default")
    }

    private fun createChatRepository(
        topicMessagesDAO: TopicMessagesDAO
    ) =
        TopicChatRepositoryStub(
            topicMessagesDAO = topicMessagesDAO
        )

    private fun createChatActor(
        topicChatRepository: TopicChatRepository
    ) =
        TopicChatActor(topicChatRepository, CoroutineScope(Job()))
}