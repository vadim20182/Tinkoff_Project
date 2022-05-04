package android.example.tinkoffproject.contacts.data

import android.example.tinkoffproject.contacts.data.db.ContactEntity
import android.example.tinkoffproject.contacts.data.db.ContactsDAO
import android.example.tinkoffproject.contacts.data.network.ContactItem
import android.example.tinkoffproject.contacts.di.Contacts
import android.example.tinkoffproject.network.ApiService
import android.example.tinkoffproject.utils.convertContactFromNetworkToDb
import android.example.tinkoffproject.utils.makePublishSubject
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@Contacts
class ContactsRepository @Inject constructor(
    private val contactsDAO: ContactsDAO,
    private val client: ApiService
) {
    val queryGetUsers: PublishSubject<Unit> by lazy { makePublishSubject<Unit>() }
    val queryGetUserPresence: PublishSubject<Pair<ContactItem, List<ContactItem>>> by lazy { makePublishSubject<Pair<ContactItem, List<ContactItem>>>() }
    val querySearch = PublishSubject.create<String>()
    val queryReset = PublishSubject.create<String>()

    val getUsersObservable: Observable<List<ContactItem>> = queryGetUsers
        .observeOn(Schedulers.io())
        .switchMapSingle {
            client.getUsers()
                .map { rawResponse ->
                    val usersWithoutBots = rawResponse.users.filter { user ->
                        !user.isBot
                    }
                    usersWithoutBots
                }
        }.map { contacts ->
            insertContactsReplace(contacts.map {
                convertContactFromNetworkToDb(
                    it
                )
            })
            contacts
        }

    val getUsersPresenceObservable: Flowable<Int> = queryGetUserPresence
        .observeOn(Schedulers.io())
        .toFlowable(BackpressureStrategy.BUFFER)
        .onBackpressureBuffer(3000)
        .concatMapSingle { (user, list) ->
            client.getUserPresence(user.email)
                .map { presenceResponse ->
                    val index = list.indexOf(user)
                    val userUpdated =
                        user.copy(status = presenceResponse.presence.clientType.status)
                    updateContacts(listOf(convertContactFromNetworkToDb(userUpdated)))
                    index
                }
        }

    val resetSearchObservable = queryReset
        .observeOn(Schedulers.io())
        .switchMapSingle { input ->
            Single.fromCallable { filterContacts(input) }
                .subscribeOn(Schedulers.io())
        }

    val allContacts = mutableListOf<ContactItem>()

    var currentContacts: List<ContactItem> = emptyList()


    fun loadContactsFromDb() =
        contactsDAO.loadAllContacts()
            .subscribeOn(Schedulers.io())

    fun getContactsFromDb() =
        contactsDAO.getAllContacts()
            .subscribeOn(Schedulers.io())

    private fun insertContactsReplace(contacts: List<ContactEntity>): Disposable =
        contactsDAO.insertContacts(contacts)
            .subscribeOn(Schedulers.io())
            .subscribe()

    private fun updateContacts(contacts: List<ContactEntity>): Disposable =
        contactsDAO.updateContacts(contacts)
            .subscribeOn(Schedulers.io())
            .subscribe()

    fun removeStatusFromContacts(): Disposable =
        contactsDAO.removeContactsStatus()
            .subscribeOn(Schedulers.io())
            .subscribe()

    fun filterContacts(input: String) =
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
}