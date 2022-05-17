package android.example.tinkoffproject.network

import android.example.tinkoffproject.channels.data.network.GetChannelsResponse
import android.example.tinkoffproject.channels.data.network.GetSubscribedChannelsResponse
import android.example.tinkoffproject.channels.data.network.GetTopicsResponse
import android.example.tinkoffproject.channels.data.network.SubscriptionStatus
import android.example.tinkoffproject.chat.common.data.network.FileResponse
import android.example.tinkoffproject.chat.common.data.network.GetMessagesResponse
import android.example.tinkoffproject.chat.common.data.network.GetSingleMessageResponse
import android.example.tinkoffproject.contacts.data.network.GetPresenceResponse
import android.example.tinkoffproject.contacts.data.network.GetUsersResponse
import android.example.tinkoffproject.contacts.data.network.UserResponse
import io.reactivex.Single
import okhttp3.MultipartBody
import org.json.JSONArray
import retrofit2.http.*


interface ApiService {
    @GET("streams")
    fun getAllStreams(): Single<GetChannelsResponse>

    @GET("users/{user_id}/subscriptions/{stream_id}")
    fun getSubscriptionStatus(
        @Path("stream_id") streamId: Int,
        @Path("user_id") userID: Int
    ): Single<SubscriptionStatus>

    @GET("users/me/subscriptions")
    fun getSubscribedStreams(): Single<GetSubscribedChannelsResponse>

    @GET("users/me/{stream_id}/topics")
    fun getTopicsForStream(@Path("stream_id") streamID: Int): Single<GetTopicsResponse>

    @GET("users")
    fun getUsers(): Single<GetUsersResponse>

    @GET("users/{user_id}")
    fun getUser(@Path("user_id") userID: Int): Single<UserResponse>

    @GET("users/{user_id_or_email}/presence")
    fun getUserPresence(@Path("user_id_or_email") userIDOrEmail: String): Single<GetPresenceResponse>

    @GET("messages")
    fun getMessages(
        @Query("narrow") filter: JSONArray,
        @Query("num_before") numBefore: Int = 0,
        @Query("num_after") numAfter: Int = 0,
        @Query("anchor") anchor: String = "newest"
    ): Single<GetMessagesResponse>

    @GET("messages")
    fun getMessagesWithAnchor(
        @Query("narrow") filter: JSONArray? = null,
        @Query("num_before") numBefore: Int = 0,
        @Query("num_after") numAfter: Int = 0,
        @Query("anchor") anchor: Int
    ): Single<GetMessagesResponse>

    @GET("messages/{msg_id}")
    fun getSingleMessage(@Path("msg_id") msgID: Int): Single<GetSingleMessageResponse>

    @PATCH("messages/{msg_id}")
    fun editMessageText(
        @Path("msg_id") msgID: Int,
        @Query("content") messageText: String
    ): Single<String>

    @PATCH("messages/{msg_id}")
    fun changeMessageTopic(
        @Path("msg_id") msgID: Int,
        @Query("topic") topic: String
    ): Single<String>

    @POST("users/me/subscriptions")
    fun createChannel(
        @Query("subscriptions") filter: JSONArray,
    ): Single<String>

    @POST("messages")
    fun sendPublicMessage(
        @Query("content") message: String,
        @Query("to") to: String,
        @Query("topic") topic: String,
        @Query("type") type: String = "stream"
    ): Single<String>

    @Multipart
    @POST("user_uploads")
    fun uploadFile(
        @Part file: MultipartBody.Part
    ): Single<FileResponse>

    @POST("messages/{message_id}/reactions")
    fun addReaction(
        @Path("message_id") msgID: Int,
        @Query("emoji_name") emojiName: String,
        @Query("emoji_code") emoji_code: String,
        @Query("reaction_type") reactionType: String = "unicode_emoji"
    ): Single<String>

    @DELETE("messages/{message_id}/reactions")
    fun removeReaction(
        @Path("message_id") msgID: Int,
        @Query("emoji_name") emojiName: String,
        @Query("emoji_code") emoji_code: String,
        @Query("reaction_type") reactionType: String = "unicode_emoji"
    ): Single<String>

    @DELETE("messages/{msg_id}")
    fun deleteMessage(
        @Path("msg_id") msgID: Int
    ): Single<String>
}