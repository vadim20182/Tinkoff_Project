package android.example.tinkoffproject.contacts.ui

import android.example.tinkoffproject.chat.ui.SingleLiveEvent
import android.example.tinkoffproject.contacts.model.ContactItem
import android.example.tinkoffproject.network.NetworkClient
import android.example.tinkoffproject.network.NetworkClient.client
import android.example.tinkoffproject.utils.makePublishSubject
import android.example.tinkoffproject.utils.makeSearchObservable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class ContactsViewModel : ViewModel() {

    private val queryGetUsers: PublishSubject<Unit> by lazy { makePublishSubject<Unit>() }
    private val queryGetUserPresence: PublishSubject<ContactItem> by lazy { makePublishSubject<ContactItem>() }
    private val querySearch = PublishSubject.create<String>()
    private val queryReset = PublishSubject.create<String>()

    private val disposables = mutableMapOf<String, Disposable>()
    private val compositeDisposable = CompositeDisposable()

    private val allContacts = mutableListOf<ContactItem>()

    private val _itemToUpdate: MutableLiveData<Int> =
        MutableLiveData<Int>()
    val itemToUpdate: LiveData<Int> = _itemToUpdate

    var currentContacts: List<ContactItem> = emptyList()
        private set

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
        _isLoaded.value = false
    }

    private fun subscribeGetUsers() {
        disposables[KEY_GET_USERS]?.dispose()
        disposables[KEY_GET_USERS] = queryGetUsers
            .observeOn(Schedulers.io())
            .flatMapSingle {
                client.getUsers()
                    .map { rawResponse ->
                        val usersWithoutBots = rawResponse.users.filter { user ->
                            !user.isBot
                        }
                        usersWithoutBots
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                allContacts.clear()
                allContacts.addAll(it)
                currentContacts = allContacts
                _isLoading.value = false
                _isLoaded.value = true
                val tempContacts = allContacts
                for (user in tempContacts)
                    queryGetUserPresence.onNext(user)
            }, onError = {
                _errorMessage.value = "Ошибка при загрузке пользователей"
                subscribeGetUsers()
            })
    }

    private fun subscribeGetUserPresence() {
        disposables[KEY_GET_PRESENCE]?.dispose()
        disposables[KEY_GET_PRESENCE] = queryGetUserPresence
            .observeOn(Schedulers.io())
            .toFlowable(BackpressureStrategy.BUFFER)
            .onBackpressureBuffer(3000)
            .concatMapSingle { user ->
                client.getUserPresence(user.email)
                    .map { presenceResponse ->
                        val index = allContacts.indexOf(user)
                        val userUpdated =
                            user.copy(status = presenceResponse.presence.clientType.status)
                        allContacts[index] = userUpdated
                        index
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    currentContacts = allContacts
                    _itemToUpdate.value = it
                },
                onError = {
                    _errorMessage.value = "Ошибка при загрузке пользователей"
                    subscribeGetUserPresence()
                })
    }

    fun searchContact(input: String) {
        if (disposables[KEY_SEARCH]?.isDisposed == true) {
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
                }, onError = {
                    _errorMessage.value = "Ошибка при поиске"
                })
            .addTo(compositeDisposable)
    }

    private fun resetSearch() {
        disposables[KEY_SEARCH]?.dispose()
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
            ) || it.email.contains(
                Regex(
                    input,
                    RegexOption.IGNORE_CASE
                )
            )
        }

    private fun subscribeSearch() {
        disposables[KEY_SEARCH] = makeSearchObservable(querySearch) { resetSearch() }
            .doOnNext {
                _isLoading.value = true
            }
            .debounce(600, TimeUnit.MILLISECONDS)
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
                }, onError = {
                    _errorMessage.value = "Ошибка при поиске"
                })
    }

    fun loadContacts() {
        _isLoading.value = true
        queryGetUsers.onNext(Unit)
    }

    override fun onCleared() {
        for (key in disposables.keys)
            disposables[key]?.dispose()
        compositeDisposable.clear()
    }

    companion object {
        private const val KEY_SEARCH = "search contact"
        private const val KEY_GET_USERS = "get users"
        private const val KEY_GET_PRESENCE = "get presence"
    }
}