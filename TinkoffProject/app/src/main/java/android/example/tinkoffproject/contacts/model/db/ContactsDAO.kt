package android.example.tinkoffproject.contacts.model.db

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface ContactsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContacts(contacts: List<ContactEntity>): Completable

    @Update
    fun updateContacts(contacts: List<ContactEntity>): Completable

    @Delete
    fun deleteContacts(contacts: List<ContactEntity>): Completable

    @Query("DELETE FROM contact")
    fun clearChannels()

    @Query("SELECT * FROM contact")
    fun getAllContacts(): Single<List<ContactEntity>>

    @Query("SELECT * FROM contact WHERE contact_name LIKE :name")
    fun findContactsByName(name: String): Single<List<ContactEntity>>
}