package ai.atick.coronago

import android.content.Context
import android.os.Handler
import androidx.work.*
import java.util.concurrent.TimeUnit

class WorkActivity(val context: Context) {

    private val key: Key = Key()
    ///////////////////////////////////////////////////////////////
    fun createPeriodicTasks() {
        val trackingWork = PeriodicWorkRequestBuilder<TrackingWork>(
            key.locationUpdateInterval, TimeUnit.MINUTES
        ).build()
        //////////////////////////////////////////////////////////////
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            key.locationTaskId,
            ExistingPeriodicWorkPolicy.REPLACE,
            trackingWork
        )
        /////////////////////////////////////////////////
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .setRequiresBatteryNotLow(false)
            .build()
        //////////////////////////////////////////////////////////////////////////
        val uploadWork = PeriodicWorkRequestBuilder<UploadWork>(key.uploadInterval, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        //////////////////////////////////////////////////////////////////
        Handler().postDelayed({
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                key.uploadTaskId,
                ExistingPeriodicWorkPolicy.REPLACE,
                uploadWork
            )
        }, 1000)
    }
}