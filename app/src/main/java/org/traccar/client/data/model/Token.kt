package org.traccar.client.data.model

import com.google.gson.annotations.SerializedName


data class Token(
	@field:SerializedName("token")
	val token: String? = null
)
