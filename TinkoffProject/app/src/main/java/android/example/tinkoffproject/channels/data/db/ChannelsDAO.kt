package android.example.tinkoffproject.channels.data.db

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface ChannelsDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChannelsIgnore(channels: List<ChannelEntity>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannelsReplace(channels: List<ChannelEntity>): Completable

    @Update
    fun updateChannels(channels: List<ChannelEntity>): Completable

    @Query("DELETE FROM ${ChannelEntity.TABLE_NAME}")
    fun clearChannels(): Completable

    @Query("SELECT * FROM ${ChannelEntity.TABLE_NAME} WHERE NOT channel_is_my ORDER BY channel_name ASC")
    fun getAllChannels(): Flowable<List<ChannelEntity>>

    @Query("SELECT * FROM ${ChannelEntity.TABLE_NAME} WHERE NOT channel_is_my ORDER BY channel_name ASC")
    fun loadAllChannels(): Single<List<ChannelEntity>>

    @Query("SELECT * FROM ${ChannelEntity.TABLE_NAME} WHERE channel_is_my ORDER BY channel_name ASC")
    fun getMyAllChannels(): Flowable<List<ChannelEntity>>

    @Query("SELECT * FROM ${ChannelEntity.TABLE_NAME} WHERE channel_is_my ORDER BY channel_name ASC")
    fun loadMyAllChannels(): Single<List<ChannelEntity>>
}



