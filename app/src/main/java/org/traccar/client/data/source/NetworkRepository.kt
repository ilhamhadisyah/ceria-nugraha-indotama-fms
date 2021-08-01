package org.traccar.client.data.source

import org.traccar.client.data.model.Token
import org.traccar.client.data.source.retrofit.APIHelper

class NetworkRepository(private val apiHelper: APIHelper) {
    suspend fun getUserAuth(email: String, password: String, device: String) =
        apiHelper.getLoginToken(email, password, device)
}