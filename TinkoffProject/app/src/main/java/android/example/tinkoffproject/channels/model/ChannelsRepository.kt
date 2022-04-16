package android.example.tinkoffproject.channels.model

import android.example.tinkoffproject.channels.model.db.AllChannelsDAO
import android.example.tinkoffproject.channels.model.db.ChannelEntity
import android.example.tinkoffproject.channels.model.db.ChannelsDAO
import android.example.tinkoffproject.channels.model.db.MyChannelsDAO
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class ChannelsRepository<T : ChannelEntity>(private val channelsDAO: ChannelsDAO<T>) {

    fun getChannelsFromDb() =
        (channelsDAO as AllChannelsDAO).getAllChannels()
            .subscribeOn(Schedulers.io())

    fun getMyChannelsFromDb() =
        (channelsDAO as MyChannelsDAO).getMyAllChannels()
            .subscribeOn(Schedulers.io())

    fun loadChannelsFromDb() =
        (channelsDAO as AllChannelsDAO).loadAllChannels()
            .subscribeOn(Schedulers.io())

    fun loadMyChannelsFromDb() =
        (channelsDAO as MyChannelsDAO).loadMyAllChannels()
            .subscribeOn(Schedulers.io())

    fun insertChannelsIgnore(channels: List<T>): Disposable =
        channelsDAO.insertChannelsIgnore(channels)
            .subscribeOn(Schedulers.io())
            .subscribe()
}