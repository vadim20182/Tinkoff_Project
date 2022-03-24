package android.example.tinkoffproject.chat.ui

import androidx.fragment.app.Fragment
import android.example.tinkoffproject.R
import android.example.tinkoffproject.message.customviews.FlexBoxLayout
import android.example.tinkoffproject.message.customviews.ReactionCustomView
import android.example.tinkoffproject.message.model.UserMessage
import android.example.tinkoffproject.message.ui.MessageCustomItemDecoration
import android.example.tinkoffproject.message.ui.MessageAsyncAdapter
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.random.Random


class ChatFragment : Fragment(R.layout.topic_chat_layout),
    MessageAsyncAdapter.OnItemClickedListener {

    private val viewModel: ChatViewModel by viewModels()
    private val myAdapter: MessageAsyncAdapter by lazy { return@lazy MessageAsyncAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemDecoration = MessageCustomItemDecoration(
            requireContext(),
            mutableListOf()
        )
        viewModel.users.observe(viewLifecycleOwner) {
            myAdapter.data = it
            itemDecoration.data = it
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.messages_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = myAdapter
        recyclerView.addItemDecoration(itemDecoration)

        view.findViewById<ImageView>(R.id.message_input_button).setOnClickListener {
            val messageText = view.findViewById<EditText>(R.id.send_message_text)
            if (messageText.text.trim().isNotEmpty()) {
                viewModel.addItem(
                    UserMessage(
                        MY_USER_ID,
                        "Me",
                        messageText = messageText.text.trim().toString(),
                        date = "${Random.nextInt(1, 31)} March"
                    )
                )
                recyclerView.smoothScrollToPosition(myAdapter.data.size)
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
                    myAdapter.notifyItemChanged(position)
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
                            myAdapter.notifyItemChanged(position)
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