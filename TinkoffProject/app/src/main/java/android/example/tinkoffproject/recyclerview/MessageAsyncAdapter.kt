package android.example.tinkoffproject.recyclerview

import android.example.tinkoffproject.MainActivity
import android.example.tinkoffproject.R
import android.example.tinkoffproject.data.UserMessage
import android.example.tinkoffproject.customviews.FlexBoxLayout
import android.example.tinkoffproject.customviews.MessageCustomViewGroup
import android.example.tinkoffproject.customviews.ReactionCustomView
import android.graphics.Rect
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

private const val TYPE_MY_MESSAGE = 0
private const val TYPE_MESSAGE = 1

class MessageAsyncAdapter(
    private val onItemClickedListener: OnItemClickedListener
) :
    RecyclerView.Adapter<MessageAsyncAdapter.BaseViewHolder>() {

    private val differ = AsyncListDiffer(this, DiffCallback)

    var data: List<UserMessage>
        set(value) = differ.submitList(value)
        get() = differ.currentList

    override fun getItemViewType(position: Int): Int =
        if (data[position].userId == MainActivity.MY_USER_ID) TYPE_MY_MESSAGE else TYPE_MESSAGE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_MESSAGE -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view =
                    layoutInflater.inflate(
                        R.layout.message_item,
                        parent,
                        false
                    )
                MessageViewHolder(view, onItemClickedListener)
            }
            else -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view =
                    layoutInflater.inflate(
                        R.layout.my_message_item,
                        parent,
                        false
                    )
                MyMessageViewHolder(view, onItemClickedListener)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val user = differ.currentList[position]
        when (holder) {
            is MessageViewHolder -> holder.bind(user)
            is MyMessageViewHolder -> holder.bind(user)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    abstract class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view)

    interface OnItemClickedListener {
        fun onItemClicked(position: Int, view: View)
    }

    class MessageViewHolder(
        itemView: View,
        private val onItemClickedListener: OnItemClickedListener
    ) :
        BaseViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.message_user_name)
        private val messageText: TextView = itemView.findViewById(R.id.message_text)
        private val reactionsFlexBoxLayout: FlexBoxLayout =
            itemView.findViewById(R.id.message_emojis)

        fun bind(userMessage: UserMessage) {
            name.text = userMessage.name
            messageText.text = userMessage.messageText
            reactionsFlexBoxLayout.removeAllViews()

            for (key in userMessage.reactions.keys) {
                reactionsFlexBoxLayout.addView((LayoutInflater.from(itemView.context)
                    .inflate(R.layout.emoji_item, null) as ReactionCustomView).apply {
                    setEmoji(key)
                    setReactionCount(userMessage.reactions[key]!!)
                    isSelected = userMessage.selectedReactions[key] == true
                    setOnClickListener {
                        onItemClickedListener.onItemClicked(adapterPosition, it)
                    }
                })
            }

            if (userMessage.reactions.isNotEmpty()) {
                reactionsFlexBoxLayout.addView((LayoutInflater.from(itemView.context)
                    .inflate(R.layout.emoji_item, null) as ReactionCustomView).apply {
                    isButton = true
                    setOnClickListener {
                        onItemClickedListener.onItemClicked(adapterPosition, this)
                    }
                })
            }
            messageText.setOnLongClickListener {
                onItemClickedListener.onItemClicked(adapterPosition, it)
                return@setOnLongClickListener true
            }
            (itemView as MessageCustomViewGroup).setAvatarId(userMessage.avatar)
        }
    }

    class MyMessageViewHolder(
        itemView: View,
        private val onItemClickedListener: OnItemClickedListener
    ) :
        BaseViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.message_text)
        private val reactionsFlexBoxLayout: FlexBoxLayout =
            itemView.findViewById(R.id.message_emojis)

        fun bind(userMessage: UserMessage) {
            messageText.text = userMessage.messageText
            reactionsFlexBoxLayout.removeAllViews()

            for (key in userMessage.reactions.keys) {
                reactionsFlexBoxLayout.addView((LayoutInflater.from(itemView.context)
                    .inflate(R.layout.emoji_item, null) as ReactionCustomView).apply {
                    setEmoji(key)
                    setReactionCount(userMessage.reactions[key]!!)
                    isSelected = userMessage.selectedReactions[key] == true
                    setOnClickListener {
                        onItemClickedListener.onItemClicked(adapterPosition, it)
                    }
                })
            }

            if (userMessage.reactions.isNotEmpty()) {
                reactionsFlexBoxLayout.addView((LayoutInflater.from(itemView.context)
                    .inflate(R.layout.emoji_item, null) as ReactionCustomView).apply {
                    isButton = true
                    setOnClickListener {
                        onItemClickedListener.onItemClicked(adapterPosition, this)
                    }
                })
            }
            messageText.setOnLongClickListener {
                onItemClickedListener.onItemClicked(adapterPosition, it)
                return@setOnLongClickListener true
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<UserMessage>() {
        override fun areItemsTheSame(oldItem: UserMessage, newItem: UserMessage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: UserMessage, newItem: UserMessage): Boolean {
            return oldItem == newItem
        }
    }
}