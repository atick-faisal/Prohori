package ai.atick.coronago

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class TrackingWork(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters)  {

    private val locationActivity: LocationActivity = LocationActivity(context)

    override fun doWork(): Result {
        locationActivity.updateLocation()
        return Result.success()
    }
}