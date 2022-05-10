package android.example.tinkoffproject.contacts.data

import android.example.tinkoffproject.contacts.data.db.ContactEntity
import android.example.tinkoffproject.contacts.data.db.ContactsDAO
import android.example.tinkoffproject.contacts.data.repository.ContactsRepositoryImpl
import android.example.tinkoffproject.network.ApiService
import android.example.tinkoffproject.stub.ClientStub
import android.example.tinkoffproject.stub.ContactsDAOStub
import android.example.tinkoffproject.utils.RxRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.junit.Test

class ContactsRepositoryImplTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val rxRule = RxRule()

    @Test
    fun `loadContactsFromDb for 1 contact return list of contacts size 1`() {
        val contactsDAOStub = ContactsDAOStub(createContact())

        val contactsRepository = createContactsRepository(contactsDAO = contactsDAOStub)
        contactsRepository.loadContactsFromDb()
            .map {
                it.size
            }
            .test()
            .assertResult(1)
            .dispose()
    }

    private fun createContactsRepository(
        client: ApiService = ClientStub(),
        contactsDAO: ContactsDAO
    ) =
        ContactsRepositoryImpl(contactsDAO, client)

    private fun createContact(
        userId: Int = 1,
        name: String = "Ivan",
        email: String = "empty@empty.ru"
    ) =
        ContactEntity(userId, name, email)
}