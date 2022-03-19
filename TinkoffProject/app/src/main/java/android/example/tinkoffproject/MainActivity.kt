package android.example.tinkoffproject

import android.example.tinkoffproject.customviews.FlexBoxLayout
import android.example.tinkoffproject.customviews.ReactionCustomView
import android.example.tinkoffproject.data.UserMessage
import android.example.tinkoffproject.databinding.ActivityMainBinding
import android.example.tinkoffproject.recyclerview.CustomItemDecoration
import android.example.tinkoffproject.recyclerview.MessageAsyncAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.isEmpty
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.random.Random

class MainActivity : AppCompatActivity(), MessageAsyncAdapter.OnItemClickedListener {

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var myAdapter: MessageAsyncAdapter
    private val _binding: ActivityMainBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val binding
        get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        myAdapter = MessageAsyncAdapter(this)
        viewModel.users.observe(this) {
            myAdapter.data = it
        }

        val recyclerView = binding.messagesRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = myAdapter
        recyclerView.addItemDecoration(CustomItemDecoration(this, viewModel.users.value!!))

        findViewById<ImageView>(R.id.message_input_button).setOnClickListener {
            val messageText = findViewById<EditText>(R.id.send_message_text)
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
        BottomSheetDialog(this).apply {
            setContentView(R.layout.reactions_bottom_sheet_dialog_layout)
            addOnContextAvailableListener {
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
        }.show()
    }

    companion object {
        const val MY_USER_ID = 8L
    }
}