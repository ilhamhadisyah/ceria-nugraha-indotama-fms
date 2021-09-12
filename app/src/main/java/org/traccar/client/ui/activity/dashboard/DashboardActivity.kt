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
import android.os.*
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.google.android.gms.location.*
import org.traccar.client.R
import org.traccar.client.data.model.ActivityModel
import org.traccar.client.data.source.retrofit.APIClient
import org.traccar.client.data.source.retrofit.APIHelper
import org.traccar.client.data.source.sqlite.DatabaseHelper
import org.traccar.client.databinding.ActivityDashboardBinding
import org.traccar.client.services.AutostartReceiver
import org.traccar.client.services.TimerService
import org.traccar.client.services.TrackingService
import org.traccar.client.ui.activity.*
import org.traccar.client.ui.viewmodel.DashboardViewModel
import org.traccar.client.ui.viewmodel.ViewModelFactory
import org.traccar.client.utils.ActivityValues
import org.traccar.client.utils.AppPreferencesManager
import org.traccar.client.utils.networkutils.Status
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet


class DashboardActivity : AppCompatActivity(), View.OnClickListener, TimerService.TimeTickListener {

    private lateinit var preferences: AppPreferencesManager
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmIntent: PendingIntent
    private lateinit var location: FusedLocationProviderClient
    private lateinit var UTC: TimeZone
    private lateinit var dateFormatter: DateFormat
    private lateinit var locationRequest: LocationRequest
    private lateinit var viewModel: DashboardViewModel
    private lateinit var alertDialog: AlertDialog.Builder
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var cm: ConnectivityManager
    private lateinit var activeNetwork: NetworkInfo
    private lateinit var timer: TimerService

    private var deviceId = ""
    private var loadingMaterial = ""
    private val activityValues = ActivityValues

    override fun onResume() {
        super.onResume()
        if (preferences.running) {
            timer.start()
        }
    }

    override fun onPause() {
        super.onPause()
        timer.detach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationRequest = LocationRequest.create()
        locationRequest.apply {
            interval = 600000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        location = LocationServices.getFusedLocationProviderClient(this)

        preferences = AppPreferencesManager(this)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        timer = TimerService(this, this)

        alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent =
            PendingIntent.getBroadcast(this, 0, Intent(this, AutostartReceiver::class.java), 0)
        preferences.keyStatus = true

        startTrackingService(checkPermission = true, initialPermission = false)
        initView()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.start_to_dumping_point -> startToDumping()
            R.id.arrive_to_dumping_point -> arriveToDumping()
            R.id.start_to_loading_point -> startToLoading()
            R.id.arrive_to_loading_point -> arriveToLoading()
            R.id.rain_btn -> {
                alertDialog = AlertDialog.Builder(this)
                alertDialog
                    .setTitle("Konfirmasi aktivitas?")
                    .setMessage("Tidak dapat kembali setelah service berjalan")
                    .setPositiveButton("Konfirmasi") { _, _ ->
                        if (!binding.rainBtn.isSelected) {
                            viewModel.rain = preferences.getChildSession()
                            binding.rainBtn.isSelected = true
                            preferences.rain = true
                            timer.stop()
                            activityMenu(activityValues.RAIN, activityValues.START, viewModel.rain)
                        } else {
                            binding.rainBtn.isSelected = false
                            preferences.rain = false
                            if (preferences.sessionState == 1 || preferences.sessionState == 3) {
                                timer.start()
                            }
                            activityMenu(activityValues.RAIN, activityValues.STOP, viewModel.rain)
                        }
                    }
                    .setNegativeButton("batal") { dialog, _ ->
                        dialog.cancel()
                    }.show()
            }
            R.id.slippery_btn -> {
                alertDialog = AlertDialog.Builder(this)
                alertDialog
                    .setTitle("Konfirmasi aktivitas?")
                    .setMessage("Tidak dapat kembali setelah service berjalan")
                    .setPositiveButton("Konfirmasi") { _, _ ->
                        if (!binding.slipperyBtn.isSelected) {
                            viewModel.slippery = preferences.getChildSession()
                            binding.slipperyBtn.isSelected = true
                            timer.stop()
                            preferences.slippery = true
                            activityMenu(
                                activityValues.SLIPPERY,
                                activityValues.START,
                                viewModel.slippery
                            )

                        } else {
                            binding.slipperyBtn.isSelected = false
                            preferences.slippery = false
                            if (preferences.sessionState == 1 || preferences.sessionState == 3) {
                                timer.start()
                            }
                            activityMenu(
                                activityValues.SLIPPERY,
                                activityValues.STOP,
                                viewModel.slippery
                            )
                        }
                    }
                    .setNegativeButton("batal") { dialog, _ ->
                        dialog.cancel()
                    }.show()

            }
            R.id.rest_btn -> {
                alertDialog = AlertDialog.Builder(this)
                alertDialog
                    .setTitle("Konfirmasi aktivitas?")
                    .setMessage("Tidak dapat kembali setelah service berjalan")
                    .setPositiveButton("Konfirmasi") { _, _ ->
                        if (!binding.restBtn.isSelected) {
                            viewModel.rest = preferences.getChildSession()
                            binding.restBtn.isSelected = true
                            timer.stop()
                            preferences.rest = true
                            activityMenu(activityValues.REST, activityValues.START, viewModel.rest)

                        } else {
                            binding.restBtn.isSelected = false
                            preferences.rest = false
                            if (preferences.sessionState == 1 || preferences.sessionState == 3) {
                                timer.start()
                            }
                            activityMenu(activityValues.REST, activityValues.STOP, viewModel.rest)
                        }
                    }
                    .setNegativeButton("batal") { dialog, _ ->
                        dialog.cancel()
                    }.show()

            }
            R.id.eat_btn -> {
                alertDialog = AlertDialog.Builder(this)
                alertDialog
                    .setTitle("Konfirmasi aktivitas?")
                    .setMessage("Tidak dapat kembali setelah service berjalan")
                    .setPositiveButton("Konfirmasi") { _, _ ->
                        if (!binding.eatBtn.isSelected) {
                            viewModel.eat = preferences.getChildSession()
                            binding.eatBtn.isSelected = true
                            timer.stop()
                            preferences.eat = true
                            activityMenu(activityValues.EAT, activityValues.START, viewModel.eat)

                        } else {
                            binding.eatBtn.isSelected = false
                            preferences.eat = false
                            if (preferences.sessionState == 1 || preferences.sessionState == 3) {
                                timer.start()
                            }
                            activityMenu(activityValues.EAT, activityValues.STOP, viewModel.eat)
                        }
                    }
                    .setNegativeButton("batal") { dialog, _ ->
                        dialog.cancel()
                    }.show()

            }
            R.id.pray_btn -> {
                alertDialog = AlertDialog.Builder(this)
                alertDialog
                    .setTitle("Konfirmasi aktivitas?")
                    .setMessage("Tidak dapat kembali setelah service berjalan")
                    .setPositiveButton("Konfirmasi") { _, _ ->
                        if (!binding.prayBtn.isSelected) {
                            viewModel.pray = preferences.getChildSession()
                            binding.prayBtn.isSelected = true
                            timer.stop()
                            preferences.pray = true
                            activityMenu(activityValues.PRAY, activityValues.START, viewModel.pray)

                        } else {
                            binding.prayBtn.isSelected = false
                            preferences.pray = false
                            if (preferences.sessionState == 1 || preferences.sessionState == 3) {
                                timer.start()
                            }
                            activityMenu(activityValues.PRAY, activityValues.STOP, viewModel.pray)
                        }
                    }
                    .setNegativeButton("batal") { dialog, _ ->
                        dialog.cancel()
                    }.show()

            }
            R.id.no_operator_btn -> {
                alertDialog = AlertDialog.Builder(this)
                alertDialog
                    .setTitle("Konfirmasi aktivitas?")
                    .setMessage("Tidak dapat kembali setelah service berjalan")
                    .setPositiveButton("Konfirmasi") { _, _ ->
                        if (!binding.noOperatorBtn.isSelected) {
                            viewModel.noOperator = preferences.getChildSession()
                            binding.noOperatorBtn.isSelected = true
                            timer.stop()
                            preferences.noOperator = true
                            activityMenu(
                                activityValues.NO_OPERATOR,
                                activityValues.START,
                                viewModel.noOperator
                            )

                        } else {
                            binding.noOperatorBtn.isSelected = false
                            preferences.noOperator = false
                            if (preferences.sessionState == 1 || preferences.sessionState == 3) {
                                timer.start()
                            }
                            activityMenu(
                                activityValues.NO_OPERATOR,
                                activityValues.STOP,
                                viewModel.noOperator
                            )
                        }
                    }
                    .setNegativeButton("batal") { dialog, _ ->
                        dialog.cancel()
                    }.show()

            }
            R.id.breakdown_btn -> {
                alertDialog = AlertDialog.Builder(this)
                alertDialog
                    .setTitle("Konfirmasi aktivitas?")
                    .setMessage("Tidak dapat kembali setelah service berjalan")
                    .setPositiveButton("Konfirmasi") { _, _ ->
                        if (!binding.breakdownBtn.isSelected) {
                            viewModel.breakDown = preferences.getChildSession()
                            binding.breakdownBtn.isSelected = true
                            timer.stop()
                            preferences.breakDown = true
                            activityMenu(
                                activityValues.BREAK_DOWN,
                                activityValues.START,
                                viewModel.breakDown
                            )

                        } else {
                            binding.breakdownBtn.isSelected = false
                            preferences.breakDown = false
                            if (preferences.sessionState == 1 || preferences.sessionState == 3) {
                                timer.start()
                            }
                            activityMenu(
                                activityValues.BREAK_DOWN,
                                activityValues.STOP,
                                viewModel.breakDown
                            )
                        }
                    }
                    .setNegativeButton("batal") { dialog, _ ->
                        dialog.cancel()
                    }.show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun activityMenu(
        activity: String,
        action: String,
        childSession: Int
    ) {
        setDateTime(FULL_DATE_FORMAT)
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

    @SuppressLint("MissingPermission")
    private fun arriveToLoading() {
        preferences.resetChildSession()
        alertDialog = AlertDialog.Builder(this)
        alertDialog
            .setTitle("Konfirmasi aktivitas?")
            .setMessage("Tidak dapat kembali setelah service berjalan")
            .setPositiveButton("Konfirmasi") { _, _ ->
                viewModel.onTheWay = false
                setDateTime(FULL_DATE_FORMAT)
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
                            viewModel.childSessionNumber,
                            location.latitude,
                            location.longitude,
                            0
                        )
                    )
                    timer.stop()
                    timer.reset()
                    getUnPostedActivity()
                    mainBtnActivityVisibility(
                        startDumping = VISIBLE,
                        arriveDumping = GONE,
                        startLoading = GONE,
                        arriveLoading = GONE
                    )
                    preferences.sessionState = 4
                }
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
            .setTitle("Pilih muatan ")
            .setSingleChoiceItems(items, -1) { _, which ->
                loadingMaterial = items[which]
            }
            .setPositiveButton("Pilih") { _, _ ->
                setDateTime(FULL_DATE_FORMAT)
                val date = dateFormatter.format(Date())
                location.lastLocation.addOnSuccessListener(this) { location ->
                    viewModel.onTheWay = true
                    writeActivity(
                        ActivityModel(
                            0,
                            null,
                            activityValues.DUMPING,
                            deviceId,
                            activityValues.STOP,
                            date,
                            viewModel.parentSessionNumber,
                            viewModel.childSessionNumber,
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
                            viewModel.childSessionNumber,
                            location.latitude,
                            location.longitude,
                            0
                        )
                    )
                    timer.start()
                    getUnPostedActivity()
                    mainBtnActivityVisibility(
                        startDumping = GONE,
                        arriveDumping = GONE,
                        startLoading = GONE,
                        arriveLoading = VISIBLE
                    )
                    preferences.sessionState = 3
                }
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
                setDateTime(FULL_DATE_FORMAT)
                val date = dateFormatter.format(Date())
                location.lastLocation.addOnSuccessListener(this) { location ->
                    viewModel.onTheWay = false
                    writeActivity(
                        ActivityModel(
                            0,
                            null,
                            activityValues.DUMPING_POINT,
                            deviceId,
                            activityValues.STOP,
                            date,
                            viewModel.parentSessionNumber,
                            viewModel.childSessionNumber,
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
                            viewModel.childSessionNumber,
                            location.latitude,
                            location.longitude,
                            0
                        )
                    )

                    timer.stop()
                    getUnPostedActivity()
                    mainBtnActivityVisibility(
                        startDumping = GONE,
                        arriveDumping = GONE,
                        startLoading = VISIBLE,
                        arriveLoading = GONE
                    )
                    preferences.sessionState = 2
                }
            }
            .setNegativeButton("batal") { dialog, _ ->
                dialog.cancel()
            }.show()
    }

    @SuppressLint("MissingPermission")
    private fun startToDumping() {
        viewModel.childSessionNumber = preferences.childSessionNumber
        viewModel.parentSessionNumber = preferences.getParentSession()
        setDateTime(FULL_DATE_FORMAT)
        alertDialog = AlertDialog.Builder(this)
        alertDialog
            .setTitle("Konfirmasi aktivitas?")
            .setMessage("Tidak dapat kembali setelah service berjalan")
            .setPositiveButton("Konfirmasi") { _, _ ->
                setDateTime(FULL_DATE_FORMAT)
                val date = dateFormatter.format(Date())
                location.lastLocation.addOnSuccessListener(this) { location ->
                    viewModel.onTheWay = true
                    writeActivity(
                        ActivityModel(
                            0,
                            null,
                            activityValues.DUMPING_POINT,
                            deviceId,
                            activityValues.START,
                            date,
                            viewModel.parentSessionNumber,
                            viewModel.childSessionNumber,
                            location.latitude,
                            location.longitude,
                            0
                        )
                    )
                    timer.start()
                    getUnPostedActivity()
                    mainBtnActivityVisibility(
                        startDumping = GONE,
                        arriveDumping = VISIBLE,
                        startLoading = GONE,
                        arriveLoading = GONE
                    )
                    preferences.sessionState = 1
                }

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

    @SuppressLint("HardwareIds", "SetTextI18n")
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
        loadMaterial()

        if (preferences.eat) {
            binding.eatBtn.isSelected = true
        }
        if (preferences.pray) {
            binding.prayBtn.isSelected = true
        }
        if (preferences.rest) {
            binding.restBtn.isSelected = true
        }
        if (preferences.breakDown) {
            binding.breakdownBtn.isSelected = true
        }
        if (preferences.noOperator) {
            binding.noOperatorBtn.isSelected = true
        }
        if (preferences.rain) {
            binding.rainBtn.isSelected = true
        }
        if (preferences.slippery) {
            binding.slipperyBtn.isSelected = true
        }

        when (preferences.sessionState) {
            1 -> {
                mainBtnActivityVisibility(
                    startDumping = GONE,
                    arriveDumping = VISIBLE,
                    startLoading = GONE,
                    arriveLoading = GONE
                )
            }
            2 -> {
                mainBtnActivityVisibility(
                    startDumping = GONE,
                    arriveDumping = GONE,
                    startLoading = VISIBLE,
                    arriveLoading = GONE
                )
            }
            3 -> {
                mainBtnActivityVisibility(
                    startDumping = GONE,
                    arriveDumping = GONE,
                    startLoading = GONE,
                    arriveLoading = VISIBLE
                )
            }
            4 -> {
                mainBtnActivityVisibility(
                    startDumping = VISIBLE,
                    arriveDumping = GONE,
                    startLoading = GONE,
                    arriveLoading = GONE
                )
            }
            else -> {
                mainBtnActivityVisibility(
                    startDumping = GONE,
                    arriveDumping = GONE,
                    startLoading = VISIBLE,
                    arriveLoading = GONE
                )
            }

        }

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
            username.text = "User : ${preferences.username}"
        }
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
            R.id.activity_log -> {
                val intent = Intent(this, ActivityLogActivity::class.java)
                startActivity(intent)
                true

            }
            R.id.logout -> {
                alertDialog = AlertDialog.Builder(this)
                alertDialog
                    .setTitle("Logout")
                    .setMessage("Logout akan menghentikan tracking, pastikan logout pada saat tidak ada aktivitas")
                    .setPositiveButton("Logout") { _, _ ->
                        stopTrackingService()
                        preferences.isLogin = false
                        preferences.token = null
                        val login = Intent(this, UserAuthActivity::class.java)
                        startActivity(login)
                        finishAffinity()
                    }
                    .setNegativeButton("batal") { dialog, _ ->
                        dialog.cancel()
                    }.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun writeActivity(position: ActivityModel) {
        databaseHelper.insertActivityAsync(position, object :
            DatabaseHelper.DatabaseHandler<Unit?> {
            override fun onComplete(success: Boolean, result: Unit?) {
                Log.d("write", result.toString())
            }
        })
    }

    private fun updateActivity(position: ActivityModel) {
        databaseHelper.updateActivityAsync(position, object :
            DatabaseHelper.DatabaseHandler<Unit?> {
            override fun onComplete(success: Boolean, result: Unit?) {
                if (success) {
                    Log.d("update", result.toString())
                } else {
                    Log.e("update", result.toString())
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
            loadMaterial()
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
                            it?.let { resource ->
                                when (resource.status) {
                                    Status.SUCCESS -> {
                                        Log.d("id ${actData.activityId}", resource.data.toString())
                                        updateActivity(data[n])
                                        loadMaterial()
                                        // load material goes here
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
                                        loadMaterial()

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

    @SuppressLint("SetTextI18n")
    private fun loadMaterial() {
        setDateTime(SIMPLE_DATE_FORMAT)
        val endDate = dateFormatter.format(Date())
        val startDate = "2021-08-01"
        viewModel.getMaterialsData(startDate, endDate).observe(this, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        val data = resource.data?.data
                        binding.loadOb.text = "Load OB : ${data?.material?.ob.toString()}"
                        binding.loadLimonit.text =
                            "Load Limonit : ${data?.material?.limonit.toString()}"
                        binding.loadSaprolit.text =
                            "Load Saprolit : ${data?.material?.saprolit.toString()}"
                    }
                    Status.ERROR -> {
                        Log.e(
                            "load material failed",
                            resource.message!!
                        )
                    }
                    Status.LOADING -> {

                    }
                }
            }
        })
    }

    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 2
        private const val FULL_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'"
        private const val SHORT_DATE_FORMAT = "EEE, dd MMM yyyy"
        private const val SIMPLE_DATE_FORMAT = "yyyy-MM-dd"
        private const val GONE = View.GONE
        private const val VISIBLE = View.VISIBLE

        private const val ALARM_MANAGER_INTERVAL = 15000
        const val KEY_STATUS = "status"
    }

    override fun onTick(time: String) {
        binding.timerTv.text = time

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
            ContextCompat.startForegroundService(this, Intent(this, TrackingService::class.java))
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                ALARM_MANAGER_INTERVAL.toLong(), ALARM_MANAGER_INTERVAL.toLong(), alarmIntent
            )
        } else {
            sharedPreferences.edit().putBoolean(KEY_STATUS, false).apply()
        }
    }

    private fun stopTrackingService() {
        alarmManager.cancel(alarmIntent)
        this.stopService(Intent(this, TrackingService::class.java))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            var granted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false
                    break
                }
            }
            startTrackingService(false, granted)
        }
    }
}