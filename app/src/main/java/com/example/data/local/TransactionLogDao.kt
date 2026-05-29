package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.data.model.TransactionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionLogDao {
    @Query("SELECT * FROM transaction_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<TransactionLog>>

    @Query("SELECT * FROM transaction_logs WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedLogs(): Flow<List<TransactionLog>>

    @Insert
    suspend fun insertLog(log: TransactionLog)

    @Query("UPDATE transaction_logs SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllAsSynced()

    @Query("DELETE FROM transaction_logs WHERE isSynced = 1")
    suspend fun clearSyncedLogs()

    @Query("DELETE FROM transaction_logs")
    suspend fun clearAllLogs()
}