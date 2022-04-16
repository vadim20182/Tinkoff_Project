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

    @Query("DELETE FROM ${ContactEntity.TABLE_NAME}")
    fun clearChannels()

    @Query("SELECT * FROM ${ContactEntity.TABLE_NAME}")
    fun getAllContacts(): Single<List<ContactEntity>>

    @Query("SELECT * FROM ${ContactEntity.TABLE_NAME} WHERE contact_name LIKE :name")
    fun findContactsByName(name: String): Single<List<ContactEntity>>
}