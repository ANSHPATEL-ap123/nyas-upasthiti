package com.example.data.repository

import com.example.data.local.EmployeeDao
import com.example.data.local.TransactionLogDao
import com.example.data.model.Employee
import com.example.data.model.TransactionLog
import com.example.utils.CryptoUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONObject

class AttendanceRepository(
    private val employeeDao: EmployeeDao,
    private val logDao: TransactionLogDao
) {
    val allEmployees: Flow<List<Employee>> = employeeDao.getAllEmployees()
    val allLogs: Flow<List<TransactionLog>> = logDao.getAllLogs()
    val unsyncedLogs: Flow<List<TransactionLog>> = logDao.getUnsyncedLogs()

    suspend fun getEmployeeById(id: String): Employee? {
        return employeeDao.getEmployeeById(id)
    }

    suspend fun insertEmployee(employee: Employee) {
        employeeDao.insertEmployee(employee)
    }

    /**
     * Pre-populates the database with standard field workers if it is currently empty.
     */
    suspend fun checkAndPrepopulate() {
        val existing = employeeDao.getAllEmployees().first()
        if (existing.isEmpty()) {
            val list = listOf(
                Employee("EMP101", "Amit Kumar", "Field Surveyor", 0.1f, 0.2f, 0.3f, 0.4f, 0.5f),
                Employee("EMP102", "Priya Sharma", "Site Inspector", 0.9f, 0.8f, 0.7f, 0.6f, 0.5f),
                Employee("EMP103", "Rajesh Patel", "Construction Supervisor", 0.3f, 0.5f, 0.1f, 0.9f, 0.2f),
                Employee("EMP104", "Sunita Rao", "Safety Lead", 0.5f, 0.5f, 0.5f, 0.5f, 0.5f),
                Employee("EMP105", "Vikram Singh", "Logistics Controller", 0.2f, 0.8f, 0.4f, 0.1f, 0.9f)
            )
            employeeDao.insertAll(list)
        }
    }

    /**
     * Saves a secure, AES-encrypted transaction log locally.
     */
    suspend fun saveSecuredLog(
        employeeId: String,
        employeeName: String,
        timestamp: Long,
        latitude: Double,
        longitude: Double,
        livenessBlinkPassed: Boolean,
        livenessSmilePassed: Boolean,
        matchingDistance: Float
    ) {
        // Create plaintext log payload
        val jsonPayload = JSONObject().apply {
            put("id", employeeId)
            put("name", employeeName)
            put("time", timestamp)
            put("lat", latitude)
            put("lng", longitude)
            put("blink", livenessBlinkPassed)
            put("smile", livenessSmilePassed)
            put("dist", matchingDistance)
        }.toString()

        // Encrypt using CryptoUtils
        val ciphertext = CryptoUtils.encrypt(jsonPayload)

        val log = TransactionLog(
            employeeId = employeeId,
            employeeName = employeeName,
            timestamp = timestamp,
            latitude = latitude,
            longitude = longitude,
            isSynced = false,
            encryptedPayload = ciphertext
        )
        logDao.insertLog(log)
    }

    /**
     * Decrypts a TransactionLog's payload back to a readable map of properties or JSON object.
     */
    fun decryptPayload(log: TransactionLog): JSONObject? {
        return try {
            val plaintext = CryptoUtils.decrypt(log.encryptedPayload)
            JSONObject(plaintext)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Mocks network synchronization: marks all local logs as synced, or optionally clears them.
     */
    suspend fun syncLogsToServer() {
        // Mock a brief API call delay and then mark as synced
        logDao.markAllAsSynced()
    }

    /**
     * Clears all synced logs (the queue).
     */
    suspend fun clearSyncedQueue() {
        logDao.clearSyncedLogs()
    }

    /**
     * Force purges all transaction logs.
     */
    suspend fun clearAll() {
        logDao.clearAllLogs()
    }
}
