package android.example.tinkoffproject.stub

import android.example.tinkoffproject.contacts.data.db.ContactEntity
import android.example.tinkoffproject.contacts.data.db.ContactsDAO
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class ContactsDAOStub(private val contact: ContactEntity) : ContactsDAO {
    override fun insertContacts(contacts: List<ContactEntity>): Completable {
        TODO("Not yet implemented")
    }

    override fun updateContacts(contacts: List<ContactEntity>): Completable {
        TODO("Not yet implemented")
    }

    override fun loadAllContacts(): Single<List<ContactEntity>> =
        Single.just(listOf(contact))

    override fun getAllContacts(): Flowable<List<ContactEntity>> = Flowable.just(
        listOf(contact)
    )


    override fun removeContactsStatus(status: String): Completable {
        TODO("Not yet implemented")
    }
}