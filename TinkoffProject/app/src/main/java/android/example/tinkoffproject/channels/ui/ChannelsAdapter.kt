package android.example.tinkoffproject.channels.ui

import android.app.Application
import android.content.res.Resources
import android.example.tinkoffproject.App
import android.example.tinkoffproject.R
import android.example.tinkoffproject.channels.data.network.ChannelItem
import android.example.tinkoffproject.databinding.ChannelItemBinding
import android.example.tinkoffproject.databinding.TopicItemBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView


private const val TYPE_ITEM_HEADER = 2
private const val TYPE_ITEM_CHILD = 3

class ChannelsAdapter(
    private val onItemClickedListener: OnItemClickedListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var data: List<ChannelItem> = emptyList()

    fun update(newList: List<ChannelItem>) = DiffUtil.calculateDiff(DiffCallback(data, newList))

    override fun getItemCount(): Int = data.size

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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        when (holder) {
            is HeaderViewHolder -> holder.bind(item)
            is ChildViewHolder -> holder.bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (data[position].isTopic) TYPE_ITEM_CHILD
        else TYPE_ITEM_HEADER


    interface OnItemClickedListener {
        fun onItemClicked(position: Int, item: ChannelItem, view: View)
    }

    inner class DiffCallback(
        private val oldData: List<ChannelItem>,
        private val newData: List<ChannelItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldData.size
        }

        override fun getNewListSize(): Int {
            return newData.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldData[oldItemPosition].name == newData[newItemPosition].name
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldData[oldItemPosition] == newData[newItemPosition]
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
                absoluteAdapterPosition,
                item,
                it
            )
        }
    }
}

private class HeaderViewHolder(
    private val binding: ChannelItemBinding,
    private val onItemClickedListener: ChannelsAdapter.OnItemClickedListener
) : ChannelsBaseViewHolder(binding.root) {
    fun bind(item: ChannelItem) {
        binding.channelItemName.text =
            App.appContext.getString(R.string.channel_name_item, item.name)
        if (item.isExpanded)
            binding.channelItemArrow.setImageResource(R.drawable.ic_channel_arrow_expanded)
        else
            binding.channelItemArrow.setImageResource(R.drawable.ic_channel_arrow)
        binding.channelItemClickableLayout.setOnClickListener {
            binding.channelItemArrow.showNext()
            onItemClickedListener.onItemClicked(
                absoluteAdapterPosition,
                item,
                it
            )
        }
        binding.channelItemName.setOnClickListener {
            onItemClickedListener.onItemClicked(
                absoluteAdapterPosition,
                item,
                it
            )
        }
    }
}