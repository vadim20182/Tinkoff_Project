package android.example.tinkoffproject.contacts.data.db

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface ContactsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContacts(contacts: List<ContactEntity>): Completable

    @Update
    fun updateContacts(contacts: List<ContactEntity>): Completable

    @Transaction
    fun insertAndRemoveInTransaction(contacts: List<ContactEntity>) {
        clearOldContacts(contacts.map {
            it.userId
        }).subscribe()
        insertContacts(contacts).subscribe()
    }

    @Query("SELECT * FROM ${ContactEntity.TABLE_NAME} ORDER BY contact_name ASC")
    fun loadAllContacts(): Single<List<ContactEntity>>

    @Query("SELECT * FROM ${ContactEntity.TABLE_NAME} ORDER BY contact_name ASC")
    fun getAllContacts(): Flowable<List<ContactEntity>>

    @Query("UPDATE ${ContactEntity.TABLE_NAME} SET contact_status=:status")
    fun removeContactsStatus(status: String = "offline"): Completable

    @Query("DELETE FROM ${ContactEntity.TABLE_NAME} WHERE contact_id NOT IN (:contactsIds)")
    fun clearOldContacts(contactsIds: List<Int>): Completable
}