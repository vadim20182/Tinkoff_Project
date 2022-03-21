package android.example.tinkoffproject.screens.contacts

import android.example.tinkoffproject.R
import android.example.tinkoffproject.recyclerview.ContactsAdapter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ContactsFragment : Fragment(R.layout.fragment_contacts) {

    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var contactsAdapter: ContactsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactsAdapter = ContactsAdapter()
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_contacts)
        viewModel.contacts.observe(viewLifecycleOwner) {
            contactsAdapter.data = it
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = contactsAdapter
    }
}