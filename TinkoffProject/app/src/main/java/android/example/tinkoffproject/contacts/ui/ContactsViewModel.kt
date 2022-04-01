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
import kotlinx.coroutines.delay
import java.util.*
import java.util.concurrent.TimeUnit

class ContactsViewModel : ViewModel() {

    private val querySearch = PublishSubject.create<String>()
    private val queryReset = PublishSubject.create<String>()
    private lateinit var searchDisposable: Disposable
    private val compositeDisposable = CompositeDisposable()

    private val allContacts = mutableListOf<ContactItem>()

    var currentContacts: List<ContactItem> = emptyList()
        private set

    private val _isLoading: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadContacts()
        subscribeSearch()
    }

    fun searchContact(input: String) {
        if (searchDisposable.isDisposed) {
            subscribeSearch()
            compositeDisposable.clear()
        }
        querySearch.onNext(input)
    }

    private fun subscribeReset() {
        queryReset
            .observeOn(Schedulers.io())
            .switchMapSingle { input ->
                Single.fromCallable { filterContacts(input) }
                    .subscribeOn(Schedulers.io())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    currentContacts = it
                    _isLoading.value = false
                    subscribeSearch()
                    compositeDisposable.clear()
                })
            .addTo(compositeDisposable)
    }

    private fun resetSearch() {
        searchDisposable.dispose()
        subscribeReset()
        queryReset.onNext(".")
    }

    private fun filterContacts(input: String) =
        allContacts.filter {
            it.name.contains(
                Regex(
                    input,
                    RegexOption.IGNORE_CASE
                )
            )
        }

    private fun subscribeSearch() {
        searchDisposable = querySearch
            .map { query -> query.trim() }
            .scan { previous, current ->
                if (current.isBlank() && previous.isNotBlank())
                    resetSearch()
                current
            }
            .filter { it.isNotBlank() }
            .distinctUntilChanged()
            .doOnNext {
                _isLoading.value = true
            }
            .debounce(1000, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.io())
            .switchMapSingle { input ->
                Single.fromCallable { filterContacts(input) }
                    .subscribeOn(Schedulers.io())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    currentContacts = it
                    _isLoading.value = false
                })
    }

    private fun loadContacts() {
        _isLoading.value = true
        Completable.fromCallable {
            for (i in 0..10000)
                allContacts.add(ContactItem(i.toLong(), "Ivan $i", "ivan$i@mail.ru"))
        }
            .subscribeOn(Schedulers.io())
            .delay(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    currentContacts = allContacts
                    _isLoading.value = false
                }
            )
            .addTo(compositeDisposable)
    }

    override fun onCleared() {
        searchDisposable.dispose()
        compositeDisposable.clear()
    }
}