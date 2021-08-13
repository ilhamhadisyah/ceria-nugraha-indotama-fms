package org.traccar.client.data.source.retrofit

import org.traccar.client.data.model.PushResponse
import org.traccar.client.data.model.Token
import retrofit2.http.*


interface APIService {
    @FormUrlEncoded
    @POST("token")
    suspend fun getToken(
        @Field("email") email: String?,
        @Field("password") password: String?,
        @Field("device") device: String?
    ): Token

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("activities")
    suspend fun sendActivities(
        //@Header("Authorization") token : String,
        @Field("imei") imei : String,
        @Field("session_parent_number") session_parent_number: Int,
        @Field("session_child_number") session_child_number: Int,
        @Field("activity_type") activity_type: String,
        @Field("loading_material") loading_material: String,
        @Field("action") action: String,
        @Field("lat") lat: Double,
        @Field("long") long: Double,
        @Field("created_at") created_at: String
    ): PushResponse

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("activities")
    suspend fun sendActivitiesWithoutMaterialType(
        //@Header("Authorization") token : String,
        @Field("imei") imei : String,
        @Field("session_parent_number") session_parent_number: Int,
        @Field("session_child_number") session_child_number: Int,
        @Field("activity_type") activity_type: String,
        @Field("action") action: String,
        @Field("lat") lat: Double,
        @Field("long") long: Double,
        @Field("created_at") created_at: String
    ): PushResponse
}