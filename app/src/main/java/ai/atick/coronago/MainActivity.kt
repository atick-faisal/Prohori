package ai.atick.coronago

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var networkActivity: NetworkActivity
    private lateinit var locationActivity: LocationActivity
    private lateinit var testActivity: TestActivity

    private val uploadTaskId = "Location Upload"

    private var userUrl: String = "http://home.jamiussiam.com:8090/user"
    // network security config was required for http request //

    // --------- Dummy Data --------- //
    private var phone = "01711010101"
    private var gender = "OTHER"
    private var birthDate = "01-01-1969"
    private val channelId = "101010"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        networkActivity = NetworkActivity(this)
        locationActivity = LocationActivity(this)
        testActivity = TestActivity(this)

        updateButton.setOnClickListener {
            testActivity.updateLocation()
        }

        sendButton.setOnClickListener {
            testActivity.uploadLocation()
        }

        createNotificationChannel()
    }

    private fun createPeriodicTasks() {
        val trackingWork = PeriodicWorkRequestBuilder<TrackingWork>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            uploadTaskId,
            ExistingPeriodicWorkPolicy.KEEP,
            trackingWork
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
