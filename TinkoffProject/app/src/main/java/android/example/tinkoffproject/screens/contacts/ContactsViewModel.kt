package android.example.tinkoffproject.screens.contacts

import android.example.tinkoffproject.data.ContactItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContactsViewModel : ViewModel() {

    private val _contacts: MutableLiveData<MutableList<ContactItem>> =
        MutableLiveData<MutableList<ContactItem>>()
    val contacts: LiveData<out List<ContactItem>> = _contacts

    private val currentContacts: MutableList<ContactItem>
        get() = _contacts.value ?: mutableListOf()

    init {
        loadContacts()
    }

    private fun loadContacts() {
        _contacts.value = mutableListOf()
        for (i in 0..19) {
            currentContacts.add(ContactItem(i.toLong(), "Ivan $i", "ivan$i@mail.ru"))
        }
    }
}