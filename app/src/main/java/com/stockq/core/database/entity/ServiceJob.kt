package com.stockq.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_jobs",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["status"]),
        Index(value = ["startDate"])
    ]
)
data class ServiceJob(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val customerId: Long? = null,
    val customerName: String,
    val customerPhone: String = "",
    val title: String,
    val description: String = "",
    val status: JobStatus = JobStatus.RECEIVED,
    val startDate: Long = System.currentTimeMillis(),
    val targetDate: Long? = null,
    val completionDate: Long? = null,
    val labourChargesPaise: Long = 0L,
    val materialChargesPaise: Long = 0L,
    val otherChargesPaise: Long = 0L,
    val directExpensesPaise: Long = 0L, // actual costs incurred by shop for this job
    val totalChargePaise: Long = 0L,
    val paidAmountPaise: Long = 0L,
    val balanceDuePaise: Long = 0L,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** Job profit = Total customer charges - Direct expenses */
    val estimatedProfitPaise: Long get() = totalChargePaise - directExpensesPaise
}

