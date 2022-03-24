package android.example.tinkoffproject.contacts.ui

import android.example.tinkoffproject.contacts.model.ContactItem
import android.example.tinkoffproject.databinding.ContactItemBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class ContactsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val differ = AsyncListDiffer(this, DiffCallback)

    var data: List<ContactItem>
        set(value) = differ.submitList(value)
        get() = differ.currentList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ContactItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = differ.currentList[position]
        if (holder is ContactViewHolder)
            holder.bind(item)
    }

    override fun getItemCount(): Int = differ.currentList.size


    object DiffCallback : DiffUtil.ItemCallback<ContactItem>() {
        override fun areItemsTheSame(oldItem: ContactItem, newItem: ContactItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ContactItem, newItem: ContactItem): Boolean {
            return oldItem == newItem
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