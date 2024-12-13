package com.example.dailymeow


import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://api.thecatapi.com/v1/"
    private const val API_KEY = "live_0ptPHcu7yGetN90wSDjXUCMxKve0FPU04160lEw1sgknAqUy0h7fMWYthuybTO2b"

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("x-api-key", API_KEY)
                    .build()
                chain.proceed(request)
            })
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: CatApiService by lazy {
        retrofit.create(CatApiService::class.java)
    }
}

