package com.subzero.stockq.feature.stockmovement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subzero.stockq.core.database.dao.StockMovementWithProduct
import com.subzero.stockq.core.database.entity.Product
import com.subzero.stockq.core.database.entity.StockMovementType
import com.subzero.stockq.data.repository.ProductRepository
import com.subzero.stockq.data.repository.StockLedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StockMovementFormState(
    val selectedProductId: Long? = null,
    val movementType: StockMovementType = StockMovementType.SALE_ISSUE,
    val quantityString: String = "",
    val reasonOrNotes: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSubmitting: Boolean = false
)

class StockMovementViewModel(
    productRepository: ProductRepository,
    private val stockLedgerRepository: StockLedgerRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(StockMovementFormState())
    val formState: StateFlow<StockMovementFormState> = _formState.asStateFlow()

    val products: StateFlow<List<Product>> = productRepository.getActiveProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentMovements: StateFlow<List<StockMovementWithProduct>> = stockLedgerRepository.getRecentMovementsWithProduct(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onProductSelect(productId: Long?) { _formState.value = _formState.value.copy(selectedProductId = productId, errorMessage = null) }
    fun onMovementTypeSelect(type: StockMovementType) { _formState.value = _formState.value.copy(movementType = type, errorMessage = null) }
    fun onQuantityChange(value: String) { _formState.value = _formState.value.copy(quantityString = value, errorMessage = null) }
    fun onReasonChange(value: String) { _formState.value = _formState.value.copy(reasonOrNotes = value) }

    fun submitMovement() {
        val state = _formState.value
        val productId = state.selectedProductId
        if (productId == null) {
            _formState.value = state.copy(errorMessage = "Please select a product")
            return
        }

        val qty = state.quantityString.toDoubleOrNull()
        if (qty == null || (qty <= 0.0 && state.movementType != StockMovementType.PHYSICAL_ADJUSTMENT)) {
            _formState.value = state.copy(errorMessage = "Please enter a valid positive quantity")
            return
        }

        _formState.value = state.copy(isSubmitting = true)

        viewModelScope.launch {
            val result = when (state.movementType) {
                StockMovementType.SALE_ISSUE -> {
                    stockLedgerRepository.recordSaleIssue(productId, qty, notes = state.reasonOrNotes)
                }
                StockMovementType.PHYSICAL_ADJUSTMENT -> {
                    stockLedgerRepository.recordPhysicalAdjustment(productId, qty, reason = state.reasonOrNotes)
                }
                StockMovementType.DAMAGED_WASTAGE -> {
                    stockLedgerRepository.recordDamagedStock(productId, qty, reason = state.reasonOrNotes)
                }
                StockMovementType.CUSTOMER_RETURN -> {
                    stockLedgerRepository.recordCustomerReturn(productId, qty, reason = state.reasonOrNotes)
                }
                else -> {
                    Result.failure(IllegalArgumentException("Unsupported direct movement type: ${state.movementType}"))
                }
            }

            if (result.isSuccess) {
                _formState.value = StockMovementFormState(
                    successMessage = "Movement recorded successfully! Stock ledger updated.",
                    selectedProductId = productId
                )
            } else {
                _formState.value = state.copy(
                    isSubmitting = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to record movement"
                )
            }
        }
    }
}

