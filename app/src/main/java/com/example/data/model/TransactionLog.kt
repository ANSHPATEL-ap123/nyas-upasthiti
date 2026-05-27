package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_logs")
data class TransactionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: String,
    val employeeName: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val isSynced: Boolean = false,
    val encryptedPayload: String // Real AES ciphertext
)
