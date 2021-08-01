package org.traccar.client.data.source.retrofit


class APIHelper(private val apiService: APIService) {
    suspend fun getLoginToken(email: String, password: String, device: String) =
        apiService.getToken(email, password, device)
}