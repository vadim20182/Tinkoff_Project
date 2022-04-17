package android.example.tinkoffproject.chat.ui

import android.app.Activity
import android.app.DownloadManager
import android.app.DownloadManager.Request.*
import android.content.Context
import android.content.Intent
import android.example.tinkoffproject.R
import android.example.tinkoffproject.chat.model.ChatRepository
import android.example.tinkoffproject.chat.model.db.MessagesRemoteMediator
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
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody


@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class ChatFragment : Fragment(R.layout.topic_chat_layout),
    MessageAsyncAdapter.OnItemClickedListener {

    private val compositeDatabase = CompositeDisposable()

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(
            requireArguments().getString(
                ARG_CHANNEL_NAME
            )!!, requireArguments().getString(ARG_TOPIC_NAME)!!, ChatRepository(
                AppDatabase.getInstance(requireContext()).messagesDAO(),
                MessagesRemoteMediator(
                    AppDatabase.getInstance(requireContext()), requireArguments().getString(
                        ARG_CHANNEL_NAME
                    )!!, requireArguments().getString(ARG_TOPIC_NAME)!!
                )
            )
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
                val requestFile: RequestBody = f
                    .asRequestBody(context?.contentResolver?.getType(tempUri)?.toMediaTypeOrNull())
                val body =
                    MultipartBody.Part.createFormData("file", f.name, requestFile)
                viewModel.uploadFile(f.name, body)
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemDecoration = MessageCustomItemDecoration(
            view.context
        )
        val recyclerView = view.findViewById<RecyclerView>(R.id.messages_recycler_view)
        val shimmer = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_chat)
        val inputViewGroup =
            view.findViewById<MessageInputCustomViewGroup>(R.id.message_input_custom_view_group)
        val inputButton: ShapeableImageView = view.findViewById(R.id.message_input_button)

        with(viewModel) {
            uiState.observe(viewLifecycleOwner) { state ->
                if (state.isLoading) {
                    inputButton.visibility = View.GONE
                    shimmer.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    inputButton.visibility = View.VISIBLE
                    shimmer.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
            reactionUpdateMessageId.observe(viewLifecycleOwner) {
                MessagesRemoteMediator.MESSAGE_ANCHOR_TO_UPDATE = viewModel.posToUpdate
                messagesAdapter.refresh()
            }
            getMessages().subscribe {
                messagesAdapter.submitData(lifecycle, it)
            }.addTo(compositeDatabase)
            messageSent.observe(viewLifecycleOwner) { messageSent ->
                if (messageSent == null) {
                    MessagesRemoteMediator.MESSAGE_ANCHOR_TO_UPDATE =
                        MessagesRemoteMediator.NEWEST_MESSAGE
                    messagesAdapter.refresh()
                    setMessageSent(true)
                }
                if (messagePlaceholderSent == null) {
                    recyclerView.scrollToPosition(0)
                    messagePlaceholderSent = false
                }
            }
            errorMessage.observe(viewLifecycleOwner) {
                Snackbar.make(
                    inputViewGroup,
                    it,
                    Snackbar.LENGTH_SHORT
                ).apply {
                    anchorView = inputViewGroup
                    setTextColor(Color.WHITE)
                    setBackgroundTint(Color.RED)
                }.show()
            }
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
                viewModel.sendMessage(messageText.text.trim().toString())
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

    override fun onItemClicked(position: Int, view: View) {
        when (view) {
            is ReactionCustomView -> {
                if (!view.isButton && !view.isSimpleEmoji) {
                    viewModel.reactionClicked(position, view.pair.first)
                } else if (view.isButton) {
                    showBottomSheetDialog(position)
                }
            }
            is TextView -> {
                showBottomSheetDialog(position)
            }
        }
    }

    override fun onLinkClicked(link: String) {
        downloadFile(link)
    }

    private fun showBottomSheetDialog(position: Int) {
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
                                viewModel.addReaction(position, this.pair.first)
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