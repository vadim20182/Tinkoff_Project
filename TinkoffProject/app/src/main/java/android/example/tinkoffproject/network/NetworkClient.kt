package android.example.tinkoffproject.network

import android.example.tinkoffproject.contacts.model.network.ContactItem
import android.example.tinkoffproject.contacts.model.network.GetPresenceResponse
import android.example.tinkoffproject.contacts.model.network.UserResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object NetworkClient {
    private const val API_KEY = "NIR3wit9SYwt4O5VuXghNRfwXfqADy41"
    private const val EMAIL = "buzka16@ya.ru"
    private const val BASE_URL = "https://tinkoff-android-spring-2022.zulipchat.com/api/v1/"
    const val MY_USER_ID = 491496

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(BasicAuthInterceptor(EMAIL, API_KEY))
        .addInterceptor(
            HttpLoggingInterceptor()
                .apply {
                    setLevel(HttpLoggingInterceptor.Level.BODY)
                })
        .build()

   private val jsonParser = Json { ignoreUnknownKeys = true }

    private val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(jsonParser.asConverterFactory("application/json".toMediaType()))
        .build()

    val client: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    fun getUserObservable(publishSubject: PublishSubject<Int>): Observable<UserResponse> =
        publishSubject
            .observeOn(Schedulers.io())
            .flatMapSingle {
                client.getUser(it)
            }

    fun getUserPresenceForProfileObservable(publishSubject: PublishSubject<ContactItem>): Observable<GetPresenceResponse> =
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

    private class BasicAuthInterceptor(email: String, apiKey: String) : Interceptor {
        private var credentials: String = Credentials.basic(email, apiKey)

        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            var request = chain.request()
            request = request.newBuilder().header("Authorization", credentials).build()
            return chain.proceed(request)
        }
    }
}
