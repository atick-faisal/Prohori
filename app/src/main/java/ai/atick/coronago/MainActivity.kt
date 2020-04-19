package ai.atick.coronago

import android.Manifest
import android.app.Activity
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
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var networkActivity: NetworkActivity
    private lateinit var locationActivity: LocationActivity
    private lateinit var testActivity: TestActivity
    private lateinit var database: AppDatabase
    private lateinit var workActivity: WorkActivity
    private val key: Key = Key()

    private var registered = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        networkActivity = NetworkActivity(this)
        locationActivity = LocationActivity(this)
        testActivity = TestActivity(this)
        workActivity = WorkActivity(this)
        database = AppDatabase(this)

        supportActionBar?.hide()

        registered = database.getBoolean("registered")
        if (registered) {
            registrationForm.visibility = View.GONE
            dashboard.visibility = View.VISIBLE
            userName.text = database.getString("name").substring(0, 1)
            networkActivity.getData(key.userUrl + "/${database.getString("phoneNumber")}")
        } else {
            registrationForm.visibility = View.VISIBLE
            dashboard.visibility = View.GONE
        }

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
            networkActivity.createUser(key.userUrl, userData)
            Log.d("corona", "New: ${key.birthDate}")
        }

        requestPermissions()
        createNotificationChannel(key.locationChannelId, "Location Channel")
        createNotificationChannel(key.uploadChannelId, "Upload Channel")
        workActivity.createPeriodicTasks()
    }

    private fun createNotificationChannel(id: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, channelName, importance).apply {
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
