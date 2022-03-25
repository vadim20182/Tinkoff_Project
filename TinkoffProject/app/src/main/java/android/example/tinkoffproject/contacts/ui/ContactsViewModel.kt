package android.example.tinkoffproject.contacts.ui

import android.example.tinkoffproject.contacts.model.ContactItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContactsViewModel : ViewModel() {

    private val _contacts: MutableLiveData<MutableList<ContactItem>> =
        MutableLiveData<MutableList<ContactItem>>()
    val contacts: LiveData<out List<ContactItem>> = _contacts

    private val currentContacts: List<ContactItem>
        get() = _contacts.value ?: emptyList()

    init {
        loadContacts()
    }

    private fun loadContacts() {
        val list = mutableListOf<ContactItem>()
        for (i in 0..19) {
            list.add(ContactItem(i.toLong(), "Ivan $i", "ivan$i@mail.ru"))
        }
        _contacts.value = list
    }
}