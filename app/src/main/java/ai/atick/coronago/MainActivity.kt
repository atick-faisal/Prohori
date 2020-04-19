package ai.atick.coronago

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.work.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var networkActivity: NetworkActivity
    private lateinit var locationActivity: LocationActivity
    private lateinit var testActivity: TestActivity
    private lateinit var database: AppDatabase
    private val key: Key = Key()

    private val uploadTaskId = "Location Upload"
    private val locationUpdateInterval: Long = 15
    private val uploadInterval: Long = 30
    private var userUrl = "https://covid-callfornation.herokuapp.com/user"
    private var registered = false
    // network security config was required for http request //


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        networkActivity = NetworkActivity(this)
        locationActivity = LocationActivity(this)
        testActivity = TestActivity(this)
        database = AppDatabase(this)

        supportActionBar?.hide()

        registered = database.getBoolean("registered")
        if (registered) {
            registrationForm.visibility = View.GONE
            dashboard.visibility = View.VISIBLE
            userName.text = database.getString("name").substring(0, 1)
            networkActivity.getData(userUrl + "/${database.getString("phoneNumber")}")
        } else {
            registrationForm.visibility = View.VISIBLE
            dashboard.visibility = View.GONE
        }

        requestPermissions()

        registerButton.setOnClickListener {
            key.name = nameText.text.toString()
            key.birthDate = birthdayText.text.toString()
            key.phoneNumber = phoneText.text.toString()
            database.putString("name", key.name)
            database.putString("gender", key.gender)
            database.putString("phoneNumber", key.phoneNumber)
            database.putString("birthDate", key.birthDate)
            val userData = networkActivity.userDataObject(
                phoneNumber = key.phoneNumber,
                gender = key.gender,
                birthDate = key.birthDate
            )
            networkActivity.postData(userUrl, userData)
            Log.d("corona", "New: ${key.birthDate}")
        }

        createPeriodicTasks()
        createNotificationChannel()
    }

    private fun createPeriodicTasks() {
        val trackingWork = PeriodicWorkRequestBuilder<TrackingWork>(
            locationUpdateInterval, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            uploadTaskId,
            ExistingPeriodicWorkPolicy.KEEP,
            trackingWork
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresCharging(true)
            .build()

        val uploadWork = PeriodicWorkRequestBuilder<UploadWork>(uploadInterval, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            uploadTaskId,
            ExistingPeriodicWorkPolicy.KEEP,
            uploadWork
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(key.channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDatePickerDialog(v: View) {
        Log.d("corona", "View: $v")
        val newFragment = DatePickerFragment()
        newFragment.show(supportFragmentManager, "datePicker")
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED -> {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        0
                    )
                }
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED -> {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        0
                    )
                }
                checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED -> {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.INTERNET),
                        0
                    )
                }
                checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED -> {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_NETWORK_STATE),
                        0
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == 0) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please allow permissions...",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissions()
            }
        }
    }
}
