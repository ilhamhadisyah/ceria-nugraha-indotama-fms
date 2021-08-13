package org.traccar.client.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ActivityModel(

    val activityId: Int = 0,

    @field:SerializedName("loading_material")
    val loadingMaterial: String? = null,

    @field:SerializedName("activity_type")
    val activityType: String? = null,

    @field:SerializedName("imei")
    val imei: String? = null,

    @field:SerializedName("action")
    val action: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("session_parent_number")
    val sessionParentNumber: Int? = null,

    @field:SerializedName("session_child_number")
    val sessionChildNumber: Int? = null,

    @field:SerializedName("lat")
    val lat: Double? = null,

    @field:SerializedName("long")
    val long: Double? = null,

    val status: Int? = 0
) : Parcelable
