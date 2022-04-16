package android.example.tinkoffproject.channels.model.db

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

sealed interface ChannelsDAO<T : ChannelEntity> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChannelsIgnore(channels: List<T>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannelsReplace(channels: List<T>): Completable

    @Update
    fun updateChannels(channels: List<T>): Completable

    @Delete
    fun deleteChannels(channels: List<T>): Completable
}

@Dao
interface AllChannelsDAO : ChannelsDAO<ChannelEntity.AllChannelsEntity> {

    @Query("DELETE FROM ${ChannelEntity.AllChannelsEntity.TABLE_NAME} WHERE stream_id =:id")
    fun deleteMyChannels(id: Int): Completable

    @Query("DELETE FROM ${ChannelEntity.AllChannelsEntity.TABLE_NAME}")
    fun clearChannels(): Completable

    @Query("SELECT * FROM ${ChannelEntity.AllChannelsEntity.TABLE_NAME} ORDER BY channel_name ASC")
    fun getAllChannels(): Flowable<List<ChannelEntity.AllChannelsEntity>>

    @Query("SELECT * FROM ${ChannelEntity.AllChannelsEntity.TABLE_NAME} ORDER BY channel_name ASC")
    fun loadAllChannels(): Single<List<ChannelEntity.AllChannelsEntity>>

    @Query("SELECT * FROM ${ChannelEntity.AllChannelsEntity.TABLE_NAME} WHERE (NOT channel_is_topic OR (channel_is_topic AND channel_is_expanded))")
    fun getCurrentChannels(): Flowable<List<ChannelEntity.AllChannelsEntity>>

    @Query("SELECT * FROM ${ChannelEntity.AllChannelsEntity.TABLE_NAME} WHERE channel_is_topic AND channel_parent = :channel")
    fun getTopicsForChannel(channel: String): Single<List<ChannelEntity.AllChannelsEntity>>

    @Query("SELECT * FROM ${ChannelEntity.AllChannelsEntity.TABLE_NAME} WHERE ((channel_name LIKE :name) OR (channel_parent LIKE :name AND channel_is_topic))")
    fun findChannelsByName(name: String): Single<List<ChannelEntity.AllChannelsEntity>>
}

@Dao
interface MyChannelsDAO : ChannelsDAO<ChannelEntity.MyChannelsEntity> {


    @Query("DELETE FROM ${ChannelEntity.MyChannelsEntity.TABLE_NAME} WHERE stream_id =:id")
    fun deleteMyChannels(id: Int): Completable

    @Query("DELETE FROM ${ChannelEntity.MyChannelsEntity.TABLE_NAME}")
    fun clearChannels(): Completable

    @Query("SELECT * FROM ${ChannelEntity.MyChannelsEntity.TABLE_NAME} WHERE channel_is_my ORDER BY channel_name ASC")
    fun getMyAllChannels(): Flowable<List<ChannelEntity.MyChannelsEntity>>

    @Query("SELECT * FROM ${ChannelEntity.MyChannelsEntity.TABLE_NAME} WHERE channel_is_my ORDER BY channel_name ASC")
    fun loadMyAllChannels(): Single<List<ChannelEntity.MyChannelsEntity>>

    @Query("SELECT * FROM ${ChannelEntity.MyChannelsEntity.TABLE_NAME} WHERE (NOT channel_is_topic OR (channel_is_topic AND channel_is_expanded))")
    fun getCurrentChannels(): Flowable<List<ChannelEntity.MyChannelsEntity>>

    @Query("SELECT * FROM ${ChannelEntity.MyChannelsEntity.TABLE_NAME} WHERE (NOT channel_is_topic OR (channel_is_topic AND channel_is_expanded)) AND channel_is_my")
    fun getMyCurrentChannels(): Flowable<List<ChannelEntity.MyChannelsEntity>>

    @Query("SELECT * FROM ${ChannelEntity.MyChannelsEntity.TABLE_NAME} WHERE channel_is_topic AND channel_parent = :channel")
    fun getTopicsForChannel(channel: String): Single<List<ChannelEntity.MyChannelsEntity>>

    @Query("SELECT * FROM ${ChannelEntity.MyChannelsEntity.TABLE_NAME} WHERE ((channel_name LIKE :name) OR (channel_parent LIKE :name AND channel_is_topic))")
    fun findChannelsByName(name: String): Single<List<ChannelEntity.MyChannelsEntity>>
}



