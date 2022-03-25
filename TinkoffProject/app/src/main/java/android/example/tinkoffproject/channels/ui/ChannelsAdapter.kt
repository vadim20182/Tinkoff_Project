package android.example.tinkoffproject.channels.ui

import android.example.tinkoffproject.channels.model.ChannelItem
import android.example.tinkoffproject.databinding.ChannelItemBinding
import android.example.tinkoffproject.databinding.TopicItemBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView


private const val TYPE_ITEM_HEADER = 2
private const val TYPE_ITEM_CHILD = 3

class ChannelsAdapter(
    private val onItemClickedListener: OnItemClickedListener
) :
    RecyclerView.Adapter<ChannelsBaseViewHolder>() {

    private val differ = AsyncListDiffer(this, DiffCallback)

    var data: List<ChannelItem>
        set(value) = differ.submitList(value)
        get() = differ.currentList

    override fun getItemCount(): Int = differ.currentList.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChannelsBaseViewHolder {
        return when (viewType) {
            TYPE_ITEM_HEADER -> {
                val binding = ChannelItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HeaderViewHolder(binding, onItemClickedListener)
            }
            else -> {
                val binding = TopicItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ChildViewHolder(binding, onItemClickedListener)
            }
        }
    }

    override fun onBindViewHolder(holder: ChannelsBaseViewHolder, position: Int) {
        val item = differ.currentList[position]
        when (holder) {
            is HeaderViewHolder -> holder.bind(item)
            is ChildViewHolder -> holder.bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (differ.currentList[position].isTopic) TYPE_ITEM_CHILD
        else TYPE_ITEM_HEADER


    interface OnItemClickedListener {
        fun onItemClicked(position: Int, item: ChannelItem)
    }


    object DiffCallback : DiffUtil.ItemCallback<ChannelItem>() {
        override fun areItemsTheSame(oldItem: ChannelItem, newItem: ChannelItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ChannelItem, newItem: ChannelItem): Boolean {
            return oldItem == newItem
        }
    }
}

sealed class ChannelsBaseViewHolder(view: View) : RecyclerView.ViewHolder(view)

private class ChildViewHolder(
    private val binding: TopicItemBinding,
    private val onItemClickedListener: ChannelsAdapter.OnItemClickedListener
) : ChannelsBaseViewHolder(binding.root) {
    fun bind(item: ChannelItem) {
        binding.topicItemName.text = item.name
        binding.topicItemLayout.setOnClickListener {
            onItemClickedListener.onItemClicked(
                adapterPosition,
                item
            )
        }
    }
}

private class HeaderViewHolder(
    private val binding: ChannelItemBinding,
    private val onItemClickedListener: ChannelsAdapter.OnItemClickedListener
) : ChannelsBaseViewHolder(binding.root) {
    fun bind(item: ChannelItem) {
        binding.channelItemName.text = item.name
        if (item.isExpanded)
            binding.channelItemArrow.setImageResource(android.R.drawable.arrow_up_float)
        else
            binding.channelItemArrow.setImageResource(android.R.drawable.arrow_down_float)
        binding.channelItemLayout.setOnClickListener {
            binding.channelItemArrow.showNext()
            onItemClickedListener.onItemClicked(
                adapterPosition,
                item
            )
        }
    }
}