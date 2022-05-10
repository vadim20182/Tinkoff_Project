package android.example.tinkoffproject.stub

import android.example.tinkoffproject.channels.data.network.GetChannelsResponse
import android.example.tinkoffproject.channels.data.network.GetTopicsResponse
import android.example.tinkoffproject.channels.data.network.SubscriptionStatus
import android.example.tinkoffproject.chat.data.network.FileResponse
import android.example.tinkoffproject.chat.data.network.GetMessagesResponse
import android.example.tinkoffproject.contacts.data.network.GetPresenceResponse
import android.example.tinkoffproject.contacts.data.network.GetUsersResponse
import android.example.tinkoffproject.contacts.data.network.UserResponse
import android.example.tinkoffproject.network.ApiService
import io.reactivex.Single
import okhttp3.MultipartBody
import org.json.JSONArray

class ClientStub : ApiService {
    override fun createChannel(filter: JSONArray): Single<String> {
        TODO("Not yet implemented")
    }

    override fun getAllStreams(): Single<GetChannelsResponse> {
        TODO("Not yet implemented")
    }

    override fun getSubscriptionStatus(streamId: Int, userID: Int): Single<SubscriptionStatus> {
        TODO("Not yet implemented")
    }

    override fun getSubscribedStreams(): Single<GetChannelsResponse> {
        TODO("Not yet implemented")
    }

    override fun getStreamID(stream: String): Single<String> {
        TODO("Not yet implemented")
    }

    override fun getTopicsForStream(streamID: Int): Single<GetTopicsResponse> {
        TODO("Not yet implemented")
    }

    override fun getUsers(): Single<GetUsersResponse> {
        TODO("Not yet implemented")
    }

    override fun getUser(userID: Int): Single<UserResponse> {
        TODO("Not yet implemented")
    }

    override fun getUserPresence(userIDOrEmail: String): Single<GetPresenceResponse> {
        TODO("Not yet implemented")
    }

    override fun getMessages(
        filter: JSONArray,
        numBefore: Int,
        numAfter: Int,
        anchor: String
    ): Single<GetMessagesResponse> {
        TODO("Not yet implemented")
    }

    override fun getMessagesWithAnchor(
        filter: JSONArray,
        numBefore: Int,
        numAfter: Int,
        anchor: Int
    ): Single<GetMessagesResponse> {
        TODO("Not yet implemented")
    }

    override fun getSingleMessage(msgID: Int): Single<String> {
        TODO("Not yet implemented")
    }

    override fun sendPublicMessage(
        message: String,
        to: String,
        topic: String,
        type: String
    ): Single<String> {
        TODO("Not yet implemented")
    }

    override fun sendPrivateMessage(message: String, to: String, type: String): Single<String> {
        TODO("Not yet implemented")
    }

    override fun uploadFile(file: MultipartBody.Part): Single<FileResponse> {
        TODO("Not yet implemented")
    }

    override fun addReaction(
        msgID: Int,
        emojiName: String,
        emoji_code: String,
        reactionType: String
    ): Single<String> {
        TODO("Not yet implemented")
    }

    override fun removeReaction(
        msgID: Int,
        emojiName: String,
        emoji_code: String,
        reactionType: String
    ): Single<String> {
        TODO("Not yet implemented")
    }
}