package org.traccar.client.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import org.traccar.client.R
import org.traccar.client.data.source.retrofit.APIClient
import org.traccar.client.data.source.retrofit.APIHelper
import org.traccar.client.databinding.ActivityUserAuthBinding
import org.traccar.client.ui.activity.dashboard.DashboardActivity
import org.traccar.client.ui.viewmodel.AuthViewModel
import org.traccar.client.ui.viewmodel.ViewModelFactory
import org.traccar.client.utils.AppPreferencesManager
import org.traccar.client.utils.PreferenceKey
import org.traccar.client.utils.networkutils.Status


class UserAuthActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var viewModel: AuthViewModel
    private lateinit var binding: ActivityUserAuthBinding
    private var deviceId: String = ""

    private lateinit var pref: AppPreferencesManager

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = AppPreferencesManager(this)
        val isLogin = pref.isLogin
        if (isLogin) {
            navigateToDashboard()
        } else {
            viewModel =
                ViewModelProviders.of(
                    this,
                    ViewModelFactory(
                        APIHelper(
                            APIClient.apiService()
                        )
                    )
                ).get(AuthViewModel::class.java)

            if (!pref.username.isNullOrEmpty()&&!pref.password.isNullOrEmpty()){
                binding.usernameEdt.setText(pref.username)
                binding.passwordEdt.setText(pref.password)
            }

            deviceId = android.provider.Settings.Secure.getString(
                this.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )

            binding.apply {
                passwordEdtLayout.error = "Password must be filled"
                passwordEdtLayout.isErrorEnabled = false
                usernameEdtLayout.error = "Username must be filled"
                usernameEdtLayout.isErrorEnabled = false

            }
            binding.loginBtn.setOnClickListener(this)
        }

    }

    private fun makeSnackBar(message: String) {
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.RED)
            .setTextColor(resources.getColor(R.color.light))

        snackBar.show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.login_btn -> {

                val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
                val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
                val email = binding.usernameEdt.text.toString().trim()
                val password = binding.passwordEdt.text.toString().trim()

                if (isConnected) {
                    when {
                        email.isEmpty() -> {
                            binding.usernameEdtLayout.isErrorEnabled = true
                        }
                        password.isEmpty() -> {
                            binding.passwordEdtLayout.isErrorEnabled = true
                        }
                        else -> {
                            binding.apply {
                                usernameEdtLayout.isErrorEnabled = false
                                passwordEdtLayout.isErrorEnabled = false
                            }
                            viewModel.getAuthToken(email, password, deviceId).observe(this, {
                                it?.let { resources ->
                                    when (resources.status) {
                                        Status.ERROR -> {
                                            makeSnackBar(resources.message.toString())
                                        }
                                        Status.LOADING -> {
                                        }
                                        Status.SUCCESS -> {
                                            pref.isLogin = true
                                            pref.token = resources.data?.token
                                            pref.username = email
                                            pref.password = password
                                            navigateToDashboard()
                                        }
                                    }
                                }
                            })
                        }
                    }
                } else {
                    makeSnackBar("No Internet")
                }

            }
        }
    }

    private fun navigateToDashboard() {
        val mainActivity =
            Intent(this, DashboardActivity::class.java)
        finishAffinity()
        startActivity(mainActivity)
    }

}