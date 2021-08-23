package org.traccar.client.utils

import android.content.Context
import android.content.SharedPreferences
import org.traccar.client.MainFragment
import org.traccar.client.utils.ActivityValues.BREAK_DOWN
import org.traccar.client.utils.ActivityValues.EAT
import org.traccar.client.utils.ActivityValues.NO_OPERATOR
import org.traccar.client.utils.ActivityValues.PRAY
import org.traccar.client.utils.ActivityValues.RAIN
import org.traccar.client.utils.ActivityValues.REST
import org.traccar.client.utils.ActivityValues.SESSION_STATE
import org.traccar.client.utils.ActivityValues.SLIPPERY
import org.traccar.client.utils.PreferenceKey.CHILD_SESSION
import org.traccar.client.utils.PreferenceKey.IS_LOGIN
import org.traccar.client.utils.PreferenceKey.PARENT_SESSION
import org.traccar.client.utils.PreferenceKey.PASSWORD
import org.traccar.client.utils.PreferenceKey.RUNNING
import org.traccar.client.utils.PreferenceKey.SECONDS
import org.traccar.client.utils.PreferenceKey.TOKEN
import org.traccar.client.utils.PreferenceKey.USERNAME
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

    var username
        get() = preferences.getString(USERNAME, null)
        set(value) = preferences.edit().putString(USERNAME, value).apply()

    var password
        get() = preferences.getString(PASSWORD, null)
        set(value) = preferences.edit().putString(PASSWORD, value).apply()

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


    var rain
        get() = preferences.getBoolean(RAIN, false)
        set(value) = preferences.edit().putBoolean(RAIN, value).apply()
    var rest
        get() = preferences.getBoolean(REST, false)
        set(value) = preferences.edit().putBoolean(REST, value).apply()
    var slippery
        get() = preferences.getBoolean(SLIPPERY, false)
        set(value) = preferences.edit().putBoolean(SLIPPERY, value).apply()
    var eat
        get() = preferences.getBoolean(EAT, false)
        set(value) = preferences.edit().putBoolean(EAT, value).apply()
    var noOperator
        get() = preferences.getBoolean(NO_OPERATOR, false)
        set(value) = preferences.edit().putBoolean(NO_OPERATOR, value).apply()
    var breakDown
        get() = preferences.getBoolean(BREAK_DOWN, false)
        set(value) = preferences.edit().putBoolean(BREAK_DOWN, value).apply()
    var pray
        get() = preferences.getBoolean(PRAY, false)
        set(value) = preferences.edit().putBoolean(PRAY, value).apply()
    var sessionState
        get() = preferences.getInt(SESSION_STATE, 0)
        set(value) = preferences.edit().putInt(SESSION_STATE, value).apply()

}