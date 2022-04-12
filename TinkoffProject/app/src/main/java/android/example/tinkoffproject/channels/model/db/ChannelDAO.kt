package android.example.tinkoffproject.channels.model.db

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single


@Dao
interface ChannelDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannels(channels: List<ChannelEntity>): Completable

    @Query("UPDATE channel SET channel_is_my=:isMy WHERE channel_name=:name AND channel_parent=:parent")
    fun updateChannels(name: String, parent: String?, isMy: Boolean): Completable

    @Delete
    fun deleteChannels(channels: List<ChannelEntity>): Completable

    @Query("DELETE FROM channel WHERE stream_id =:id")
    fun deleteMyChannels(id: Int): Completable

    @Query("DELETE FROM channel")
    fun clearChannels(): Completable

    @Query("SELECT * FROM channel ORDER BY channel_name ASC")
    fun getAllChannels(): Single<List<ChannelEntity>>

    @Query("SELECT * FROM channel WHERE channel_is_my ORDER BY channel_name ASC")
    fun getMyAllChannels(): Single<List<ChannelEntity>>

    @Query("SELECT * FROM channel WHERE (NOT channel_is_topic OR (channel_is_topic AND channel_is_expanded))")
    fun getCurrentChannels(): Single<List<ChannelEntity>>

    @Query("SELECT * FROM channel WHERE (NOT channel_is_topic OR (channel_is_topic AND channel_is_expanded)) AND channel_is_my")
    fun getMyCurrentChannels(): Single<List<ChannelEntity>>

    @Query("SELECT * FROM channel WHERE channel_is_topic AND channel_parent = :channel")
    fun getTopicsForChannel(channel: String): Single<List<ChannelEntity>>

    @Query("SELECT * FROM channel WHERE ((channel_name LIKE :name) OR (channel_parent LIKE :name AND channel_is_topic))")
    fun findChannelsByName(name: String): Single<List<ChannelEntity>>
}