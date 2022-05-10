package android.example.tinkoffproject.contacts.presentation

import android.example.tinkoffproject.contacts.data.repository.ContactsRepositoryImpl
import android.example.tinkoffproject.contacts.data.db.ContactEntity
import android.example.tinkoffproject.contacts.data.db.ContactsDAO
import android.example.tinkoffproject.network.ApiService
import android.example.tinkoffproject.stub.ClientStub
import android.example.tinkoffproject.stub.ContactsDAOStub
import android.example.tinkoffproject.utils.RxRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test


class ContactsViewModelTest {
    @get:Rule
    val viewModelRule = InstantTaskExecutorRule()

    @get:Rule
    val rxRule = RxRule()

    @Test
    fun `contactsViewModel after loading for isLoaded return true`() {
        val contactsDAOStub = ContactsDAOStub(createContact())

        val contactsRepository = createContactsRepository(contactsDAO = contactsDAOStub)
        val viewModel = createViewModel(contactsRepository)

        assertEquals(false, viewModel.isLoaded.value)

        viewModel.loadContacts()

        assertEquals(true, viewModel.isLoaded.value)
    }

    private fun createViewModel(contactsRepositoryImpl: ContactsRepositoryImpl) =
        ContactsViewModel(contactsRepositoryImpl)

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