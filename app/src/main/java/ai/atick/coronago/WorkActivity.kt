package ai.atick.coronago

import android.content.Context
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
            ExistingPeriodicWorkPolicy.KEEP,
            trackingWork
        )
        /////////////////////////////////////////////////
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresCharging(true)
            .build()
        //////////////////////////////////////////////////////////////////////////
        val uploadWork = PeriodicWorkRequestBuilder<UploadWork>(key.uploadInterval, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        //////////////////////////////////////////////////////////////////
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            key.uploadTaskId,
            ExistingPeriodicWorkPolicy.KEEP,
            uploadWork
        )
    }
}