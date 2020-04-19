package ai.atick.coronago

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class UploadActivity(private val context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    private val networkActivity: NetworkActivity = NetworkActivity(context)
    //private val locationActivity: LocationActivity = LocationActivity(context)
    private var locationUrl: String = "http://home.jamiussiam.com:8090/location"
    private val channelId = "101010"

    override fun doWork(): Result {
//        val locationData = networkActivity.locationDataObject(
//            latitude = locationActivity.latitude,
//            longitude = locationActivity.longitude,
//            timeStamp = locationActivity.getTimeStamp()
//        )
//        networkActivity.postData(
//            url = locationUrl,
//            data = locationData
//        )

        //locationActivity.updateLocation()
        val locationActivity = LocationActivity(context)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Location")
            .setContentText("Latitude: ${locationActivity.latitude}," +
                    " Longitude: ${locationActivity.longitude}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(1010, builder.build())
        }

        return Result.success()
    }
}