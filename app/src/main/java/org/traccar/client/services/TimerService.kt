package org.traccar.client.services

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.traccar.client.utils.AppPreferencesManager
import java.sql.Time
import java.util.*


class TimerService(context: Context, val tickListener: TimeTickListener) {

    val preferences: AppPreferencesManager = AppPreferencesManager(context)
    var time: String = ""
    val handler = Handler(Looper.myLooper()!!)
    private val value = object : Runnable {
        override fun run() {

            val rawTime = preferences.seconds

            if (preferences.running) {
                preferences.seconds = rawTime + 1
            }

            val hours: Int = rawTime % 86400 / 3600
            val minutes: Int = rawTime % 86400 % 3600 / 60
            val secs: Int = rawTime % 86400 % 3600 % 60

            time = java.lang.String
                .format(
                    Locale.getDefault(),
                    "%d:%02d:%02d", hours,
                    minutes, secs
                )
            tickListener.onTick(time)
            handler.postDelayed(this, 1000)

        }
    }


    fun start() {
        handler.post(value)
        preferences.running = true
    }

    fun stop() {
        handler.removeCallbacks(value)
        preferences.running = false
    }

    fun detach() {
        handler.removeCallbacks(value)
    }

    fun reset() {
        preferences.running = false
        preferences.seconds = 0
    }

    interface TimeTickListener {
        fun onTick(time: String)
    }

}