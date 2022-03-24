package android.example.tinkoffproject.message.ui

import android.example.tinkoffproject.R
import android.example.tinkoffproject.message.model.UserMessage
import android.example.tinkoffproject.message.customviews.FlexBoxLayout
import android.example.tinkoffproject.message.customviews.MessageCustomViewGroup
import android.example.tinkoffproject.message.customviews.ReactionCustomView
import android.example.tinkoffproject.databinding.MessageCustomViewGroupLayoutBinding
import android.example.tinkoffproject.databinding.MessageItemBinding
import android.example.tinkoffproject.databinding.MyMessageCustomViewGroupLayoutBinding
import android.example.tinkoffproject.databinding.MyMessageItemBinding
import android.example.tinkoffproject.chat.ui.ChatFragment
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
    RecyclerView.Adapter<MessageBaseViewHolder>() {

    private val differ = AsyncListDiffer(this, DiffCallback)

    var data: List<UserMessage>
        set(value) = differ.submitList(value)
        get() = differ.currentList

    override fun getItemViewType(position: Int): Int =
        if (data[position].userId == ChatFragment.MY_USER_ID) TYPE_MY_MESSAGE else TYPE_MESSAGE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageBaseViewHolder {
        return when (viewType) {
            TYPE_MESSAGE -> {
                val binding = MessageItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                val mergeLayoutBinding = MessageCustomViewGroupLayoutBinding.bind(binding.root)
                MessageViewHolder(mergeLayoutBinding, onItemClickedListener)
            }
            else -> {
                val binding = MyMessageItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                val mergeLayoutBinding = MyMessageCustomViewGroupLayoutBinding.bind(binding.root)
                MyMessageViewHolder(mergeLayoutBinding, onItemClickedListener)
            }
        }
    }

    override fun onBindViewHolder(holder: MessageBaseViewHolder, position: Int) {
        val user = differ.currentList[position]
        when (holder) {
            is MessageViewHolder -> holder.bind(user)
            is MyMessageViewHolder -> holder.bind(user)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size


    interface OnItemClickedListener {
        fun onItemClicked(position: Int, view: View)
    }

    object DiffCallback : DiffUtil.ItemCallback<UserMessage>() {
        override fun areItemsTheSame(oldItem: UserMessage, newItem: UserMessage): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: UserMessage, newItem: UserMessage): Boolean {
            return oldItem == newItem
        }
    }
}

sealed class MessageBaseViewHolder(view: View) : RecyclerView.ViewHolder(view)

private class MessageViewHolder(
    private val binding: MessageCustomViewGroupLayoutBinding,
    private val onItemClickedListener: MessageAsyncAdapter.OnItemClickedListener
) :
    MessageBaseViewHolder(binding.root) {
    val name: TextView = binding.messageUserName
    private val messageText: TextView = binding.messageText
    private val reactionsFlexBoxLayout: FlexBoxLayout =
        binding.messageEmojis

    fun bind(userMessage: UserMessage) {
        name.text = userMessage.name
        messageText.text = userMessage.messageText
        reactionsFlexBoxLayout.removeAllViews()

        if (userMessage.reactions.isNotEmpty()) {
            for (key in userMessage.reactions.keys) {
                reactionsFlexBoxLayout.addView((LayoutInflater.from(binding.root.context)
                    .inflate(
                        R.layout.emoji_item,
                        null
                    ) as ReactionCustomView).apply {
                    setEmoji(key)
                    setReactionCount(userMessage.reactions[key]!!)
                    isSelected = userMessage.selectedReactions[key] == true
                    setOnClickListener {
                        onItemClickedListener.onItemClicked(adapterPosition, it)
                    }
                })
            }

            reactionsFlexBoxLayout.addView((LayoutInflater.from(binding.root.context)
                .inflate(
                    R.layout.emoji_item,
                    null
                ) as ReactionCustomView).apply {
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
        (binding.root as MessageCustomViewGroup).setAvatarId(userMessage.avatar)
    }
}

private class MyMessageViewHolder(
    private val binding: MyMessageCustomViewGroupLayoutBinding,
    private val onItemClickedListener: MessageAsyncAdapter.OnItemClickedListener
) :
    MessageBaseViewHolder(binding.root) {
    private val messageText: TextView = binding.messageText
    private val reactionsFlexBoxLayout: FlexBoxLayout =
        binding.messageEmojis

    fun bind(userMessage: UserMessage) {
        messageText.text = userMessage.messageText
        reactionsFlexBoxLayout.removeAllViews()

        if (userMessage.reactions.isNotEmpty()) {
            for (key in userMessage.reactions.keys) {
                reactionsFlexBoxLayout.addView((LayoutInflater.from(binding.root.context)
                    .inflate(
                        R.layout.emoji_item,
                        null
                    ) as ReactionCustomView).apply {
                    setEmoji(key)
                    setReactionCount(userMessage.reactions[key]!!)
                    isSelected = userMessage.selectedReactions[key] == true
                    setOnClickListener {
                        onItemClickedListener.onItemClicked(adapterPosition, it)
                    }
                })
            }

            reactionsFlexBoxLayout.addView((LayoutInflater.from(binding.root.context)
                .inflate(
                    R.layout.emoji_item,
                    null
                ) as ReactionCustomView).apply {
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