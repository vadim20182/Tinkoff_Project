package android.example.tinkoffproject.contacts.ui

import android.example.tinkoffproject.contacts.model.ContactItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class ContactsViewModel : ViewModel() {

    private val querySearch = PublishSubject.create<String>()
    private lateinit var searchDisposable: Disposable
    private val compositeDisposable = CompositeDisposable()

    private val allContacts = mutableListOf<ContactItem>().apply {
        for (i in 0..19) {
            this.add(ContactItem(i.toLong(), "Ivan $i", "ivan$i@mail.ru"))
        }
    }

    private val _contacts: MutableLiveData<List<ContactItem>> =
        MutableLiveData<List<ContactItem>>()
    val contacts: LiveData<out List<ContactItem>> = _contacts

    private val _isSearchCompleted: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>()
    val isSearchCompleted: LiveData<Boolean> = _isSearchCompleted

    private val currentContacts: List<ContactItem>
        get() = _contacts.value ?: emptyList()

    private var _currentSearch = ""
    val currentSearch
        get() = _currentSearch

    init {
        loadContacts()
        subscribe()
    }

    fun searchContact(input: String) {
        _currentSearch = input
        _isSearchCompleted.value = false
        querySearch.onNext(input)
    }

    fun resetSearch() {
        _currentSearch = ""
        searchDisposable.dispose()
        _isSearchCompleted.value = true
        _contacts.value = allContacts
        subscribe()
    }

    private fun filterContacts(input: String) = allContacts.filter { it.name.contains(input, true) }

    private fun subscribe() {
        searchDisposable = querySearch
            .map { query -> query.trim() }
            .debounce(1500, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.io())
            .switchMapSingle { input ->
                Single.fromCallable { filterContacts(input) }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    _contacts.value = it
                    _isSearchCompleted.value = true
                })
    }

    private fun loadContacts() {
        _isSearchCompleted.value = false
        val list = mutableListOf<ContactItem>()
        Completable.fromCallable {}
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                for (i in 0..19)
                    list.add(ContactItem(i.toLong(), "Ivan $i", "ivan$i@mail.ru"))
            }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    _contacts.value = list
                    _isSearchCompleted.value = true
                }
            )
            .addTo(compositeDisposable)
    }

    override fun onCleared() {
        searchDisposable.dispose()
        compositeDisposable.clear()
    }
}