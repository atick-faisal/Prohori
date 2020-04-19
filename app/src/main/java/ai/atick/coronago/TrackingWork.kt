package ai.atick.coronago

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class TrackingWork(private val context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters)  {

    private val locationActivity: LocationActivity = LocationActivity(context)
    private val channelId = "101010"

    override fun doWork(): Result {
        locationActivity.updateLocation()

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Location Updated")
            .setContentText("")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(0, builder.build())
        }

        return Result.success()
    }
}