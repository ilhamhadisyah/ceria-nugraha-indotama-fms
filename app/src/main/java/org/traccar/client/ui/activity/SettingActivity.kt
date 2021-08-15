package org.traccar.client.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.traccar.client.R

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        supportActionBar?.setDisplayShowCustomEnabled(true)
    }
}