package org.traccar.client.utils

import android.content.Context
import android.content.SharedPreferences
import org.traccar.client.MainFragment
import org.traccar.client.utils.PreferenceKey.CHILD_SESSION
import org.traccar.client.utils.PreferenceKey.IS_LOGIN
import org.traccar.client.utils.PreferenceKey.PARENT_SESSION
import org.traccar.client.utils.PreferenceKey.RUNNING
import org.traccar.client.utils.PreferenceKey.SECONDS
import org.traccar.client.utils.PreferenceKey.TOKEN
import org.traccar.client.utils.PreferenceKey.WAS_RUNNING

class AppPreferencesManager(context: Context) {
    private var preferences: SharedPreferences =
        context.getSharedPreferences(PreferenceKey.MAIN_PREFERENCE, Context.MODE_PRIVATE)

    var isLogin
        get() = preferences.getBoolean(IS_LOGIN, false)
        set(value) = preferences.edit().putBoolean(IS_LOGIN, value).apply()

    var token
        get() = preferences.getString(TOKEN, null)
        set(value) = preferences.edit().putString(TOKEN, value).apply()

    var parentSessionNumber
        get() = preferences.getInt(PARENT_SESSION, 0)
        set(value) = preferences.edit().putInt(PARENT_SESSION, value).apply()

    var childSessionNumber
        get() = preferences.getInt(CHILD_SESSION, 0)
        set(value) = preferences.edit().putInt(CHILD_SESSION, value).apply()

    var keyStatus
        get() = preferences.getBoolean(MainFragment.KEY_STATUS, false)
        set(value) = preferences.edit().putBoolean(MainFragment.KEY_STATUS, value).apply()


    //timer service
    var seconds
        get() = preferences.getInt(SECONDS, 0)
        set(value) = preferences.edit().putInt(SECONDS, value).apply()

    var running
        get() = preferences.getBoolean(RUNNING, false)
        set(value) = preferences.edit().putBoolean(RUNNING, value).apply()

    var wasRunning
        get() = preferences.getBoolean(WAS_RUNNING, false)
        set(value) = preferences.edit().putBoolean(WAS_RUNNING, value).apply()

    fun getParentSession(): Int {
        val recent: Int = parentSessionNumber
        parentSessionNumber = recent + 1
        return recent
    }

    fun getChildSession(): Int {
        val recent: Int = childSessionNumber
        childSessionNumber = recent + 1
        return recent
    }

    fun resetChildSession() {
        childSessionNumber = 0
    }
}