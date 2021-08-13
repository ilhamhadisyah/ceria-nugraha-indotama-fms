package org.traccar.client.data.source.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object APIClient {

    private const val BASE_URL = "https://fms.antarejasinergi.com/api/"

    private fun client(token: String): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(OAuthInterceptor("Bearer", token))
            .build()
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private fun getRetrofit(token: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client(token))
            .build()
    }

    fun apiService(token: String): APIService = getRetrofit(token).create(APIService::class.java)
    fun apiService(): APIService = getRetrofit().create(APIService::class.java)
}