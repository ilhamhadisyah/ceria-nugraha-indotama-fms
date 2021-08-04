package org.traccar.client.ui.activity

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.preference.TwoStatePreference
import org.traccar.client.MainFragment
import org.traccar.client.MainFragment.Companion.KEY_STATUS
import org.traccar.client.R
import org.traccar.client.databinding.ActivityDashboardBinding
import org.traccar.client.services.AutostartReceiver
import org.traccar.client.services.TrackingService

class DashboardActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_dashboard)

        supportActionBar?.apply {
            setDisplayShowCustomEnabled(true)
            setCustomView(R.layout.custom_toolbar)
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = PendingIntent.getBroadcast(this, 0, Intent(this, AutostartReceiver::class.java), 0)


        sharedPreferences.edit().putBoolean(KEY_STATUS,true).apply()
        if (sharedPreferences.getBoolean(KEY_STATUS, false)) {
            startTrackingService(checkPermission = true, initialPermission = false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.setting -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.status -> {
                val intent = Intent(this, StatusActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }


    companion object {
        private const val ALARM_MANAGER_INTERVAL = 15000
        private const val PERMISSIONS_REQUEST_LOCATION = 2
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == KEY_STATUS) {
            if (sharedPreferences!!.getBoolean(KEY_STATUS, false)) {
                startTrackingService(checkPermission = true, initialPermission = false)
            } else {
                stopTrackingService()
            }
            (this.application as MainApplication).handleRatingFlow(this)
        }
    }
    private fun startTrackingService(checkPermission: Boolean, initialPermission: Boolean) {
        var permission = initialPermission
        if (checkPermission) {
            val requiredPermissions: MutableSet<String> = HashSet()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            permission = requiredPermissions.isEmpty()
            if (!permission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        requiredPermissions.toTypedArray(),
                        PERMISSIONS_REQUEST_LOCATION
                    )
                }
                return
            }
        }
        if (permission) {
            //setPreferencesEnabled(false)
            ContextCompat.startForegroundService(this, Intent(this, TrackingService::class.java))
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                ALARM_MANAGER_INTERVAL.toLong(), ALARM_MANAGER_INTERVAL.toLong(), alarmIntent
            )
        } else {
            sharedPreferences.edit().putBoolean(KEY_STATUS, false).apply()
//            val preference = findPreference<TwoStatePreference>(MainFragment.KEY_STATUS)
//            preference?.isChecked = false
        }
    }
    private fun stopTrackingService() {
        alarmManager.cancel(alarmIntent)
        this.stopService(Intent(this, TrackingService::class.java))
        //setPreferencesEnabled(true)
    }
}