package com.matriarch.ai.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import com.matriarch.ai.BuildConfig

data class SessionStartRequest(
    val call_type: String = "default",
    val call_id: String
)

data class SessionStartResponse(
    val session_id: String,
    val call_id: String,
    val session_started_at: String?
)

interface MatriarchApiService {
    @POST("/sessions")
    suspend fun startSession(@Body request: SessionStartRequest): SessionStartResponse
}

object MatriarchNetwork {
    // Azure Container Apps backend (HTTPS, no port needed)
    private const val BASE_URL = "https://ca-aimata-backend.salmonmushroom-b4b13164.eastus2.azurecontainerapps.io/"

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            // In a production app, the token should be fetched securely (e.g., EncryptedSharedPreferences)
            // after the user logs in. For now, we use a build config token.
            .header("Authorization", "Bearer ${BuildConfig.API_TOKEN}")
        chain.proceed(requestBuilder.build())
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: MatriarchApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MatriarchApiService::class.java)
    }
}
