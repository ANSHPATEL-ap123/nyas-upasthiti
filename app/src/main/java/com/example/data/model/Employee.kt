package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val id: String,
    val name: String,
    val department: String,
    val f1: Float,
    val f2: Float,
    val f3: Float,
    val f4: Float,
    val f5: Float
) {
    /**
     * Calculates the Euclidean distance between this employee's face embedding
     * and a target live embedding score.
     */
    fun calculateDistance(target: FloatArray): Float {
        if (target.size < 5) return 999.0f
        val diff1 = f1 - target[0]
        val diff2 = f2 - target[1]
        val diff3 = f3 - target[2]
        val diff4 = f4 - target[3]
        val diff5 = f5 - target[4]
        return kotlin.math.sqrt(diff1 * diff1 + diff2 * diff2 + diff3 * diff3 + diff4 * diff4 + diff5 * diff5)
    }
}
