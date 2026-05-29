package com.example.data.repository

import com.example.data.local.EmployeeDao
import com.example.data.local.TransactionLogDao
import com.example.data.model.Employee
import com.example.data.model.TransactionLog
import kotlinx.coroutines.flow.Flow

class AttendanceRepository(
    private val employeeDao: EmployeeDao,
    private val logDao: TransactionLogDao
) {
    val allEmployees: Flow<List<Employee>> = employeeDao.getAllEmployees()
    val allLogs: Flow<List<TransactionLog>> = logDao.getAllLogs()
    val unsyncedLogs: Flow<List<TransactionLog>> = logDao.getUnsyncedLogs()

    suspend fun checkAndPrepopulate() {
        // First run pe mock data load karega
        if (employeeDao.getEmployeeCount() == 0) {
            val dummyWorkers = listOf(
                Employee("EMP101", "Ansh Patel", "NHAI-DEL-MUM-01", 0.15f, 0.45f, 0.15f, 0.45f, 0.25f),
                Employee("EMP102", "Priya Sharma", "NHAI-DEL-MUM-02", 0.5f, 0.5f, 0.5f, 0.5f, 0.5f),
                Employee("EMP103", "Rajesh Patel", "NHAI-UP-CORRIDOR", 0.2f, 0.8f, 0.2f, 0.4f, 0.6f),
                Employee("EMP104", "Sunita Rao", "NHAI-SOUTH-HIGHWAY", 0.7f, 0.3f, 0.6f, 0.2f, 0.9f)
            )
            employeeDao.insertAll(dummyWorkers)
        }
    }

    suspend fun saveSecuredLog(
        employeeId: String, employeeName: String, timestamp: Long,
        latitude: Double, longitude: Double,
        livenessBlinkPassed: Boolean, livenessSmilePassed: Boolean,
        matchingDistance: Float
    ) {
        val newLog = TransactionLog(
            empId = employeeId,
            empName = employeeName,
            timestamp = timestamp,
            lat = latitude,
            lng = longitude,
            blinkPassed = livenessBlinkPassed,
            smilePassed = livenessSmilePassed,
            matchDistance = matchingDistance,
            isSynced = false // PENDING status me rahega
        )
        logDao.insertLog(newLog)
    }

    suspend fun syncLogsToServer() {
        logDao.markAllAsSynced()
    }

    suspend fun clearSyncedQueue() {
        logDao.clearSyncedLogs()
    }

    suspend fun clearAll() {
        logDao.clearAllLogs()
    }
}