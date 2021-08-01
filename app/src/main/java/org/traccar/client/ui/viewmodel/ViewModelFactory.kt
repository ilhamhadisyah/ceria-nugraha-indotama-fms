package org.traccar.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.traccar.client.data.source.NetworkRepository
import org.traccar.client.data.source.retrofit.APIHelper
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val apiHelper: APIHelper) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)){
            return AuthViewModel(NetworkRepository(apiHelper)) as T
        }
        throw IllegalArgumentException("Unkown Class")
    }

}