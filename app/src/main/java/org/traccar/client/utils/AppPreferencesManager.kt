package org.traccar.client.utils

import android.content.Context
import android.content.SharedPreferences
import org.traccar.client.MainFragment
import org.traccar.client.utils.PreferenceKey.CHILD_SESSION
import org.traccar.client.utils.PreferenceKey.IS_LOGIN
import org.traccar.client.utils.PreferenceKey.PARENT_SESSION
import org.traccar.client.utils.PreferenceKey.TOKEN

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