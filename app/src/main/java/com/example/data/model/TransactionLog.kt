package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_logs")
data class TransactionLog(
    @PrimaryKey(autoGenerate = true) val logId: Int = 0,
    val empId: String,
    val empName: String,
    val timestamp: Long,
    val lat: Double,
    val lng: Double,
    val blinkPassed: Boolean,
    val smilePassed: Boolean,
    val matchDistance: Float,
    val isSynced: Boolean = false // Sync flag for AWS queue
)