package ai.atick.coronago

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONArray
import org.json.JSONObject

class UploadWork(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    private val networkActivity: NetworkActivity = NetworkActivity(context)
    private val database: AppDatabase = AppDatabase(context)
    private var locationUrl: String = "http://home.jamiussiam.com:8090/location"

    override fun doWork(): Result {
        val latitudeList = database.getListString("locationList")
        val longitudeList = database.getListString("longitudeList")
        val timestampList = database.getListString("timestampList")

        val dataArray = JSONArray()

        timestampList.forEachIndexed { index, timestamp ->
            val jsonObject = networkActivity.locationDataObject(
                latitude = latitudeList[index],
                longitude = longitudeList[index],
                timeStamp = timestamp
            )
            dataArray.put(jsonObject)
        }

        Log.d("corona", dataArray.toString())

        networkActivity.postLocationData(locationUrl, dataArray)

        return Result.success()
    }
}