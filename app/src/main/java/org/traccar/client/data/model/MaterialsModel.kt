package org.traccar.client.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MaterialsModel(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("params")
	val params: Params? = null,

	@field:SerializedName("status")
	val status: String? = null
) : Parcelable

@Parcelize
data class Params(

	@field:SerializedName("end_date")
	val endDate: String? = null,

	@field:SerializedName("start_date")
	val startDate: String? = null
) : Parcelable

@Parcelize
data class Material(

	@field:SerializedName("ob")
	val ob: Int? = null,

	@field:SerializedName("saprolit")
	val saprolit: Int? = null,

	@field:SerializedName("limonit")
	val limonit: Int? = null
) : Parcelable

@Parcelize
data class Data(

	@field:SerializedName("material")
	val material: Material? = null,

	@field:SerializedName("totalTrip")
	val totalTrip: Int? = null
) : Parcelable
