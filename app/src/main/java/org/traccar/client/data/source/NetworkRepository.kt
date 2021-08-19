package org.traccar.client.data.source

import org.traccar.client.data.source.retrofit.APIHelper

class NetworkRepository(private val apiHelper: APIHelper) {
    suspend fun getUserAuth(email: String, password: String, device: String) =
        apiHelper.getLoginToken(email, password, device)

    suspend fun getMaterialData(startDate: String, endDate: String) =
        apiHelper.getMaterialData(startDate, endDate)

    suspend fun pushActivity(
        //token: String,
        imei: String,
        session_parent_number: Int,
        session_child_number: Int,
        activity_type: String,
        loading_material: String,
        action: String,
        lat: Double,
        long: Double,
        created_at: String
    ) = apiHelper.pushActivity(
        //token,
        imei,
        session_parent_number,
        session_child_number,
        activity_type,
        loading_material,
        action,
        lat,
        long,
        created_at
    )
    suspend fun pushActivity(
        //token: String,
        imei: String,
        session_parent_number: Int,
        session_child_number: Int,
        activity_type: String,
        action: String,
        lat: Double,
        long: Double,
        created_at: String
    ) = apiHelper.pushActivity(
        //token,
        imei,
        session_parent_number,
        session_child_number,
        activity_type,
        action,
        lat,
        long,
        created_at
    )

}