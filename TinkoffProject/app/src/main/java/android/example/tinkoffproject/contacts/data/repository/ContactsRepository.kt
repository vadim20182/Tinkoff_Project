package android.example.tinkoffproject.contacts.data.repository

import android.example.tinkoffproject.contacts.data.db.ContactEntity
import android.example.tinkoffproject.contacts.data.network.ContactItem
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

interface ContactsRepository {
    val queryGetUsers: PublishSubject<Unit>
    val queryGetUserPresence: PublishSubject<Pair<ContactItem, List<ContactItem>>>
    val querySearch: PublishSubject<String>
    val queryReset: PublishSubject<String>

    val getUsersObservable: Observable<List<ContactItem>>

    val getUsersPresenceObservable: Flowable<Int>

    val resetSearchObservable: Observable<List<ContactItem>>

    val allContacts: MutableList<ContactItem>

    var currentContacts: List<ContactItem>


    fun loadContactsFromDb(): Single<List<ContactEntity>>

    fun getContactsFromDb(): Flowable<List<ContactEntity>>

    fun insertAndRemoveOldContacts(contacts: List<ContactEntity>)

    fun updateContacts(contacts: List<ContactEntity>): Disposable

    fun removeStatusFromContacts(): Disposable

    fun filterContacts(input: String): List<ContactItem>
}