package org.traccar.client.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import org.traccar.client.R
import org.traccar.client.data.source.retrofit.APIClient
import org.traccar.client.data.source.retrofit.APIHelper
import org.traccar.client.databinding.ActivityUserAuthBinding
import org.traccar.client.ui.viewmodel.AuthViewModel
import org.traccar.client.ui.viewmodel.ViewModelFactory
import org.traccar.client.utils.AppPreferenceManager
import org.traccar.client.utils.PreferenceKey
import org.traccar.client.utils.networkutils.Status


class UserAuthActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var viewModel: AuthViewModel
    private lateinit var binding: ActivityUserAuthBinding
    private lateinit var tm: TelephonyManager
    private var deviceId: String = ""

    private lateinit var preferences: SharedPreferences

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = this.getSharedPreferences(PreferenceKey.MAIN_PREFERENCE,Context.MODE_PRIVATE)
        val isLogin = preferences.getBoolean(PreferenceKey.IS_LOGIN,false)
        if (isLogin) {
            navigateToDashboard()
        } else {
            viewModel =
                ViewModelProviders.of(this,
                    ViewModelFactory(
                        APIHelper(
                            APIClient.apiService
                        )
                    )
                ).get(AuthViewModel::class.java)

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
            tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

            Toast.makeText(this, deviceId, Toast.LENGTH_SHORT).show()
        }

    }

    private fun makeSnackBar(message: String) {
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.red))
            .setTextColor(resources.getColor(R.color.light))

        snackBar.show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.login_btn -> {
//                val imm: InputMethodManager =
//                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//                imm.hideSoftInputFromWindow(v.windowToken, 0)

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
                                            with(preferences.edit()){
                                                putString(PreferenceKey.TOKEN,resources.data?.token)
                                                putBoolean(PreferenceKey.IS_LOGIN,true)
                                                commit()
                                            }
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
        startActivity(mainActivity)
    }

}