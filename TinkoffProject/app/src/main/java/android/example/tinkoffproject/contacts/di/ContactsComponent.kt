package android.example.tinkoffproject.contacts.di

import android.example.tinkoffproject.contacts.ui.ContactsFragment
import dagger.Subcomponent
import javax.inject.Scope

@Contacts
@Subcomponent(modules = [ContactsModule::class, ContactsViewModelModule::class])
interface ContactsComponent {

    fun inject(contactsFragment: ContactsFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(): ContactsComponent
    }
}

@Scope
annotation class Contacts