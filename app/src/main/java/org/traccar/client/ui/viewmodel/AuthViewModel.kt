package org.traccar.client.ui.viewmodel

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import org.traccar.client.data.source.NetworkRepository
import org.traccar.client.utils.networkutils.Resources
import java.lang.Exception

class AuthViewModel(private val networkRepository: NetworkRepository) : ViewModel() {

    fun getAuthToken(email: String, password: String) = liveData(Dispatchers.IO) {
        emit(Resources.loading(data = null))
        try {
            emit(
                Resources.success(
                    data = networkRepository.getUserAuth(
                        email,
                        password,
                        Build.MODEL
                    )
                )
            )
        } catch (e: Exception) {
            emit(
                Resources.error(
                    message = "Authorization failed, please check your email or password",
                    data = null
                )
            )
        }
    }
}