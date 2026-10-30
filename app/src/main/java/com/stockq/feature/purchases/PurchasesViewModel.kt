package com.stockq.feature.purchases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockq.core.database.dao.PurchaseWithSupplier
import com.stockq.core.database.entity.Product
import com.stockq.core.database.entity.Supplier
import com.stockq.core.model.Money
import com.stockq.data.repository.ProductRepository
import com.stockq.data.repository.PurchaseDraftItemInput
import com.stockq.data.repository.PurchaseRepository
import com.stockq.data.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DraftPurchaseItemInput(
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitCostPaise: Long,
    val taxPercent: Double = 0.0,
    val discountPercent: Double = 0.0
)

data class PurchaseDialogState(
    val isOpen: Boolean = false,
    val selectedSupplierId: Long? = null,
    val invoiceNumber: String = "",
    val freightString: String = "0",
    val notes: String = "",
    val items: List<DraftPurchaseItemInput> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class PurchasesViewModel(
    private val purchaseRepository: PurchaseRepository,
    supplierRepository: SupplierRepository,
    productRepository: ProductRepository
) : ViewModel() {

    val purchases: StateFlow<List<PurchaseWithSupplier>> = purchaseRepository.getAllPurchasesWithSupplier()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<Supplier>> = supplierRepository.getAllSuppliers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = productRepository.getActiveProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dialogState = MutableStateFlow(PurchaseDialogState())
    val dialogState: StateFlow<PurchaseDialogState> = _dialogState.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun openNewPurchaseDialog() {
        _dialogState.value = PurchaseDialogState(
            isOpen = true,
            selectedSupplierId = suppliers.value.firstOrNull()?.id
        )
    }

    fun closeDialog() {
        _dialogState.value = PurchaseDialogState(isOpen = false)
    }

    fun onSupplierSelected(supplierId: Long) {
        _dialogState.value = _dialogState.value.copy(selectedSupplierId = supplierId, errorMessage = null)
    }

    fun onInvoiceNumberChange(number: String) {
        _dialogState.value = _dialogState.value.copy(invoiceNumber = number, errorMessage = null)
    }

    fun onFreightChange(freight: String) {
        _dialogState.value = _dialogState.value.copy(freightString = freight)
    }

    fun onNotesChange(notes: String) {
        _dialogState.value = _dialogState.value.copy(notes = notes)
    }

    fun addItemToDraft(
        productId: Long,
        quantity: Double,
        costRupees: Double,
        taxPercent: Double = 0.0,
        discountPercent: Double = 0.0
    ) {
        val prod = products.value.find { it.id == productId } ?: return
        val unitCostPaise = Money.rupeesToPaise(costRupees)
        val newItem = DraftPurchaseItemInput(
            productId = productId,
            productName = prod.name,
            quantity = quantity,
            unitCostPaise = unitCostPaise,
            taxPercent = taxPercent,
            discountPercent = discountPercent
        )
        val currentItems = _dialogState.value.items.toMutableList()
        currentItems.add(newItem)
        _dialogState.value = _dialogState.value.copy(items = currentItems, errorMessage = null)
    }

    fun removeItemFromDraft(index: Int) {
        val currentItems = _dialogState.value.items.toMutableList()
        if (index in currentItems.indices) {
            currentItems.removeAt(index)
            _dialogState.value = _dialogState.value.copy(items = currentItems)
        }
    }

    fun savePurchase(autoConfirm: Boolean) {
        val state = _dialogState.value
        val supplierId = state.selectedSupplierId
        if (supplierId == null) {
            _dialogState.value = state.copy(errorMessage = "Please select a supplier")
            return
        }
        if (state.items.isEmpty()) {
            _dialogState.value = state.copy(errorMessage = "Please add at least one item to inward")
            return
        }

        _dialogState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                val freightPaise = Money.rupeesToPaise(state.freightString.toDoubleOrNull() ?: 0.0)
                val draftItems = state.items.map {
                    PurchaseDraftItemInput(
                        productId = it.productId,
                        quantity = it.quantity,
                        unitCostPaise = it.unitCostPaise,
                        discountPercent = it.discountPercent,
                        taxRatePercent = it.taxPercent
                    )
                }

                val createResult = purchaseRepository.createPurchase(
                    supplierId = supplierId,
                    invoiceNumber = state.invoiceNumber.trim(),
                    purchaseDate = System.currentTimeMillis(),
                    items = draftItems,
                    discountPaise = 0L,
                    transportChargesPaise = freightPaise,
                    notes = state.notes.trim()
                )

                if (createResult.isFailure) {
                    _dialogState.value = state.copy(
                        isSaving = false,
                        errorMessage = createResult.exceptionOrNull()?.message ?: "Failed to create purchase"
                    )
                    return@launch
                }

                val purchaseId = createResult.getOrThrow()

                if (autoConfirm) {
                    val confirmResult = purchaseRepository.confirmPurchase(purchaseId)
                    if (confirmResult.isSuccess) {
                        _statusMessage.value = "Purchase confirmed and inventory updated successfully!"
                    } else {
                        _statusMessage.value = "Purchase saved as DRAFT. Confirm note: ${confirmResult.exceptionOrNull()?.message}"
                    }
                } else {
                    _statusMessage.value = "Purchase draft saved successfully."
                }

                closeDialog()
            } catch (e: Exception) {
                _dialogState.value = state.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to save purchase"
                )
            }
        }
    }

    fun confirmPurchase(purchaseId: Long) {
        viewModelScope.launch {
            val result = purchaseRepository.confirmPurchase(purchaseId)
            if (result.isSuccess) {
                _statusMessage.value = "Purchase #$purchaseId confirmed! Stock ledger updated."
            } else {
                _statusMessage.value = "Error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun cancelPurchase(purchaseId: Long, reason: String = "User cancelled") {
        viewModelScope.launch {
            val result = purchaseRepository.cancelPurchase(purchaseId, reason)
            if (result.isSuccess) {
                _statusMessage.value = "Purchase #$purchaseId cancelled and stock reversed."
            } else {
                _statusMessage.value = "Error: ${result.exceptionOrNull()?.message}"
            }
        }
    }
}
