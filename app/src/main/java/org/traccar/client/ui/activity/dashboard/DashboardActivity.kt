package org.traccar.client.ui.activity.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.Chronometer
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.preference.Preference
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.traccar.client.MainFragment
import org.traccar.client.MainFragment.Companion.KEY_STATUS
import org.traccar.client.R
import org.traccar.client.data.model.ActivityModel
import org.traccar.client.data.source.retrofit.APIClient
import org.traccar.client.data.source.retrofit.APIHelper
import org.traccar.client.data.source.sqlite.DatabaseHelper
import org.traccar.client.databinding.ActivityDashboardBinding
import org.traccar.client.services.AutostartReceiver
import org.traccar.client.services.TrackingService
import org.traccar.client.ui.activity.MainActivity
import org.traccar.client.ui.activity.MainApplication
import org.traccar.client.ui.activity.StatusActivity
import org.traccar.client.ui.viewmodel.DashboardViewModel
import org.traccar.client.ui.viewmodel.ViewModelFactory
import org.traccar.client.utils.ActivityValues
import org.traccar.client.utils.AppPreferencesManager
import org.traccar.client.utils.PreferenceKey
import org.traccar.client.utils.PreferenceKey.CHILD_SESSION
import org.traccar.client.utils.PreferenceKey.PARENT_SESSION
import org.traccar.client.utils.networkutils.Status
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class DashboardActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener,
    View.OnClickListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var preferences: AppPreferencesManager

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmIntent: PendingIntent
    private lateinit var location: FusedLocationProviderClient
    private lateinit var UTC: TimeZone
    private lateinit var dateFormatter: DateFormat
    private lateinit var timer: Chronometer
    private lateinit var viewModel: DashboardViewModel
    private lateinit var alertDialog: AlertDialog.Builder
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var cm: ConnectivityManager
    private lateinit var activeNetwork: NetworkInfo

    private var deviceId = ""
    private var loadingMaterial = ""
    private val activityValues = ActivityValues

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferencesManager(this)
        sharedPreferences =
            this.getSharedPreferences(PreferenceKey.MAIN_PREFERENCE, Context.MODE_PRIVATE)
        initView()
        alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent =
            PendingIntent.getBroadcast(this, 0, Intent(this, AutostartReceiver::class.java), 0)
        preferences.keyStatus = true
        startTrackingService(checkPermission = true, initialPermission = false)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.start_to_dumping_point -> startToDumping()
            R.id.arrive_to_dumping_point -> arriveToDumping()
            R.id.start_to_loading_point -> startToLoading()
            R.id.arrive_to_loading_point -> arriveToLoading()
            R.id.rain_btn -> {
                if (!binding.rainBtn.isSelected) {
                    viewModel.rain = preferences.getChildSession()
                    binding.rainBtn.isSelected = true
                    activityMenu(activityValues.RAIN,activityValues.START,viewModel.rain)
                } else {
                    binding.rainBtn.isSelected = false
                    activityMenu(activityValues.RAIN,activityValues.STOP,viewModel.rain)
                }
            }
            R.id.slippery_btn -> {
                if (!binding.slipperyBtn.isSelected) {
                    viewModel.slippery = preferences.getChildSession()
                    binding.slipperyBtn.isSelected = true
                    activityMenu(activityValues.SLIPPERY,activityValues.START,viewModel.slippery)

                } else {
                    binding.slipperyBtn.isSelected = false
                    activityMenu(activityValues.SLIPPERY,activityValues.STOP,viewModel.slippery)
                }
            }
            R.id.rest_btn -> {
                if (!binding.restBtn.isSelected) {
                    viewModel.rest = preferences.getChildSession()
                    binding.restBtn.isSelected = true
                    activityMenu(activityValues.REST,activityValues.START,viewModel.rest)

                } else {
                    binding.restBtn.isSelected = false
                    activityMenu(activityValues.REST,activityValues.STOP,viewModel.rest)
                }
            }
            R.id.eat_btn -> {
                if (!binding.eatBtn.isSelected) {
                    viewModel.eat = preferences.getChildSession()
                    binding.eatBtn.isSelected = true
                    activityMenu(activityValues.EAT,activityValues.START,viewModel.eat)

                } else {
                    binding.eatBtn.isSelected = false
                    activityMenu(activityValues.EAT,activityValues.STOP,viewModel.eat)
                }
            }
            R.id.pray_btn -> {
                if (!binding.prayBtn.isSelected) {
                    viewModel.pray = preferences.getChildSession()
                    binding.prayBtn.isSelected = true
                    activityMenu(activityValues.PRAY,activityValues.START,viewModel.pray)

                } else {
                    binding.prayBtn.isSelected = false
                    activityMenu(activityValues.PRAY,activityValues.STOP,viewModel.pray)
                }
            }
            R.id.no_operator_btn -> {
                if (!binding.noOperatorBtn.isSelected) {
                    viewModel.noOperator = preferences.getChildSession()
                    binding.noOperatorBtn.isSelected = true
                    activityMenu(activityValues.NO_OPERATOR,activityValues.START,viewModel.noOperator)

                } else {
                    binding.noOperatorBtn.isSelected = false
                    activityMenu(activityValues.NO_OPERATOR,activityValues.STOP,viewModel.noOperator)
                }
            }
            R.id.breakdown_btn -> {
                if (!binding.breakdownBtn.isSelected) {
                    viewModel.breakDown = preferences.getChildSession()
                    binding.breakdownBtn.isSelected = true
                    activityMenu(activityValues.BREAK_DOWN,activityValues.START,viewModel.breakDown)

                } else {
                    binding.breakdownBtn.isSelected = false
                    activityMenu(activityValues.BREAK_DOWN,activityValues.STOP,viewModel.breakDown)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun activityMenu(
        activity: String,
        action: String,
        childSession: Int
    ) {
        alertDialog = AlertDialog.Builder(this)
        alertDialog
            .setTitle("Konfirmasi aktivitas?")
            .setMessage("Tidak dapat kembali setelah service berjalan")
            .setPositiveButton("Konfirmasi") { _, _ ->
                val date = dateFormatter.format(Date())
                location.lastLocation.addOnSuccessListener(this) { location ->

                    writeActivity(
                        ActivityModel(
                            0,
                            null,
                            activity,
                            deviceId,
                            action,
                            date,
                            viewModel.parentSessionNumber,
                            childSession,
                            location.latitude,
                            location.longitude,
                            0
                        )
                    )

                    getUnPostedActivity()
                }
            }
            .setNegativeButton("batal") { dialog, _ ->
                dialog.cancel()
            }.show()
    }

    @SuppressLint("MissingPermission")
    private fun arriveToLoading() {
        preferences.resetChildSession()
        alertDialog = AlertDialog.Builder(this)
        alertDialog
            .setTitle("Konfirmasi aktivitas?")
            .setMessage("Tidak dapat kembali setelah service berjalan")
            .setPositiveButton("Konfirmasi") { _, _ ->
                val date = dateFormatter.format(Date())
                location.lastLocation.addOnSuccessListener(this) { location ->
                    writeActivity(
                        ActivityModel(
                            0,
                            loadingMaterial,
                            activityValues.LOADING_POINT,
                            deviceId,
                            activityValues.STOP,
                            date,
                            viewModel.parentSessionNumber,
                            preferences.childSessionNumber,
                            location.latitude,
                            location.longitude,
                            0
                        )
                    )
//                    writeActivity(
//                        ActivityModel(
//                            0,
//                            null,
//                            activityValues.LOADING,
//                            deviceId,
//                            "start",
//                            date,
//                            viewModel.parentSessionNumber,
//                            1,
//                            location.latitude,
//                            location.longitude,
//                            0
//                        )
//                    )
                    timer.stop()
                    viewModel.recentTime = timer.base - SystemClock.elapsedRealtime()
                    getUnPostedActivity()
                }
                mainBtnActivityVisibility(
                    startDumping = VISIBLE,
                    arriveDumping = GONE,
                    startLoading = GONE,
                    arriveLoading = GONE
                )
            }
            .setNegativeButton("batal") { dialog, _ ->
                dialog.cancel()
            }.show()
    }

    @SuppressLint("MissingPermission")
    private fun startToLoading() {
        val items = arrayOf("ob", "limonit", "saprolit")

        alertDialog = AlertDialog.Builder(this)
        alertDialog
            .setTitle("Pilih muatan barang")
            .setSingleChoiceItems(items, -1) { _, which ->
                loadingMaterial = items[which]
            }
            .setPositiveButton("Pilih") { _, _ ->
                val date = dateFormatter.format(Date())
                location.lastLocation.addOnSuccessListener(this) { location ->
                    writeActivity(
                        ActivityModel(
                            0,
                            null,
                            activityValues.DUMPING,
                            deviceId,
                            activityValues.STOP,
                            date,
                            viewModel.parentSessionNumber,
                            preferences.childSessionNumber,
                            location.latitude,
                            location.longitude,
                            0
                        )
                    )
                    writeActivity(
                        ActivityModel(
                            0,
                            loadingMaterial,
                            activityValues.LOADING_POINT,
                            deviceId,
                            activityValues.START,
                            date,
                            viewModel.parentSessionNumber,
                            preferences.childSessionNumber,
                            location.latitude,
                            location.longitude,
                            0
                        )
                    )
                    timer.base = SystemClock.elapsedRealtime() + viewModel.recentTime
                    timer.start()
                    getUnPostedActivity()
                }
                mainBtnActivityVisibility(
                    startDumping = GONE,
                    arriveDumping = GONE,
                    startLoading = GONE,
                    arriveLoading = VISIBLE
                )
            }
            .setNegativeButton("batal") { dialog, _ ->
                dialog.cancel()
            }.show()
    }

    @SuppressLint("MissingPermission")
    private fun arriveToDumping() {
        alertDialog = AlertDialog.Builder(this)
        alertDialog
            .setTitle("Konfirmasi aktivitas?")
            .setMessage("Tidak dapat kembali setelah service berjalan")
            .setPositiveButton("Konfirmasi") { _, _ ->
                val date = dateFormatter.format(Date())
                location.lastLocation.addOnSuccessListener(this) { location ->
                    writeActivity(
                        ActivityModel(
                            0,
                            null,
                            activityValues.DUMPING_POINT,
                            deviceId,
                            activityValues.STOP,
                            date,
                            viewModel.parentSessionNumber,
                            preferences.childSessionNumber,
                            location.latitude,
                            location.longitude,
                            0
                        )
                    )
                    writeActivity(
                        ActivityModel(
                            0,
                            null,
                            activityValues.DUMPING,
                            deviceId,
                            activityValues.START,
                            date,
                            viewModel.parentSessionNumber,
                            preferences.childSessionNumber,
                            location.latitude,
                            location.longitude,
                            0
                        )
                    )

                    timer.stop()
                    viewModel.recentTime = timer.base - SystemClock.elapsedRealtime()
                    getUnPostedActivity()
                }
                mainBtnActivityVisibility(
                    startDumping = GONE,
                    arriveDumping = GONE,
                    startLoading = VISIBLE,
                    arriveLoading = GONE
                )
            }
            .setNegativeButton("batal") { dialog, _ ->
                dialog.cancel()
            }.show()
    }

    @SuppressLint("MissingPermission")
    private fun startToDumping() {
        viewModel.parentSessionNumber = preferences.getParentSession()
        setDateTime(FULL_DATE_FORMAT)
        alertDialog = AlertDialog.Builder(this)
        alertDialog
            .setTitle("Konfirmasi aktivitas?")
            .setMessage("Tidak dapat kembali setelah service berjalan")
            .setPositiveButton("Konfirmasi") { _, _ ->
                val date = dateFormatter.format(Date())
                location.lastLocation.addOnSuccessListener(this) { location ->
                    writeActivity(
                        ActivityModel(
                            0,
                            null,
                            activityValues.DUMPING_POINT,
                            deviceId,
                            activityValues.START,
                            date,
                            viewModel.parentSessionNumber,
                            preferences.childSessionNumber,
                            location.latitude,
                            location.longitude,
                            0
                        )
                    )
                    timer.base = SystemClock.elapsedRealtime()
                    timer.start()
                    getUnPostedActivity()
                }
                mainBtnActivityVisibility(
                    startDumping = GONE,
                    arriveDumping = VISIBLE,
                    startLoading = GONE,
                    arriveLoading = GONE
                )
            }
            .setNegativeButton("batal") { dialog, _ ->
                dialog.cancel()
            }.show()
    }

    private fun mainBtnActivityVisibility(
        startDumping: Int,
        arriveDumping: Int,
        startLoading: Int,
        arriveLoading: Int
    ) {
        binding.apply {
            startToDumpingPoint.visibility = startDumping
            arriveToDumpingPoint.visibility = arriveDumping
            startToLoadingPoint.visibility = startLoading
            arriveToLoadingPoint.visibility = arriveLoading
        }
    }

    @SuppressLint("HardwareIds")
    private fun initView() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        databaseHelper = DatabaseHelper(this)
        cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        activeNetwork = cm.activeNetworkInfo!!

        supportActionBar?.apply {
            setDisplayShowCustomEnabled(true)
            setCustomView(R.layout.custom_toolbar)
        }
        location = LocationServices.getFusedLocationProviderClient(this)

        val rawToken = preferences.token
        val token = rawToken?.substringAfter('|')

        viewModel =
            ViewModelProviders.of(
                this,
                ViewModelFactory(
                    APIHelper(
                        APIClient.apiService(token!!)
                    )
                )
            ).get(DashboardViewModel::class.java)

        deviceId = android.provider.Settings.Secure.getString(
            this.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )

        setDateTime(SHORT_DATE_FORMAT)
        val date = dateFormatter.format(Date())
        binding.dateTv.text = date

        mainBtnActivityVisibility(
            startDumping = VISIBLE,
            arriveDumping = GONE,
            startLoading = GONE,
            arriveLoading = GONE
        )
        binding.apply {
            startToDumpingPoint.setOnClickListener(this@DashboardActivity)
            arriveToDumpingPoint.setOnClickListener(this@DashboardActivity)
            startToLoadingPoint.setOnClickListener(this@DashboardActivity)
            arriveToLoadingPoint.setOnClickListener(this@DashboardActivity)
            rainBtn.setOnClickListener(this@DashboardActivity)
            slipperyBtn.setOnClickListener(this@DashboardActivity)
            restBtn.setOnClickListener(this@DashboardActivity)
            eatBtn.setOnClickListener(this@DashboardActivity)
            prayBtn.setOnClickListener(this@DashboardActivity)
            noOperatorBtn.setOnClickListener(this@DashboardActivity)
            breakdownBtn.setOnClickListener(this@DashboardActivity)
        }
        timer = findViewById(R.id.timer_tv)
    }


    private fun setDateTime(format: String) {
        UTC = TimeZone.getTimeZone("GMT")
        dateFormatter = SimpleDateFormat(format, Locale.US)
        dateFormatter.timeZone = UTC
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


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == KEY_STATUS) {
            if (preferences.keyStatus) {
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
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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
            preferences.keyStatus = false
//            val preference = findPreference<TwoStatePreference>(MainFragment.KEY_STATUS)
//            preference?.isChecked = false
        }
    }

    private fun stopTrackingService() {
        alarmManager.cancel(alarmIntent)
        this.stopService(Intent(this, TrackingService::class.java))
        //setPreferencesEnabled(true)
    }

    private fun writeActivity(position: ActivityModel) {
        databaseHelper.insertActivityAsync(position, object :
            DatabaseHelper.DatabaseHandler<Unit?> {
            override fun onComplete(success: Boolean, result: Unit?) {
                if (success) {
                    Toast.makeText(this@DashboardActivity, "write success", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    private fun updateActivity(position: ActivityModel) {
        databaseHelper.updateActivityAsync(position, object :
            DatabaseHelper.DatabaseHandler<Unit?> {
            override fun onComplete(success: Boolean, result: Unit?) {
                if (success) {
                    Toast.makeText(this@DashboardActivity, "updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this@DashboardActivity,
                        "update unsuccessful",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun getUnPostedActivity() {
        databaseHelper.getActivitiesQueueAsync(object :
            DatabaseHelper.DatabaseHandler<ArrayList<ActivityModel>?> {
            override fun onComplete(success: Boolean, result: ArrayList<ActivityModel>?) {
                if (success) {
                    checkDatabaseUpdate(result)
                }
            }
        })
    }

    private fun checkDatabaseUpdate(data: ArrayList<ActivityModel>?) {
        val isConnected: Boolean = activeNetwork.isConnectedOrConnecting
        if (isConnected) {
            if (data?.isNotEmpty()!!) {
                for (n in data.indices) {
                    val actData = data[n]
                    if (!data[n].loadingMaterial.isNullOrEmpty()) {
                        viewModel.postActivity(
                            actData.imei!!,
                            actData.sessionParentNumber!!,
                            actData.sessionChildNumber!!,
                            actData.activityType!!,
                            actData.loadingMaterial!!,
                            actData.action!!,
                            actData.lat!!,
                            actData.long!!,
                            actData.createdAt!!

                        ).observe(this, {
                            it.let { resource ->
                                when (resource.status) {
                                    Status.SUCCESS -> {
                                        Log.d("id ${actData.activityId}", resource.data.toString())
                                        updateActivity(data[n])
                                    }
                                    Status.ERROR -> {
                                        Log.e(
                                            "invalid given data id ${actData.activityId}",
                                            resource.message!!
                                        )
                                    }
                                    else -> {
                                    }
                                }
                            }
                        })
                    } else {
                        viewModel.postActivity(
                            actData.imei!!,
                            actData.sessionParentNumber!!,
                            actData.sessionChildNumber!!,
                            actData.activityType!!,
                            actData.action!!,
                            actData.lat!!,
                            actData.long!!,
                            actData.createdAt!!
                        ).observe(this, {
                            it.let { resource ->
                                when (resource.status) {
                                    Status.SUCCESS -> {
                                        Log.d("id ${actData.activityId}", resource.data.toString())
                                        updateActivity(data[n])

                                    }
                                    Status.ERROR -> {
                                        Log.e(
                                            "invalid given data id ${actData.activityId}",
                                            resource.message!!
                                        )
                                    }
                                    else -> {
                                    }
                                }
                            }
                        })
                    }
                }
            }
        }

    }

    companion object {
        private const val ALARM_MANAGER_INTERVAL = 15000
        private const val PERMISSIONS_REQUEST_LOCATION = 2
        private const val FULL_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'"
        private const val SHORT_DATE_FORMAT = "EEE, dd MMM yyyy"
        private const val GONE = View.GONE
        private const val VISIBLE = View.VISIBLE


    }

}