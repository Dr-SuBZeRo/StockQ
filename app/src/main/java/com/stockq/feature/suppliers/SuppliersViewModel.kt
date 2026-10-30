package com.stockq.feature.suppliers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockq.core.database.entity.Supplier
import com.stockq.data.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SupplierDialogState(
    val isOpen: Boolean = false,
    val supplierId: Long? = null,
    val name: String = "",
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val gstin: String = "",
    val address: String = "",
    val paymentTermsDays: String = "30",
    val errorMessage: String? = null
)

class SuppliersViewModel(
    private val supplierRepository: SupplierRepository
) : ViewModel() {

    val suppliers: StateFlow<List<Supplier>> = supplierRepository.getActiveSuppliers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dialogState = MutableStateFlow(SupplierDialogState())
    val dialogState: StateFlow<SupplierDialogState> = _dialogState.asStateFlow()

    fun openAddDialog() {
        _dialogState.value = SupplierDialogState(isOpen = true)
    }

    fun openEditDialog(supplier: Supplier) {
        _dialogState.value = SupplierDialogState(
            isOpen = true,
            supplierId = supplier.id,
            name = supplier.name,
            contactPerson = supplier.contactPerson,
            phone = supplier.phone,
            email = supplier.email,
            gstin = supplier.gstin,
            address = supplier.address,
            paymentTermsDays = supplier.paymentTermsDays.toString()
        )
    }

    fun closeDialog() {
        _dialogState.value = SupplierDialogState(isOpen = false)
    }

    fun updateName(value: String) { _dialogState.value = _dialogState.value.copy(name = value, errorMessage = null) }
    fun updateContactPerson(value: String) { _dialogState.value = _dialogState.value.copy(contactPerson = value) }
    fun updatePhone(value: String) { _dialogState.value = _dialogState.value.copy(phone = value) }
    fun updateEmail(value: String) { _dialogState.value = _dialogState.value.copy(email = value) }
    fun updateGstin(value: String) { _dialogState.value = _dialogState.value.copy(gstin = value) }
    fun updateAddress(value: String) { _dialogState.value = _dialogState.value.copy(address = value) }
    fun updatePaymentTerms(value: String) { _dialogState.value = _dialogState.value.copy(paymentTermsDays = value) }

    fun saveSupplier() {
        val state = _dialogState.value
        val trimmedName = state.name.trim()
        if (trimmedName.isBlank()) {
            _dialogState.value = state.copy(errorMessage = "Supplier name is required")
            return
        }

        val terms = state.paymentTermsDays.toIntOrNull() ?: 30

        viewModelScope.launch {
            if (state.supplierId == null) {
                supplierRepository.createSupplier(
                    name = trimmedName,
                    contactPerson = state.contactPerson.trim(),
                    phone = state.phone.trim(),
                    email = state.email.trim(),
                    address = state.address.trim(),
                    gstin = state.gstin.trim(),
                    paymentTermsDays = terms
                )
            } else {
                val updatedSupplier = Supplier(
                    id = state.supplierId,
                    name = trimmedName,
                    contactPerson = state.contactPerson.trim(),
                    phone = state.phone.trim(),
                    email = state.email.trim(),
                    address = state.address.trim(),
                    gstin = state.gstin.trim(),
                    paymentTermsDays = terms
                )
                supplierRepository.updateSupplier(updatedSupplier)
            }
            closeDialog()
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            supplierRepository.setArchived(supplier.id, true)
        }
    }
}
