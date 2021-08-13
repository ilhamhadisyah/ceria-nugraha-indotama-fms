package org.traccar.client.data.source.retrofit


class APIHelper(private var apiService: APIService) {

    suspend fun getLoginToken(email: String, password: String, device: String) =
        apiService.getToken(email, password, device)

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
    ) = apiService.sendActivities(
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
    ) = apiService.sendActivitiesWithoutMaterialType(
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