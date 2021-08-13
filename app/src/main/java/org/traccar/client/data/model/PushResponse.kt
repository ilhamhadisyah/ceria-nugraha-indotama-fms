package org.traccar.client.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PushResponse(
    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("errors")
    val errors: String? = null
) : Parcelable
