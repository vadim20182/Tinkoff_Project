package android.example.tinkoffproject.contacts.presentation

import android.example.tinkoffproject.contacts.data.repository.ContactsRepository
import android.example.tinkoffproject.utils.SingleLiveEvent
import android.example.tinkoffproject.utils.convertContactFromDbToNetwork
import android.example.tinkoffproject.utils.displayErrorMessage
import android.example.tinkoffproject.utils.makeSearchObservable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ContactsViewModel @Inject constructor(val contactsRepository: ContactsRepository) :
    ViewModel() {
    private val disposables = mutableMapOf<String, Disposable>()
    private val compositeDisposable = CompositeDisposable()

    private val _itemToUpdate: MutableLiveData<Int> =
        MutableLiveData<Int>()
    val itemToUpdate: LiveData<Int> = _itemToUpdate

    private val _isLoading: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _isLoaded = SingleLiveEvent<Boolean>()
    val isLoaded: LiveData<Boolean>
        get() = _isLoaded

    init {
        subscribeSearch()
        subscribeGetUsers()
        subscribeGetUserPresence()
        subscribeRefresh()
        _isLoaded.value = false
    }

    private fun subscribeGetUsers() {
        disposables[KEY_GET_USERS]?.dispose()
        disposables[KEY_GET_USERS] = contactsRepository.getUsersObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                contactsRepository.allContacts.clear()
                contactsRepository.allContacts.addAll(it)
                contactsRepository.currentContacts = contactsRepository.allContacts
                _isLoading.value = false
                _isLoaded.value = true
                val tempContacts = contactsRepository.allContacts
                for (user in tempContacts)
                    contactsRepository.queryGetUserPresence.onNext(Pair(user, tempContacts))
            }, onError = {
                _errorMessage.value = displayErrorMessage(it, "Ошибка при загрузке пользователей")
                subscribeGetUsers()
            })
    }

    private fun subscribeGetUserPresence() {
        disposables[KEY_GET_PRESENCE]?.dispose()
        disposables[KEY_GET_PRESENCE] = contactsRepository.getUsersPresenceObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { index ->
                    contactsRepository.currentContacts =
                        contactsRepository.allContacts
                    _itemToUpdate.value = index
                },
                onError = {
                    _errorMessage.value =
                        displayErrorMessage(it, "Ошибка при загрузке статуса пользователей")
                    subscribeGetUserPresence()
                })
    }

    fun searchContact(input: String) {
        if (disposables[KEY_SEARCH]?.isDisposed == true) {
            subscribeSearch()
            compositeDisposable.clear()
        }
        contactsRepository.querySearch.onNext(input)
    }

    private fun subscribeReset() {
        contactsRepository.resetSearchObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { list ->
                    contactsRepository.currentContacts = list
                    _isLoading.value = false
                    subscribeSearch()
                    compositeDisposable.clear()
                }, onError = {
                    _errorMessage.value = "Ошибка при поиске"
                })
            .addTo(compositeDisposable)
    }

    private fun resetSearch() {
        disposables[KEY_SEARCH]?.dispose()
        subscribeReset()
        contactsRepository.queryReset.onNext(".")
    }


    private fun subscribeSearch() {
        disposables[KEY_SEARCH] =
            makeSearchObservable(contactsRepository.querySearch) { resetSearch() }
                .doOnNext {
                    _isLoading.value = true
                }
                .debounce(600, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .switchMapSingle { input ->
                    Single.fromCallable { contactsRepository.filterContacts(input) }
                        .subscribeOn(Schedulers.io())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        contactsRepository.currentContacts = it
                        _isLoading.value = false
                    }, onError = {
                        _errorMessage.value = "Ошибка при поиске"
                    })
    }

    fun loadContacts() {
        disposables[KEY_LOAD_CONTACTS]?.dispose()
        disposables[KEY_LOAD_CONTACTS] =
            contactsRepository.loadContactsFromDb()
                .map { dbList ->
                    dbList.map {
                        convertContactFromDbToNetwork(it)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { all ->
                    if (all.isNotEmpty()) {
                        contactsRepository.allContacts.clear()
                        contactsRepository.allContacts.addAll(all)
                        contactsRepository.currentContacts = all
                        _isLoading.value = false
                    } else {
                        _isLoading.value = true
                    }
                    if (isLoaded.value == false) {
                        contactsRepository.queryGetUsers.onNext(Unit)
                        _isLoaded.value = true
                    }
                    subscribeToDbUpdates()
                }, onError = {
                }).addTo(compositeDisposable)
    }

    private fun subscribeToDbUpdates() {
        disposables[KEY_SUBSCRIBE_TO_DB]?.dispose()
        disposables[KEY_SUBSCRIBE_TO_DB] = contactsRepository.getContactsFromDb()
            .map { dbList ->
                val dbResponse = dbList.map {
                    convertContactFromDbToNetwork(it)
                }
                dbResponse
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = { all ->
                if (all.isNotEmpty()) {
                    contactsRepository.allContacts.clear()
                    contactsRepository.allContacts.addAll(all)
                    contactsRepository.currentContacts = all
                    _isLoading.value = false
                }
            }, onError = {
            }).addTo(compositeDisposable)
    }

    private fun subscribeRefresh() {
        disposables[KEY_REFRESH]?.dispose()
        disposables[KEY_REFRESH] = queryRefresh
            .throttleFirst(4000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                subscribeGetUsers()
                subscribeGetUserPresence()
                _isLoaded.value = false
                loadContacts()
            }, onError = {
                _errorMessage.value =
                    displayErrorMessage(it, "Ошибка при обновлении списка контактов")
            })
    }

    fun refresh() {
        queryRefresh.onNext(Unit)
    }

    override fun onCleared() {
        contactsRepository.removeStatusFromContacts()
        for (key in disposables.keys)
            disposables[key]?.dispose()
        compositeDisposable.clear()
    }

    companion object {
        private const val KEY_SEARCH = "search contact"
        private const val KEY_GET_USERS = "get users"
        private const val KEY_GET_PRESENCE = "get presence"
        private const val KEY_LOAD_CONTACTS = "load contacts"
        private const val KEY_REFRESH = "refresh contacts"
        private const val KEY_SUBSCRIBE_TO_DB = "subscribe to db"
        val queryRefresh = PublishSubject.create<Unit>()
    }
}