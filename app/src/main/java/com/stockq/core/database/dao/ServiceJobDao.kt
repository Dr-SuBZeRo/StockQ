package com.stockq.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockq.core.database.entity.JobStatus
import com.stockq.core.database.entity.ServiceJob
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceJobDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: ServiceJob): Long

    @Update
    suspend fun updateJob(job: ServiceJob)

    @Delete
    suspend fun deleteJob(job: ServiceJob)

    @Query("SELECT * FROM service_jobs WHERE id = :id")
    suspend fun getJobById(id: Long): ServiceJob?

    @Query("SELECT * FROM service_jobs ORDER BY startDate DESC")
    fun getAllJobs(): Flow<List<ServiceJob>>

    @Query("SELECT * FROM service_jobs WHERE status = :status ORDER BY startDate DESC")
    fun getJobsByStatus(status: JobStatus): Flow<List<ServiceJob>>

    @Query("SELECT * FROM service_jobs WHERE customerId = :customerId ORDER BY startDate DESC")
    fun getJobsForCustomer(customerId: Long): Flow<List<ServiceJob>>

    @Query("SELECT COUNT(*) FROM service_jobs WHERE status NOT IN ('DELIVERED', 'CANCELLED')")
    fun getActiveJobsCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(balanceDuePaise), 0) FROM service_jobs WHERE balanceDuePaise > 0 AND status != 'CANCELLED'")
    fun getTotalPendingJobBalanceDue(): Flow<Long>

    @Query("SELECT COALESCE(SUM(totalChargePaise), 0) FROM service_jobs WHERE status = 'DELIVERED'")
    fun getTotalCompletedJobRevenue(): Flow<Long>

    @Query("SELECT COALESCE(SUM(directExpensesPaise), 0) FROM service_jobs WHERE status = 'DELIVERED'")
    fun getTotalCompletedJobExpenses(): Flow<Long>

    @Query("UPDATE service_jobs SET status = :newStatus, updatedAt = :updatedAt WHERE id = :jobId")
    suspend fun updateJobStatus(jobId: Long, newStatus: JobStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE service_jobs SET paidAmountPaise = :paidAmount, balanceDuePaise = :balanceDue, updatedAt = :updatedAt WHERE id = :jobId")
    suspend fun recordJobPayment(jobId: Long, paidAmount: Long, balanceDue: Long, updatedAt: Long = System.currentTimeMillis())
}

