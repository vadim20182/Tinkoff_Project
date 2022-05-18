package android.example.tinkoffproject.chat.topic.ui

import android.content.Context
import android.example.tinkoffproject.R
import android.example.tinkoffproject.changeStatusBarColor
import android.example.tinkoffproject.chat.common.ui.*
import android.example.tinkoffproject.chat.topic.di.DaggerTopicChatComponent
import android.example.tinkoffproject.chat.topic.presentation.TopicChatViewModel
import android.example.tinkoffproject.chat.topic.presentation.elm.ChatEffect
import android.example.tinkoffproject.chat.topic.presentation.elm.ChatEvent
import android.example.tinkoffproject.chat.topic.presentation.elm.ChatState
import android.example.tinkoffproject.chat.topic.presentation.elm.TopicChatStoreFactory
import android.example.tinkoffproject.getComponent
import android.example.tinkoffproject.message.customviews.MessageInputCustomViewGroup
import android.example.tinkoffproject.message.customviews.ReactionCustomView
import android.example.tinkoffproject.utils.displayErrorMessage
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toFile
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import vivid.money.elmslie.android.base.ElmFragment
import vivid.money.elmslie.core.store.Store
import javax.inject.Inject


class TopicChatFragment : ElmFragment<ChatEvent, ChatEffect, ChatState>(R.layout.topic_chat_layout),
    MessageAsyncAdapter.OnItemClickedListener {
    @Inject
    lateinit var topicChatStoreFactory: TopicChatStoreFactory

    private lateinit var shimmer: ShimmerFrameLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputViewGroup: MessageInputCustomViewGroup

    private val messagesAdapter: MessageAsyncAdapter by lazy {
        MessageAsyncAdapter(this)
    }

    override val initEvent: ChatEvent = ChatEvent.Ui.InitLoad

    private val viewModel: TopicChatViewModel by viewModels()

    override fun createStore(): Store<ChatEvent, ChatEffect, ChatState> =
        topicChatStoreFactory.provide()

    override fun render(state: ChatState) {
        state.pagingData?.let {
            messagesAdapter.submitData(lifecycle, it)
        }
        if (state.isLoading) {
            shimmer.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            shimmer.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    override fun handleEffect(effect: ChatEffect) {
        when (effect) {
            is ChatEffect.MessagePlaceholderIsSent -> {
                recyclerView.scrollToPosition(0)
            }
            is ChatEffect.TopicsLoaded -> {
                topicChatStoreFactory.topics = effect.topics
            }
            is ChatEffect.SomeError -> {
                Snackbar.make(
                    inputViewGroup,
                    displayErrorMessage(effect.error),
                    Snackbar.LENGTH_SHORT
                ).apply {
                    anchorView = inputViewGroup
                    setTextColor(Color.WHITE)
                    setBackgroundTint(Color.RED)
                }.show()
            }
        }
    }

    private val pickFile =
        registerForActivityResult(OpenDoc()) { uri: Uri? ->
            if (uri != null) {
                val tempUri = uri.buildUpon().scheme("file").build()
                val f = tempUri.toFile()
                val requestFile = requireContext().contentResolver.openInputStream(uri).use {
                    it?.readBytes()
                        ?.toRequestBody(context?.contentResolver?.getType(uri)?.toMediaTypeOrNull())
                }
                val body =
                    MultipartBody.Part.createFormData("file", f.name, requestFile!!)
                store.accept(ChatEvent.Ui.UploadFile(f.name, body))
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (viewModel.topicChatComponent == null) {
            viewModel.topicChatComponent = DaggerTopicChatComponent.factory().create(
                requireStringArgs(ARG_CHANNEL_NAME, this),
                requireStringArgs(ARG_TOPIC_NAME, this),
                this.requireActivity().getComponent()
            )
        }
        viewModel.topicChatComponent?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.topic_chat_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.requireActivity().changeStatusBarColor(
            resources.getColor(R.color.default_green_color, null)
        )
        messagesAdapter.addOnPagesUpdatedListener {
            store.accept(ChatEvent.Ui.AdapterUpdated)
        }

        val itemDecoration = ChatCustomItemDecoration(
            view.context
        )
        recyclerView = view.findViewById(R.id.messages_recycler_view)
        shimmer = view.findViewById(R.id.shimmer_chat)
        inputViewGroup =
            view.findViewById(R.id.message_input_custom_view_group)
        val inputButton: ShapeableImageView = view.findViewById(R.id.message_input_button)

        with(recyclerView) {
            itemAnimator = null
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = false
                reverseLayout = true
            }
            adapter = messagesAdapter
            addItemDecoration(itemDecoration)
        }

        inputButton.setOnClickListener {
            val messageText = view.findViewById<EditText>(R.id.send_message_text)
            if (messageText.text.trim().isNotEmpty()) {
                store.accept(ChatEvent.Ui.SendMessage(messageText.text.trim().toString()))
                messageText.text.clear()
            } else {
                pickFile.launch(arrayOf("*/*"))
            }
        }

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        NavigationUI.setupWithNavController(
            toolbar,
            findNavController()
        )
        toolbar.title =
            getString(R.string.toolbar_chat_title, arguments?.getString(ARG_CHANNEL_NAME))
        view.findViewById<TextView>(R.id.chat_topic_name).text =
            getString(R.string.topic_name, arguments?.getString(ARG_TOPIC_NAME))
    }

    override fun onItemClicked(view: View, message: UiModel.MessageItem) {
        when (view) {
            is ReactionCustomView -> {
                if (!view.isButton && !view.isSimpleEmoji) {
                    store.accept(
                        ChatEvent.Ui.ClickReaction(
                            view.pair.first,
                            message.messageId
                        )
                    )
                } else if (view.isButton) {
                    showReactionBottomSheetDialog(message.messageId, this)
                }
            }
            is TextView -> {
                showMessageBottomSheetDialog(
                    message,
                    this,
                    topicChatStoreFactory.topics?.toTypedArray() ?: emptyArray()
                )
            }
        }
    }

    override fun onLinkClicked(link: String) {
        downloadFile(link, requireContext())
    }

    fun editMessage(newMessage: String, messageId: Int) {
        store.accept(ChatEvent.Ui.EditMessage(newMessage, messageId))
    }

    fun deleteMessage(messageId: Int) {
        store.accept(ChatEvent.Ui.DeleteMessage(messageId))
    }

    fun changeTopicForMessage(topic: String, messageId: Int) {
        store.accept(ChatEvent.Ui.ChangeTopic(topic, messageId))
    }

    companion object {
        const val ARG_CHANNEL_NAME = "channel_name"
        const val ARG_TOPIC_NAME = "topic_name"
    }
}