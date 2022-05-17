package android.example.tinkoffproject.channels.data.repository.main

import io.reactivex.Single

interface MainChannelsRepository {

    fun createChannel(channelName: String, description: String?): Single<String>
}