package android.example.tinkoffproject.chat.ui

import android.app.Activity
import android.app.DownloadManager
import android.app.DownloadManager.Request.*
import android.content.Context
import android.content.Intent
import android.example.tinkoffproject.R
import android.example.tinkoffproject.chat.data.ChatRepository
import android.example.tinkoffproject.chat.data.db.MessagesRemoteMediator
import android.example.tinkoffproject.chat.presentation.ChatViewModel
import android.example.tinkoffproject.chat.presentation.ChatViewModelFactory
import android.example.tinkoffproject.chat.presentation.elm.*
import android.example.tinkoffproject.database.AppDatabase
import android.example.tinkoffproject.message.customviews.FlexBoxLayout
import android.example.tinkoffproject.message.customviews.MessageInputCustomViewGroup
import android.example.tinkoffproject.message.customviews.ReactionCustomView
import android.example.tinkoffproject.network.NetworkClient
import android.example.tinkoffproject.utils.EMOJI_MAP
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toFile
import androidx.core.view.children
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import vivid.money.elmslie.android.base.ElmFragment
import vivid.money.elmslie.core.store.Store


class ChatFragment :
    ElmFragment<ChatEvent, ChatEffect, ChatState>(R.layout.topic_chat_layout),
    MessageAsyncAdapter.OnItemClickedListener {

    private val chatRepository by lazy {
        ChatRepository(
            AppDatabase.getInstance(requireContext()).messagesDAO(),
            MessagesRemoteMediator(
                AppDatabase.getInstance(requireContext()), requireArguments().getString(
                    ARG_CHANNEL_NAME
                )!!, requireArguments().getString(ARG_TOPIC_NAME)!!
            ), requireArguments().getString(
                ARG_CHANNEL_NAME
            )!!, requireArguments().getString(ARG_TOPIC_NAME)!!
        )
    }

    private lateinit var shimmer: ShimmerFrameLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputViewGroup: MessageInputCustomViewGroup

    override val initEvent: ChatEvent = ChatEvent.Ui.InitLoad

    override fun createStore(): Store<ChatEvent, ChatEffect, ChatState> =
        ChatStoreFactory(ChatActor(chatRepository)).provide()

    override fun render(state: ChatState) {
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
            is ChatEffect.MessageIsSent -> {
                MessagesRemoteMediator.MESSAGE_ANCHOR_TO_UPDATE =
                    MessagesRemoteMediator.NEWEST_MESSAGE
                messagesAdapter.refresh()
            }
            is ChatEffect.MessageReactionUpdated -> {
                MessagesRemoteMediator.MESSAGE_ANCHOR_TO_UPDATE = effect.posToUpdate
                messagesAdapter.refresh()
            }
            is ChatEffect.SomeError -> {
                Snackbar.make(
                    inputViewGroup,
                    effect.error.localizedMessage,
                    Snackbar.LENGTH_SHORT
                ).apply {
                    anchorView = inputViewGroup
                    setTextColor(Color.WHITE)
                    setBackgroundTint(Color.RED)
                }.show()
            }
        }
    }

    private val compositeDatabase = CompositeDisposable()

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(
            requireArguments().getString(
                ARG_CHANNEL_NAME
            )!!,
            requireArguments().getString(ARG_TOPIC_NAME)!!,
            chatRepository
        )
    }
    private val messagesAdapter: MessageAsyncAdapter by lazy {
        MessageAsyncAdapter(this)
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemDecoration = MessageCustomItemDecoration(
            view.context
        )
        recyclerView = view.findViewById(R.id.messages_recycler_view)
        shimmer = view.findViewById(R.id.shimmer_chat)
        inputViewGroup =
            view.findViewById(R.id.message_input_custom_view_group)
        val inputButton: ShapeableImageView = view.findViewById(R.id.message_input_button)

        with(viewModel) {
            getMessages().subscribe {
                messagesAdapter.submitData(lifecycle, it)
            }.addTo(compositeDatabase)
        }
        recyclerView.itemAnimator = null
        recyclerView.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = false
            reverseLayout = true
        }
        recyclerView.adapter = messagesAdapter
        recyclerView.addItemDecoration(itemDecoration)

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
        toolbar.title = arguments?.getString(ARG_CHANNEL_NAME)
        view.findViewById<TextView>(R.id.chat_topic_name).text =
            getString(R.string.topic_name, arguments?.getString(ARG_TOPIC_NAME))
    }

    override fun onItemClicked(view: View, messageId: Int) {
        when (view) {
            is ReactionCustomView -> {
                if (!view.isButton && !view.isSimpleEmoji) {
                    store.accept(
                        ChatEvent.Ui.ClickReaction(
                            view.pair.first,
                            messageId
                        )
                    )
                } else if (view.isButton) {
                    showBottomSheetDialog(messageId)
                }
            }
            is TextView -> {
                showBottomSheetDialog(messageId)
            }
        }
    }

    override fun onLinkClicked(link: String) {
        downloadFile(link)
    }

    private fun showBottomSheetDialog(messageId: Int) {
        object : BottomSheetDialog(requireContext()) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                val bottomSheetFlexBoxLayout =
                    findViewById<FlexBoxLayout>(R.id.bottom_sheet_emojis)
                if (bottomSheetFlexBoxLayout?.isEmpty() == true) {
                    for (key in EMOJI_MAP.keys)
                        bottomSheetFlexBoxLayout.addView((LayoutInflater.from(context)
                            .inflate(
                                R.layout.emoji_item,
                                null
                            ) as ReactionCustomView).apply {
                            isSimpleEmoji = true
                            setEmojiNameAndCode(key, EMOJI_MAP[key]!!)
                        })
                    for (child in bottomSheetFlexBoxLayout.children)
                        child.setOnClickListener {
                            with(child as ReactionCustomView) {
                                store.accept(
                                    ChatEvent.Ui.AddReaction(
                                        this.pair.first,
                                        messageId
                                    )
                                )
                            }
                            cancel()
                        }
                }
            }
        }.apply {
            setContentView(R.layout.reactions_bottom_sheet_dialog_layout)
        }.show()
    }

    override fun onDestroyView() {
        compositeDatabase.clear()
        super.onDestroyView()
    }

    private fun downloadFile(link: String) {
        val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE)
                as DownloadManager
        val request = DownloadManager.Request(Uri.parse(link))
            .setAllowedOverMetered(true)
            .addRequestHeader(
                "Authorization",
                Credentials.basic(NetworkClient.EMAIL, NetworkClient.API_KEY)
            )
            .setDescription("Downloading")
            .setTitle("Zulip file")
            .setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, link.substringAfterLast("/"))
            .setNotificationVisibility(VISIBILITY_VISIBLE)
            .setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        downloadManager.enqueue(request)
    }

    companion object {
        const val ARG_CHANNEL_NAME = "channel_name"
        const val ARG_TOPIC_NAME = "topic_name"
    }
}

private class OpenDoc : ActivityResultContract<Array<String>, Uri>() {
    override fun createIntent(context: Context, input: Array<String>?): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .putExtra(Intent.EXTRA_MIME_TYPES, input)
            .setType("*/*");

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
    }
}