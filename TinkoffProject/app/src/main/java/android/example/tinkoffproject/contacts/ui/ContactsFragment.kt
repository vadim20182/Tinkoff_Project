package android.example.tinkoffproject.contacts.ui

import android.example.tinkoffproject.R
import android.example.tinkoffproject.channels.data.ChannelsRepository
import android.example.tinkoffproject.channels.presentation.all.AllChannelViewModelFactory
import android.example.tinkoffproject.contacts.data.ContactsRepository
import android.example.tinkoffproject.contacts.data.network.ContactItem
import android.example.tinkoffproject.contacts.presentation.ContactsViewModel
import android.example.tinkoffproject.contacts.presentation.ContactsViewModelFactory
import android.example.tinkoffproject.database.AppDatabase
import android.example.tinkoffproject.profile.ui.ProfileFragment
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
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import java.util.concurrent.TimeUnit

class ContactsFragment : Fragment(R.layout.fragment_contacts),
    ContactsAdapter.OnItemClickedListener {

    private val viewModel: ContactsViewModel by viewModels {
        ContactsViewModelFactory(
            ContactsRepository(
                AppDatabase.getInstance(requireContext()).contactsDAO()
            )
        )
    }
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

        makeSearchDisposable(queryUpdateItems, shimmer, recyclerView)
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

    private fun makeSearchDisposable(
        querySearch: PublishSubject<Boolean>,
        shimmer: ShimmerFrameLayout,
        recyclerView: RecyclerView
    ) = querySearch
        .debounce(100, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .switchMapSingle {
            val queryTimeoutReset = SingleSubject.create<Boolean>()
            val disposableTimeout = queryTimeoutReset
                .subscribeOn(Schedulers.io())
                .timeout(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    shimmer.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                })
            Single.fromCallable {
                Pair(
                    it, contactsAdapter.update(viewModel.contactsRepository.currentContacts)
                )
            }
                .subscribeOn(Schedulers.io())
                .doAfterSuccess {
                    queryTimeoutReset.onSuccess(true)
                    disposableTimeout.dispose()
                }
        }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(onNext = {
            contactsAdapter.data = viewModel.contactsRepository.currentContacts
            it.second.dispatchUpdatesTo(contactsAdapter)
            if (!it.first) {
                shimmer.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            } else {
                shimmer.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        })
}