package ai.atick.coronago

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class TrackingWork(private val context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters)  {

    private val locationActivity: LocationActivity = LocationActivity(context)
    private val key: Key = Key()

    override fun doWork(): Result {
        locationActivity.updateLocation()

        val builder = NotificationCompat.Builder(context, key.locationChannelId)
            .setSmallIcon(R.drawable.location)
            .setContentTitle("Location Updated")
            .setContentText("Your location was recorded by the Prohori app")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(0, builder.build())
        }

        return Result.success()
    }
}