package com.stockq.feature.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockq.data.repository.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BackupRestoreViewModel(
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun exportBackup(context: Context, destinationUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val outputStream = context.contentResolver.openOutputStream(destinationUri)
                if (outputStream == null) {
                    _message.value = "Unable to open destination file for export"
                    _isLoading.value = false
                    return@launch
                }

                val result = backupRepository.exportDatabase(outputStream)
                result.onSuccess { bytes ->
                    _message.value = "Backup successfully exported (${bytes / 1024} KB)!"
                }.onFailure { err ->
                    _message.value = "Export failed: ${err.message}"
                }
            } catch (e: Exception) {
                _message.value = "Export error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restoreBackup(context: Context, sourceUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                if (inputStream == null) {
                    _message.value = "Unable to open selected backup file"
                    _isLoading.value = false
                    return@launch
                }

                val result = backupRepository.restoreDatabase(inputStream)
                result.onSuccess {
                    _message.value = "Database restored successfully! Please restart StockQ to apply changes."
                }.onFailure { err ->
                    _message.value = "Restore failed: ${err.message}"
                }
            } catch (e: Exception) {
                _message.value = "Restore error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

