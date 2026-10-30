package com.stockq.data.repository

import androidx.room.withTransaction
import com.stockq.core.database.StockQDatabase
import com.stockq.core.database.entity.BusinessExpense
import com.stockq.core.database.entity.JobStatus
import com.stockq.core.database.entity.ServiceJob
import kotlinx.coroutines.flow.Flow

class ServiceJobRepository(private val database: StockQDatabase) {

    private val jobDao = database.serviceJobDao()
    private val expenseDao = database.businessExpenseDao()

    fun getAllJobs(): Flow<List<ServiceJob>> = jobDao.getAllJobs()

    fun getJobsByStatus(status: JobStatus): Flow<List<ServiceJob>> = jobDao.getJobsByStatus(status)

    fun getActiveJobsCount(): Flow<Int> = jobDao.getActiveJobsCount()

    fun getTotalPendingJobBalanceDue(): Flow<Long> = jobDao.getTotalPendingJobBalanceDue()

    fun getTotalCompletedJobRevenue(): Flow<Long> = jobDao.getTotalCompletedJobRevenue()

    fun getTotalCompletedJobExpenses(): Flow<Long> = jobDao.getTotalCompletedJobExpenses()

    suspend fun getJobById(id: Long): ServiceJob? = jobDao.getJobById(id)

    suspend fun createJob(
        customerId: Long? = null,
        customerName: String,
        customerPhone: String = "",
        title: String,
        description: String = "",
        targetDate: Long? = null,
        labourChargesPaise: Long = 0L,
        materialChargesPaise: Long = 0L,
        otherChargesPaise: Long = 0L,
        directExpensesPaise: Long = 0L,
        paidAmountPaise: Long = 0L,
        notes: String = ""
    ): Result<Long> = database.withTransaction {
        if (title.isBlank()) {
            return@withTransaction Result.failure(IllegalArgumentException("Job title cannot be empty"))
        }

        val totalCharges = labourChargesPaise + materialChargesPaise + otherChargesPaise
        val initialPaid = paidAmountPaise.coerceAtLeast(0L).coerceAtMost(totalCharges)
        val balanceDue = totalCharges - initialPaid
        val now = System.currentTimeMillis()

        val job = ServiceJob(
            customerId = customerId,
            customerName = customerName.trim().ifBlank { "Walk-in Customer" },
            customerPhone = customerPhone.trim(),
            title = title.trim(),
            description = description.trim(),
            status = JobStatus.RECEIVED,
            startDate = now,
            targetDate = targetDate,
            labourChargesPaise = labourChargesPaise,
            materialChargesPaise = materialChargesPaise,
            otherChargesPaise = otherChargesPaise,
            directExpensesPaise = directExpensesPaise,
            totalChargePaise = totalCharges,
            paidAmountPaise = initialPaid,
            balanceDuePaise = balanceDue,
            notes = notes.trim(),
            createdAt = now,
            updatedAt = now
        )

        val jobId = jobDao.insertJob(job)

        // If direct expenses were incurred immediately, log to business expenses
        if (directExpensesPaise > 0L) {
            expenseDao.insertExpense(
                BusinessExpense(
                    category = "Service Materials",
                    title = "Materials for Job #$jobId: ${title.take(30)}",
                    amountPaise = directExpensesPaise,
                    linkedJobId = jobId,
                    notes = "Direct expense incurred for job"
                )
            )
        }

        Result.success(jobId)
    }

    suspend fun updateJobStatus(jobId: Long, newStatus: JobStatus) {
        val now = System.currentTimeMillis()
        jobDao.updateJobStatus(jobId, newStatus, now)
        if (newStatus == JobStatus.DELIVERED) {
            val job = jobDao.getJobById(jobId)
            if (job != null) {
                jobDao.updateJob(job.copy(status = newStatus, completionDate = now, updatedAt = now))
            }
        }
    }

    suspend fun recordJobPayment(jobId: Long, amountPaise: Long): Result<Unit> = database.withTransaction {
        val job = jobDao.getJobById(jobId)
            ?: return@withTransaction Result.failure(IllegalArgumentException("Job #$jobId not found"))

        val newPaid = job.paidAmountPaise + amountPaise
        val newBalance = (job.totalChargePaise - newPaid).coerceAtLeast(0L)

        jobDao.recordJobPayment(jobId, newPaid, newBalance)
        Result.success(Unit)
    }
}

