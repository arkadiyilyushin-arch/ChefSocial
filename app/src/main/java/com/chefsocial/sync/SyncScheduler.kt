package com.chefsocial.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chefsocial.util.isAutoSyncEnabled
import java.util.concurrent.TimeUnit

fun scheduleBackgroundSync(context: Context) {
    if (!isAutoSyncEnabled(context)) {
        cancelBackgroundSync(context)
        return
    }
    val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build(),
        )
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        SyncWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.UPDATE,
        request,
    )
}

fun cancelBackgroundSync(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork(SyncWorker.WORK_NAME)
}

fun updateBackgroundSync(context: Context) {
    if (isAutoSyncEnabled(context)) scheduleBackgroundSync(context) else cancelBackgroundSync(context)
}
