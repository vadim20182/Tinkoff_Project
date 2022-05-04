package android.example.tinkoffproject.network

import android.example.tinkoffproject.contacts.data.network.ContactItem
import android.example.tinkoffproject.contacts.data.network.GetPresenceResponse
import android.example.tinkoffproject.contacts.data.network.UserResponse
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import okhttp3.Interceptor
import org.json.JSONArray

object NetworkCommon {
    const val API_KEY = "NIR3wit9SYwt4O5VuXghNRfwXfqADy41"
    const val EMAIL = "buzka16@ya.ru"
    const val BASE_URL = "https://tinkoff-android-spring-2022.zulipchat.com/api/v1/"
    const val MY_USER_ID = 491496

    val jsonParser = Json { ignoreUnknownKeys = true }

    fun getUserObservable(
        publishSubject: PublishSubject<Int>,
        client: ApiService
    ): Observable<UserResponse> =
        publishSubject
            .observeOn(Schedulers.io())
            .flatMapSingle {
                client.getUser(it)
            }

    fun getUserPresenceForProfileObservable(
        publishSubject: PublishSubject<ContactItem>,
        client: ApiService
    ): Observable<GetPresenceResponse> =
        publishSubject
            .observeOn(Schedulers.io())
            .flatMapSingle {
                client.getUserPresence(it.email)
            }

    fun makeJSONArray(list: List<Pair<String, String>>): JSONArray {
        var s = "["
        for (p in list)
            s += "{operator: \"${p.first}\", operand: \"${p.second}\"},"
        s = s.substring(0, s.length - 1)
        s += "]"
        return JSONArray(s)
    }

    class BasicAuthInterceptor(email: String, apiKey: String) : Interceptor {
        private var credentials: String = Credentials.basic(email, apiKey)

        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            var request = chain.request()
            request = request.newBuilder().header("Authorization", credentials).build()
            return chain.proceed(request)
        }
    }
}
