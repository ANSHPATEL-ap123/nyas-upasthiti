package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.sqrt

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val id: String,
    val name: String,
    val projectCode: String,
    // 5D Mock Vector for offline face matching simulation
    val f1: Float, val f2: Float, val f3: Float, val f4: Float, val f5: Float,
    val role: String = "USER",
    val adminId: String = "ADM001"
) {
    // Euclidean distance calculation live camera feed se match karne ke liye
    fun calculateDistance(liveVector: FloatArray): Float {
        var sum = 0f
        val empVector = floatArrayOf(f1, f2, f3, f4, f5)
        for (i in 0..4) {
            val diff = empVector[i] - liveVector[i]
            sum += diff * diff
        }
        return sqrt(sum.toDouble()).toFloat()
    }
}