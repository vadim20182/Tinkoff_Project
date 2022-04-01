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
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import java.sql.Time
import java.util.*
import java.util.concurrent.TimeUnit


class ContactsFragment : Fragment(R.layout.fragment_contacts) {

    private val viewModel: ContactsViewModel by viewModels()
    private val contactsAdapter: ContactsAdapter by lazy { ContactsAdapter() }
    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchText = view.findViewById<EditText>(R.id.contact_user_search)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_contacts)
        val shimmer = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_contacts_view)
        val queryUpdateItems = PublishSubject.create<Boolean>()

        queryUpdateItems
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
                Single.fromCallable { Pair(it, contactsAdapter.update(viewModel.currentContacts)) }
                    .subscribeOn(Schedulers.io())
                    .doAfterSuccess {
                        queryTimeoutReset.onSuccess(true)
                        disposableTimeout.dispose()
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                contactsAdapter.data = viewModel.currentContacts
                it.second.dispatchUpdatesTo(contactsAdapter)
                recyclerView.scrollToPosition(0)
                if (!it.first) {
                    shimmer.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                } else {
                    shimmer.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            })
            .addTo(compositeDisposable)

        with(viewModel) {
            isLoading.observe(viewLifecycleOwner) { isLoading ->
                queryUpdateItems.onNext(isLoading)
            }
        }

        recyclerView.itemAnimator = null
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = contactsAdapter

        searchText.doAfterTextChanged { text ->
            viewModel.searchContact(text?.toString().orEmpty())
        }
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }
}