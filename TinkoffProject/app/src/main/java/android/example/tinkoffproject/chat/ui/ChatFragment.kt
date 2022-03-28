package android.example.tinkoffproject.chat.ui

import androidx.fragment.app.Fragment
import android.example.tinkoffproject.R
import android.example.tinkoffproject.message.customviews.FlexBoxLayout
import android.example.tinkoffproject.message.customviews.MessageInputCustomViewGroup
import android.example.tinkoffproject.message.customviews.ReactionCustomView
import android.example.tinkoffproject.message.model.UserMessage
import android.example.tinkoffproject.message.ui.MessageCustomItemDecoration
import android.example.tinkoffproject.message.ui.MessageAsyncAdapter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.core.view.isEmpty
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random


class ChatFragment : Fragment(R.layout.topic_chat_layout),
    MessageAsyncAdapter.OnItemClickedListener {

    private val viewModel: ChatViewModel by viewModels()
    private val myAdapter: MessageAsyncAdapter by lazy { MessageAsyncAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemDecoration = MessageCustomItemDecoration(
            requireContext(),
            mutableListOf()
        )
        val recyclerView = view.findViewById<RecyclerView>(R.id.messages_recycler_view)
        val shimmer = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_chat)
        val inputViewGroup =
            view.findViewById<MessageInputCustomViewGroup>(R.id.message_input_custom_view_group)
        val inputButton: ShapeableImageView = view.findViewById(R.id.message_input_button)

        with(viewModel) {
            this.isAsyncTaskCompleted.observe(viewLifecycleOwner) { isAsyncTaskCompleted ->
                if (isAsyncTaskCompleted) {
                    inputButton.visibility = View.VISIBLE
                    shimmer.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                } else {
                    inputButton.visibility = View.GONE
                    shimmer.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            }
            this.topicMessages.observe(viewLifecycleOwner) {
                itemDecoration.data = it
                myAdapter.data = it
            }
            this.itemToUpdate.observe(viewLifecycleOwner) {
                myAdapter.notifyItemChanged(it)
            }
            this.messagesCount.observe(viewLifecycleOwner) {
                recyclerView.smoothScrollToPosition(myAdapter.data.size)
            }
            this.errorMessage.observe(viewLifecycleOwner) {
                if (it != "") {
                    Snackbar.make(
                        inputViewGroup,
                        it,
                        Snackbar.LENGTH_SHORT
                    ).apply {
                        anchorView = inputViewGroup
                        setTextColor(Color.WHITE)
                        setBackgroundTint(Color.RED)
                    }.show()
                    this.resetErrorMessage()
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = myAdapter
        recyclerView.addItemDecoration(itemDecoration)

        inputButton.setOnClickListener {
            val messageText = view.findViewById<EditText>(R.id.send_message_text)
            if (messageText.text.trim().isNotEmpty()) {
                viewModel.sendMessage(
                    UserMessage(
                        MY_USER_ID,
                        "Me",
                        messageText = messageText.text.trim().toString(),
                        date = "${Random.nextInt(1, 31)} March"
                    )
                )
                messageText.text.clear()
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
                    viewModel.reactionClicked(position, view.getEmoji())
                } else if (view.isButton) {
                    showBottomSheetDialog(position)
                }
            }
            is TextView -> {
                showBottomSheetDialog(position)
            }
        }
    }

    private fun showBottomSheetDialog(position: Int) {
        object : BottomSheetDialog(requireContext()) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                val bottomSheetFlexBoxLayout =
                    findViewById<FlexBoxLayout>(R.id.bottom_sheet_emojis)
                if (bottomSheetFlexBoxLayout?.isEmpty() == true) {
                    for (emoji in ReactionCustomView.EMOJI_LIST)
                        bottomSheetFlexBoxLayout.addView((LayoutInflater.from(context)
                            .inflate(
                                R.layout.emoji_item,
                                null
                            ) as ReactionCustomView).apply {
                            isSimpleEmoji = true
                            setEmoji(emoji)
                        })
                    for (child in bottomSheetFlexBoxLayout.children)
                        child.setOnClickListener {
                            with(child as ReactionCustomView) {
                                viewModel.addReaction(position, this.getEmoji())
                            }
                            cancel()
                        }
                }
            }
        }.apply {
            setContentView(R.layout.reactions_bottom_sheet_dialog_layout)
        }.show()
    }

    companion object {
        const val MY_USER_ID = 8L
        const val ARG_CHANNEL_NAME = "channel_name"
        const val ARG_TOPIC_NAME = "topic_name"
    }
}