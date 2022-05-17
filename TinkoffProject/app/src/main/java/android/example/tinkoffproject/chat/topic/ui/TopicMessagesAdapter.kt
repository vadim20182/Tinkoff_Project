package android.example.tinkoffproject.chat.topic.ui

import android.example.tinkoffproject.R
import android.example.tinkoffproject.chat.common.ui.UiModel
import android.example.tinkoffproject.databinding.*
import android.example.tinkoffproject.message.customviews.FlexBoxLayout
import android.example.tinkoffproject.message.customviews.MessageCustomViewGroup
import android.example.tinkoffproject.message.customviews.MyMessageCustomViewGroup
import android.example.tinkoffproject.message.customviews.ReactionCustomView
import android.example.tinkoffproject.utils.EMOJI_MAP
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

private const val TYPE_MY_MESSAGE = 0
private const val TYPE_MESSAGE = 1
private const val TYPE_PLACEHOLDER = 2
private const val TYPE_SEPARATOR = 3



class MessageAsyncAdapter(
    private val onItemClickedListener: OnItemClickedListener
) : PagingDataAdapter<UiModel, MessageBaseViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is UiModel.MessageItem -> when ((getItem(position) as UiModel.MessageItem).isMyMessage) {
                true -> {
                    TYPE_MY_MESSAGE
                }
                else -> TYPE_MESSAGE
            }
            is UiModel.SeparatorMessageItem -> TYPE_SEPARATOR
            null -> TYPE_PLACEHOLDER
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageBaseViewHolder {
        return when (viewType) {
            TYPE_MESSAGE -> {
                val binding = MessageItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                val mergeLayoutBinding = MessageCustomViewGroupLayoutBinding.bind(binding.root)
                MessageViewHolder(mergeLayoutBinding, onItemClickedListener)
            }
            TYPE_MY_MESSAGE -> {
                val binding = MyMessageItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                val mergeLayoutBinding = MyMessageCustomViewGroupLayoutBinding.bind(binding.root)
                MyMessageViewHolder(mergeLayoutBinding, onItemClickedListener)
            }
            else -> {
                val binding = MessagesLoadingItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                PlaceholderViewHolder(binding.root)
            }
        }
    }

    override fun onBindViewHolder(holder: MessageBaseViewHolder, position: Int) {
        val uiModel = getItem(position)
        uiModel.let {
            when (uiModel) {
                is UiModel.MessageItem -> when (holder) {
                    is MessageViewHolder -> holder.bind((uiModel))
                    is MyMessageViewHolder -> holder.bind(uiModel)
                    else -> {}
                }
                else -> {}
            }
        }
    }

    interface OnItemClickedListener {
        fun onItemClicked(view: View, message:UiModel.MessageItem)
        fun onLinkClicked(link: String)
    }

    object DiffCallback : DiffUtil.ItemCallback<UiModel>() {
        override fun areItemsTheSame(oldItem: UiModel, newItem: UiModel): Boolean {
            return (oldItem is UiModel.MessageItem && newItem is UiModel.MessageItem &&
                    oldItem.messageId == newItem.messageId) ||
                    (oldItem is UiModel.SeparatorMessageItem && newItem is UiModel.SeparatorMessageItem &&
                            oldItem.topic == newItem.topic)

        }

        override fun areContentsTheSame(oldItem: UiModel, newItem: UiModel): Boolean {
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

    fun bind(userMessage: UiModel.MessageItem) {
        name.text = userMessage.name
        messageText.text = userMessage.messageText
        messageText.text = userMessage.messageText
        if (userMessage.fileLink != null) {
            messageText.setOnClickListener {
                onItemClickedListener.onLinkClicked(
                    userMessage.fileLink
                )
            }
            messageText.setTextColor(Color.BLUE)
        } else
            messageText.setTextColor(Color.WHITE)

        reactionsFlexBoxLayout.removeAllViews()

        if (userMessage.reactions.isNotEmpty()) {
            for (key in userMessage.reactions.keys) {
                reactionsFlexBoxLayout.addView((LayoutInflater.from(binding.root.context)
                    .inflate(
                        R.layout.emoji_item,
                        null
                    ) as ReactionCustomView).apply {
                    setEmojiNameAndCode(key, EMOJI_MAP[key] ?: 0x1F480)
                    setReactionCount(userMessage.reactions[key] ?: 0)
                    isSelected = userMessage.selectedReactions[key] == true
                    setOnClickListener {
                        onItemClickedListener.onItemClicked(
                            it,
                            userMessage
                        )
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
                    onItemClickedListener.onItemClicked(
                        this,
                        userMessage
                    )
                }
            })
        }
        messageText.setOnLongClickListener {
            onItemClickedListener.onItemClicked(it, userMessage)
            return@setOnLongClickListener true
        }
        if (userMessage.avatarUrl != null) {
            Glide.with(itemView.context)
                .asDrawable()
                .load(userMessage.avatarUrl)
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.avatar)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        (binding.root as MessageCustomViewGroup).setAvatar(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        } else
            (binding.root as MessageCustomViewGroup).setAvatarId(R.mipmap.avatar)

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

    fun bind(userMessage: UiModel.MessageItem) {
        messageText.text = userMessage.messageText
        if (userMessage.fileLink != null) {
            messageText.setOnClickListener {
                onItemClickedListener.onLinkClicked(
                    userMessage.fileLink
                )
            }
            messageText.setTextColor(Color.BLUE)
        } else
            messageText.setTextColor(Color.WHITE)
        reactionsFlexBoxLayout.removeAllViews()
        (binding.root as MyMessageCustomViewGroup).setShader(userMessage.isSent)

        if (userMessage.reactions.isNotEmpty()) {
            for (key in userMessage.reactions.keys) {
                reactionsFlexBoxLayout.addView((LayoutInflater.from(binding.root.context)
                    .inflate(
                        R.layout.emoji_item,
                        null
                    ) as ReactionCustomView).apply {
                    setEmojiNameAndCode(key, EMOJI_MAP[key] ?: 0x1F480)
                    setReactionCount(userMessage.reactions[key] ?: 0x1F480)
                    isSelected = userMessage.selectedReactions[key] == true
                    setOnClickListener {
                        onItemClickedListener.onItemClicked(
                            it,
                            userMessage
                        )
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
                    onItemClickedListener.onItemClicked(
                        this,
                        userMessage
                    )
                }
            })
        }
        messageText.setOnLongClickListener {
            onItemClickedListener.onItemClicked(it, userMessage)
            return@setOnLongClickListener true
        }
    }
}

private class PlaceholderViewHolder(view: View) : MessageBaseViewHolder(view) {}