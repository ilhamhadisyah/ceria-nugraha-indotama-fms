package org.traccar.client.data.source.retrofit

import org.traccar.client.data.model.Token
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface APIService {
    @FormUrlEncoded
    @POST("token")
    suspend fun getToken(
        @Field("email") email: String?,
        @Field("password") password: String?,
        @Field("device") device: String?
    ): Token
}