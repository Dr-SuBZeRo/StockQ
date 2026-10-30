package com.stockq.feature.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockq.core.database.entity.BusinessDocument
import com.stockq.core.database.entity.DocumentType
import com.stockq.data.repository.BusinessDocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DocumentsViewModel(
    private val documentRepository: BusinessDocumentRepository
) : ViewModel() {

    val documents: StateFlow<List<BusinessDocument>> = documentRepository.getAllActiveDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expiringCount: StateFlow<Int> = documentRepository.getExpiringDocumentsCount(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val expiredCount: StateFlow<Int> = documentRepository.getExpiredDocumentsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun addDocument(
        title: String,
        documentType: DocumentType,
        documentNumber: String,
        issuingAuthority: String,
        expiryDaysFromNow: Long?,
        notes: String,
        onSuccess: () -> Unit
    ) {
        val expiryDate = expiryDaysFromNow?.let {
            System.currentTimeMillis() + (it * 24 * 60 * 60 * 1000)
        }

        viewModelScope.launch {
            val result = documentRepository.createDocument(
                title = title,
                documentType = documentType,
                documentNumber = documentNumber,
                issuingAuthority = issuingAuthority,
                expiryDate = expiryDate,
                notes = notes
            )
            result.onSuccess {
                _message.value = "Document registered"
                onSuccess()
            }.onFailure {
                _message.value = it.message ?: "Failed to save document"
            }
        }
    }

    fun deleteDocument(document: BusinessDocument) {
        viewModelScope.launch {
            documentRepository.deleteDocument(document)
            _message.value = "Document deleted"
        }
    }
}

