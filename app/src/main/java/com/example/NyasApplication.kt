package com.example

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.SyncWorker
import java.util.concurrent.TimeUnit

/**
 * NyasApplication — Custom Application class that enqueues the
 * WorkManager periodic sync task on app startup.
 *
 * The SyncWorker will only execute when the device has an active
 * network connection (NetworkType.CONNECTED). This ensures offline
 * attendance logs sync to the cloud even if the app is killed.
 */
class NyasApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        enqueuePeriodicSync()
    }

    private fun enqueuePeriodicSync() {
        // Constraint: Only run when connected to the internet
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Schedule a periodic sync every 15 minutes (minimum interval for WorkManager)
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        // KEEP policy avoids duplicate enqueues if app restarts
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
