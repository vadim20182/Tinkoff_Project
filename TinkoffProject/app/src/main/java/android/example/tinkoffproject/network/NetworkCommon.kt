package android.example.tinkoffproject.network

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

    fun makeJSONArray(list: List<Pair<String, String>>): JSONArray {
        var s = "["
        for (p in list)
            s += "{operator: \"${p.first}\", operand: \"${p.second}\"},"
        s = s.substring(0, s.length - 1)
        s += "]"
        return JSONArray(s)
    }

    fun makeCreateChannelJSONArray(channelName: String, description: String?): JSONArray {
        return JSONArray(
            if (description != null) "[{name: \"${channelName}\", description: \"${description}\"}]"
            else "[{name: \"${channelName}\"}]"
        )
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
