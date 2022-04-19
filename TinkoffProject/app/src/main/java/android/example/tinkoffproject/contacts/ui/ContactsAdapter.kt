package android.example.tinkoffproject.contacts.ui

import android.example.tinkoffproject.R
import android.example.tinkoffproject.contacts.data.network.ContactItem
import android.example.tinkoffproject.databinding.ContactItemBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class ContactsAdapter(
    private val onItemClickedListener: OnItemClickedListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var data: List<ContactItem> = emptyList()

    fun update(newList: List<ContactItem>) = DiffUtil.calculateDiff(DiffCallback(data, newList))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ContactItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ContactViewHolder(binding, onItemClickedListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        if (holder is ContactViewHolder)
            holder.bind(item)
    }

    override fun getItemCount(): Int = data.size

    interface OnItemClickedListener {
        fun onItemClicked(position: Int, item: ContactItem)
    }


    inner class DiffCallback(
        private val oldData: List<ContactItem>,
        private val newData: List<ContactItem>
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

private class ContactViewHolder(
    private val binding: ContactItemBinding,
    private val onItemClickedListener: ContactsAdapter.OnItemClickedListener
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(contact: ContactItem) {
        binding.contactItemName.text = contact.name
        binding.contactItemEmail.text = contact.email
        if (contact.avatarUrl != null) {
            Glide.with(itemView.context)
                .asDrawable()
                .load(contact.avatarUrl)
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.avatar)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(binding.contactItemAvatar)
        } else
            binding.contactItemAvatar.setImageResource(R.mipmap.avatar)

        when (contact.status) {
            "active" -> binding.contactItemStatus.setImageResource(android.R.drawable.presence_online)
            "idle" -> binding.contactItemStatus.setImageResource(android.R.drawable.presence_away)
            else -> binding.contactItemStatus.setImageResource(android.R.drawable.presence_busy)
        }
        binding.contactItemLayer.setOnClickListener {
            onItemClickedListener.onItemClicked(absoluteAdapterPosition, contact)
        }
    }
}