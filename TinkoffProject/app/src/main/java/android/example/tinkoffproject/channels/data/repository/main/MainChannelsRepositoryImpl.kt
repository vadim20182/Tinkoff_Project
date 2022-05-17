package android.example.tinkoffproject.channels.data.repository.main

import android.example.tinkoffproject.channels.di.main.MainStreams
import android.example.tinkoffproject.network.ApiService
import android.example.tinkoffproject.network.NetworkCommon.makeCreateChannelJSONArray
import io.reactivex.Single
import javax.inject.Inject

@MainStreams
class MainChannelsRepositoryImpl @Inject constructor(private val client: ApiService) :
    MainChannelsRepository {

    override fun createChannel(channelName: String, description: String?): Single<String> =
        client.createChannel(makeCreateChannelJSONArray(channelName, description))
}