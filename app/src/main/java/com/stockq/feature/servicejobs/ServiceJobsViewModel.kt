package com.stockq.feature.servicejobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockq.core.database.entity.JobStatus
import com.stockq.core.database.entity.ServiceJob
import com.stockq.data.repository.ServiceJobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServiceJobsViewModel(
    private val serviceJobRepository: ServiceJobRepository
) : ViewModel() {

    val jobs: StateFlow<List<ServiceJob>> = serviceJobRepository.getAllJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCount: StateFlow<Int> = serviceJobRepository.getActiveJobsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingBalancePaise: StateFlow<Long> = serviceJobRepository.getTotalPendingJobBalanceDue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun createJob(
        customerName: String,
        customerPhone: String,
        title: String,
        description: String,
        labourChargesRupees: String,
        materialChargesRupees: String,
        directExpenseRupees: String,
        paidRupees: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val labourPaise = (labourChargesRupees.toDoubleOrNull()?.times(100))?.toLong() ?: 0L
            val materialPaise = (materialChargesRupees.toDoubleOrNull()?.times(100))?.toLong() ?: 0L
            val directPaise = (directExpenseRupees.toDoubleOrNull()?.times(100))?.toLong() ?: 0L
            val paidPaise = (paidRupees.toDoubleOrNull()?.times(100))?.toLong() ?: 0L

            val result = serviceJobRepository.createJob(
                customerName = customerName,
                customerPhone = customerPhone,
                title = title,
                description = description,
                labourChargesPaise = labourPaise,
                materialChargesPaise = materialPaise,
                directExpensesPaise = directPaise,
                paidAmountPaise = paidPaise
            )

            result.onSuccess {
                _message.value = "Service job registered successfully"
                onSuccess()
            }.onFailure {
                _message.value = it.message ?: "Failed to create service job"
            }
        }
    }

    fun updateJobStatus(jobId: Long, newStatus: JobStatus) {
        viewModelScope.launch {
            serviceJobRepository.updateJobStatus(jobId, newStatus)
            _message.value = "Job status updated to ${newStatus.name}"
        }
    }

    fun recordJobPayment(jobId: Long, amountRupees: String, onSuccess: () -> Unit) {
        val amountPaise = (amountRupees.toDoubleOrNull()?.times(100))?.toLong()
        if (amountPaise == null || amountPaise <= 0L) {
            _message.value = "Invalid payment amount"
            return
        }

        viewModelScope.launch {
            val result = serviceJobRepository.recordJobPayment(jobId, amountPaise)
            result.onSuccess {
                _message.value = "Payment recorded"
                onSuccess()
            }.onFailure {
                _message.value = it.message ?: "Failed to record payment"
            }
        }
    }
}

