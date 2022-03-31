package android.example.tinkoffproject.contacts.ui

import android.example.tinkoffproject.R
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout


class ContactsFragment : Fragment(R.layout.fragment_contacts) {

    private val viewModel: ContactsViewModel by viewModels()
    private val contactsAdapter: ContactsAdapter by lazy { ContactsAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchText = view.findViewById<EditText>(R.id.contact_user_search)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_contacts)
        val shimmer = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_contacts_view)

        with(viewModel) {
            isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    shimmer.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    shimmer.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
            contacts.observe(viewLifecycleOwner) {
                contactsAdapter.data = it
            }
        }


        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = contactsAdapter

        searchText.doAfterTextChanged { text ->
            viewModel.searchContact(text?.toString().orEmpty())
        }
    }
}