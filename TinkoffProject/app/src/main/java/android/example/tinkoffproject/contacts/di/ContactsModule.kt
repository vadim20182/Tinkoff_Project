package android.example.tinkoffproject.contacts.di

import android.example.tinkoffproject.contacts.data.db.ContactsDAO
import android.example.tinkoffproject.contacts.presentation.ContactsViewModel
import android.example.tinkoffproject.database.AppDatabase
import android.example.tinkoffproject.di.ViewModelKey
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class ContactsModule {

    @Provides
    @Contacts
    fun provideContactsDAO(database: AppDatabase): ContactsDAO = database.contactsDAO()
}

@Module
abstract class ContactsViewModelModule {
    @Contacts
    @Binds
    @IntoMap
    @ViewModelKey(ContactsViewModel::class)
    abstract fun bindViewModel(viewModel: ContactsViewModel): ViewModel
}