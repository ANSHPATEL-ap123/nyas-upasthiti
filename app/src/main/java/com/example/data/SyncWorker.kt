package com.example.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.repository.AttendanceRepository

/**
 * SyncWorker — CoroutineWorker that syncs offline attendance logs to the server.
 * Triggered by WorkManager ONLY when NetworkType.CONNECTED constraint is met.
 * This ensures logs sync even if the app is killed.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val repository = AttendanceRepository(db.employeeDao(), db.transactionLogDao())

            // Mark all unsynced logs as synced (simulated server push)
            repository.syncLogsToServer()

            Result.success()
        } catch (e: Exception) {
            // Retry on failure — WorkManager will re-enqueue when network is available
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "nyas_attendance_sync_worker"
    }
}
