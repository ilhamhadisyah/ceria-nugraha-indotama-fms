package org.traccar.client.utils

import android.content.Context
import android.content.SharedPreferences

class AppPreferenceManager(context: Context) {
    companion object{
        private const val NAME = "traccar"
        private const val MODE = Context.MODE_PRIVATE
    }
    private var pref: SharedPreferences = context.getSharedPreferences(NAME, MODE)

    private val TOKEN = Pair("token", "")
    private val IS_LOGIN = Pair("login_status", false)

    //    fun init(context: Context) {
//        pref = context.getSharedPreferences(NAME, MODE)
//    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var token: String
        get() = pref.getString(TOKEN.first, "") ?: ""
        set(value) = pref.edit {
            it.putString(TOKEN.first, value)
        }

    var isLogin: Boolean
        get() = pref.getBoolean(IS_LOGIN.first, false)
        set(value) = pref.edit {
            it.putBoolean(TOKEN.first, value)
        }
}