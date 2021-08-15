package org.traccar.client.services

import android.content.Context
import android.os.Handler
import org.traccar.client.utils.AppPreferencesManager
import java.util.*


class TimerService(context: Context, val tickListener: TimeTickListener) {

    val preferences: AppPreferencesManager = AppPreferencesManager(context)
    var time: String = ""

    init {
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                val hours: Int = preferences.seconds / 3600
                val minutes: Int = preferences.seconds % 3600 / 60
                val secs: Int = preferences.seconds % 60

                time = java.lang.String
                    .format(
                        Locale.getDefault(),
                        "%d:%02d:%02d", hours,
                        minutes, secs
                    )

                if (preferences.running) {
                    tickListener.onTick(time)
                    preferences.seconds++
                }

                handler.postDelayed(this, 1000)
            }
        })
    }


    fun start() {
        preferences.running = true
    }

    fun stop() {
        preferences.running = false
    }

    fun reset() {
        preferences.running = false
        preferences.seconds = 0
    }

    interface TimeTickListener {
        fun onTick(time: String)
    }

}