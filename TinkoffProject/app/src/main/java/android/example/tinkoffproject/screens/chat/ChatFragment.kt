package android.example.tinkoffproject.screens.chat

import androidx.fragment.app.Fragment
import android.example.tinkoffproject.R
import android.example.tinkoffproject.customviews.FlexBoxLayout
import android.example.tinkoffproject.customviews.ReactionCustomView
import android.example.tinkoffproject.data.UserMessage
import android.example.tinkoffproject.recyclerview.MessageCustomItemDecoration
import android.example.tinkoffproject.recyclerview.MessageAsyncAdapter
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
    private lateinit var myAdapter: MessageAsyncAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myAdapter = MessageAsyncAdapter(this)
        viewModel.users.observe(viewLifecycleOwner) {
            myAdapter.data = it
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.messages_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = myAdapter
        recyclerView.addItemDecoration(
            MessageCustomItemDecoration(
                requireContext(),
                viewModel.users.value!!
            )
        )

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
                myAdapter.notifyItemInserted(myAdapter.data.size)
                recyclerView.scrollToPosition(myAdapter.data.size - 1)
                messageText.text.clear()
            }
        }

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        NavigationUI.setupWithNavController(
            toolbar,
            findNavController()
        )
        toolbar.title = arguments?.getString("channel_name")
        view.findViewById<TextView>(R.id.chat_topic_name).text =
            getString(R.string.topic_name, arguments?.getString("topic_name"))

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
    }
}