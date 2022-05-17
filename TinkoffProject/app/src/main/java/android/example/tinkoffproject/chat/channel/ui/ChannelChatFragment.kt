package android.example.tinkoffproject.chat.channel.ui

import android.content.Context
import android.example.tinkoffproject.R
import android.example.tinkoffproject.changeStatusBarColor
import android.example.tinkoffproject.chat.channel.di.DaggerChannelChatComponent
import android.example.tinkoffproject.chat.channel.presentation.ChannelChatViewModel
import android.example.tinkoffproject.chat.channel.presentation.elm.ChannelChatEffect
import android.example.tinkoffproject.chat.channel.presentation.elm.ChannelChatEvent
import android.example.tinkoffproject.chat.channel.presentation.elm.ChannelChatState
import android.example.tinkoffproject.chat.channel.presentation.elm.ChannelChatStoreFactory
import android.example.tinkoffproject.chat.common.ui.*
import android.example.tinkoffproject.chat.topic.ui.TopicChatFragment
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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
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


class ChannelChatFragment : ElmFragment<ChannelChatEvent, ChannelChatEffect, ChannelChatState>(),
    ChannelMessagesAdapter.OnItemClickedListener {

    @Inject
    lateinit var channelChatStoreFactory: ChannelChatStoreFactory

    private lateinit var shimmer: ShimmerFrameLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputViewGroup: MessageInputCustomViewGroup
    private lateinit var inputTopic: AutoCompleteTextView

    private val messagesAdapter: ChannelMessagesAdapter by lazy {
        ChannelMessagesAdapter(this)
    }

    private val navController: NavController by lazy {
        findNavController()
    }

    override val initEvent: ChannelChatEvent = ChannelChatEvent.Ui.InitLoad

    private val viewModel: ChannelChatViewModel by viewModels()

    override fun createStore(): Store<ChannelChatEvent, ChannelChatEffect, ChannelChatState> =
        channelChatStoreFactory.provide()

    override fun render(state: ChannelChatState) {
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

    override fun handleEffect(effect: ChannelChatEffect) {
        when (effect) {
            is ChannelChatEffect.MessagePlaceholderIsSent -> {
                recyclerView.scrollToPosition(0)
            }
            is ChannelChatEffect.TopicsLoaded -> {
                channelChatStoreFactory.topics = effect.topics
                inputTopic.setAdapter(
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line, effect.topics
                    )
                )
            }
            is ChannelChatEffect.SomeError -> {
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
                store.accept(
                    ChannelChatEvent.Ui.UploadFile(
                        f.name,
                        body,
                        inputTopic.text.trim().toString().ifBlank { "(no topic)" }
                    )
                )
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (viewModel.channelChatComponent == null) {
            viewModel.channelChatComponent = DaggerChannelChatComponent.factory().create(
                requireStringArgs(ARG_CHANNEL_NAME, this),
                requireIntArgs(ARG_CHANNEL_ID, this),
                this.requireActivity().getComponent()
            )
        }
        viewModel.channelChatComponent?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.channel_chat_layout, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.requireActivity().changeStatusBarColor(
            resources.getColor(R.color.default_green_color, null)
        )
        messagesAdapter.addOnPagesUpdatedListener {
            store.accept(ChannelChatEvent.Ui.AdapterUpdated)
        }

        val itemDecoration = ChatCustomItemDecoration(
            view.context
        )
        recyclerView = view.findViewById(R.id.messages_recycler_view)
        shimmer = view.findViewById(R.id.shimmer_chat)
        inputViewGroup =
            view.findViewById(R.id.message_input_custom_view_group)
        val inputButton: ShapeableImageView = view.findViewById(R.id.message_input_button)

        inputTopic = view.findViewById(R.id.topic_input)
        channelChatStoreFactory.topics?.let {
            inputTopic.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line, it
                )
            )
        }
        inputTopic.threshold = 1

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
                store.accept(
                    ChannelChatEvent.Ui.SendMessage(
                        messageText.text.trim().toString(),
                        inputTopic.text.trim().toString().ifBlank { "(no topic)" })
                )
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
    }

    override fun onItemClicked(view: View, message: UiModel.MessageItem) {
        when (view) {
            is ReactionCustomView -> {
                if (!view.isButton && !view.isSimpleEmoji) {
                    store.accept(
                        ChannelChatEvent.Ui.ClickReaction(
                            view.pair.first,
                            message.messageId
                        )
                    )
                } else if (view.isButton) {
                    showReactionBottomSheetDialog(message.messageId, this)
                }
            }
            is TextView -> {
                showMessageBottomSheetDialog(message, this)
            }
        }
    }

    override fun onLinkClicked(link: String) {
        downloadFile(link, requireContext())
    }

    override fun onSeparatorClicked(topic: String) {
        val bundle =
            bundleOf(
                TopicChatFragment.ARG_CHANNEL_NAME to requireStringArgs(ARG_CHANNEL_NAME, this),
                TopicChatFragment.ARG_TOPIC_NAME to topic
            )
        navController.navigate(R.id.action_channelChatFragment_to_chatFragment, bundle)
    }

    fun editMessage(newMessage: String, messageId: Int) {
        store.accept(ChannelChatEvent.Ui.EditMessage(newMessage, messageId))
    }

    fun deleteMessage(messageId: Int) {
        store.accept(ChannelChatEvent.Ui.DeleteMessage(messageId))
    }

    fun changeTopicForMessage(topic: String, messageId: Int) {
        store.accept(ChannelChatEvent.Ui.ChangeTopic(topic, messageId))
    }

    companion object {
        const val ARG_CHANNEL_NAME = "channel_name"
        const val ARG_CHANNEL_ID = "channel_id"
    }
}