package android.example.tinkoffproject.contacts.ui

import android.example.tinkoffproject.R
import android.example.tinkoffproject.contacts.model.network.ContactItem
import android.example.tinkoffproject.profile.ui.ProfileFragment
import android.example.tinkoffproject.utils.makeSearchDisposable
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject


class ContactsFragment : Fragment(R.layout.fragment_contacts),
    ContactsAdapter.OnItemClickedListener {

    private val viewModel: ContactsViewModel by viewModels()
    private val contactsAdapter: ContactsAdapter by lazy { ContactsAdapter(this) }
    private val navController: NavController by lazy {
        var parent = parentFragment
        var navController = findNavController()
        while (parent != null) {
            if (parent is NavHostFragment)
                navController = parent.findNavController()
            parent = parent.parentFragment
        }
        return@lazy navController
    }
    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchText = view.findViewById<EditText>(R.id.contact_user_search)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_contacts)
        val shimmer = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_contacts_view)
        val queryUpdateItems = PublishSubject.create<Boolean>()

        makeSearchDisposable(queryUpdateItems, shimmer, recyclerView, contactsAdapter, viewModel)
            .addTo(compositeDisposable)

        with(viewModel) {
            isLoading.observe(viewLifecycleOwner) { isLoading ->
                queryUpdateItems.onNext(isLoading)
            }
            itemToUpdate.observe(viewLifecycleOwner) { itemToUpdate ->
                contactsAdapter.notifyItemChanged(itemToUpdate)
            }
            errorMessage.observe(viewLifecycleOwner) {
                Snackbar.make(
                    view,
                    it,
                    Snackbar.LENGTH_SHORT
                ).apply {
                    setTextColor(Color.WHITE)
                    setBackgroundTint(Color.RED)
                }.show()
            }
            if (isLoaded.value == false)
                loadContacts()
        }

        recyclerView.itemAnimator = null
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = contactsAdapter

        searchText.doAfterTextChanged { text ->
            viewModel.searchContact(text?.toString().orEmpty())
        }
    }

    override fun onItemClicked(position: Int, item: ContactItem) {
        val bundle =
            bundleOf(
                ProfileFragment.ARG_PROFILE_NAME to item.name,
                ProfileFragment.ARG_PROFILE_STATUS to item.status,
                ProfileFragment.ARG_PROFILE_AVATAR to item.avatarUrl
            )
        navController.navigate(R.id.action_contactsFragment_to_userProfileFragment, bundle)
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }
}