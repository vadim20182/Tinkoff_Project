package android.example.tinkoffproject.contacts.ui

import android.example.tinkoffproject.contacts.model.ContactItem
import android.example.tinkoffproject.databinding.ContactItemBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class ContactsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var data: List<ContactItem> = emptyList()

    fun update(newList: List<ContactItem>) = DiffUtil.calculateDiff(DiffCallback(data, newList))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ContactItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        if (holder is ContactViewHolder)
            holder.bind(item)
    }

    override fun getItemCount(): Int = data.size


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

private class ContactViewHolder(private val binding: ContactItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(contact: ContactItem) {
        binding.contactItemName.text = contact.name
        binding.contactItemEmail.text = contact.email
        binding.contactItemAvatar.setImageResource(contact.avatarId)
    }
}