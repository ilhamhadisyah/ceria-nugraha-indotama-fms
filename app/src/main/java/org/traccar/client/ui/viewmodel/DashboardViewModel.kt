package org.traccar.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import org.traccar.client.data.source.NetworkRepository
import org.traccar.client.utils.networkutils.Resources
import java.lang.Exception

class DashboardViewModel(private val networkRepository: NetworkRepository) : ViewModel() {

    var recentTime: Long = 0L

    var parentSessionNumber: Int = 0
    var rain: Int = 0
    var rest: Int = 0
    var slippery: Int = 0
    var eat: Int = 0
    var noOperator: Int = 0
    var refueling: Int = 0
    var briefing: Int = 0
    var others: Int = 0
    var pray: Int = 0
    var breakDown: Int = 0
    var shiftChange: Int = 0


    fun postActivity(
        imei: String,
        session_parent_number: Int,
        session_child_number: Int,
        activity_type: String,
        loading_material: String,
        action: String,
        lat: Double,
        long: Double,
        created_at: String
    ) = liveData(Dispatchers.IO) {
        emit(Resources.loading(data = null))
        try {
            emit(
                Resources.success(
                    data = networkRepository.pushActivity(
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
                )
            )
        } catch (e: Exception) {
            emit(
                Resources.error(
                    message = e.message,
                    data = null
                )
            )
        }

    }

    fun postActivity(
        imei: String,
        session_parent_number: Int,
        session_child_number: Int,
        activity_type: String,
        action: String,
        lat: Double,
        long: Double,
        created_at: String
    ) = liveData(Dispatchers.IO) {
        emit(Resources.loading(data = null))
        try {
            emit(
                Resources.success(
                    data = networkRepository.pushActivity(
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
                )
            )
        } catch (e: Exception) {
            emit(
                Resources.error(
                    message = e.message,
                    data = null
                )
            )
        }

    }


}