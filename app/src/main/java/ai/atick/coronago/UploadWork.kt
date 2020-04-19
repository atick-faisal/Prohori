package ai.atick.coronago

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONArray

class UploadWork(private val context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    private val networkActivity: NetworkActivity = NetworkActivity(context)
    private val database: AppDatabase = AppDatabase(context)
    private var locationUrl: String = "https://covid-callfornation.herokuapp.com/location"
    private val channelId = "101010"

    override fun doWork(): Result {
        val phoneNumber = database.getString("phoneNumber")
        val latitudeList = database.getListString("latitudeList")
        val longitudeList = database.getListString("longitudeList")
        val timestampList = database.getListString("timestampList")

        val locationArray = JSONArray()
        timestampList.forEachIndexed { index, timestamp ->
            val locationObject = networkActivity.locationObject(
                latitude = latitudeList[index],
                longitude = longitudeList[index],
                timeStamp = timestamp
            )
            locationArray.put(locationObject)
        }
        val locationDataObject = networkActivity.locationDataObject(
            phoneNumber = phoneNumber,
            locationArray = locationArray
        )

        Log.d("corona", "My Data: $locationDataObject")

        networkActivity.postData(locationUrl, locationDataObject)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Location Uploaded")
            .setContentText("${timestampList.size} Locations Uploaded")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
        return Result.success()
    }
}