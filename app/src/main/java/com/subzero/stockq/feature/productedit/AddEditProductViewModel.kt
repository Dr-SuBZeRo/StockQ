package com.subzero.stockq.feature.productedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subzero.stockq.core.database.entity.ProductCategory
import com.subzero.stockq.core.model.Money
import com.subzero.stockq.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddEditProductState(
    val name: String = "",
    val sku: String = "",
    val barcode: String = "",
    val unit: String = "pcs",
    val costPriceRupees: String = "",
    val sellingPriceRupees: String = "",
    val initialStock: String = "0",
    val minStockThreshold: String = "5",
    val idealReorderQty: String = "20",
    val selectedCategoryId: Long? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)

class AddEditProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditProductState())
    val uiState: StateFlow<AddEditProductState> = _uiState.asStateFlow()

    val categories: StateFlow<List<ProductCategory>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onNameChange(value: String) { _uiState.value = _uiState.value.copy(name = value, errorMessage = null) }
    fun onSkuChange(value: String) { _uiState.value = _uiState.value.copy(sku = value) }
    fun onBarcodeChange(value: String) { _uiState.value = _uiState.value.copy(barcode = value) }
    fun onUnitChange(value: String) { _uiState.value = _uiState.value.copy(unit = value) }
    fun onCostPriceChange(value: String) { _uiState.value = _uiState.value.copy(costPriceRupees = value, errorMessage = null) }
    fun onSellingPriceChange(value: String) { _uiState.value = _uiState.value.copy(sellingPriceRupees = value, errorMessage = null) }
    fun onInitialStockChange(value: String) { _uiState.value = _uiState.value.copy(initialStock = value) }
    fun onMinThresholdChange(value: String) { _uiState.value = _uiState.value.copy(minStockThreshold = value) }
    fun onIdealReorderChange(value: String) { _uiState.value = _uiState.value.copy(idealReorderQty = value) }
    fun onCategoryChange(categoryId: Long?) { _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId) }

    fun saveProduct() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Product name is required")
            return
        }

        val costRupees = state.costPriceRupees.toDoubleOrNull() ?: 0.0
        val sellRupees = state.sellingPriceRupees.toDoubleOrNull() ?: 0.0
        val initStock = state.initialStock.toDoubleOrNull() ?: 0.0
        val minThreshold = state.minStockThreshold.toDoubleOrNull() ?: 5.0
        val reorderQty = state.idealReorderQty.toDoubleOrNull() ?: 20.0

        if (costRupees < 0.0 || sellRupees < 0.0) {
            _uiState.value = state.copy(errorMessage = "Prices cannot be negative")
            return
        }

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            val result = repository.createProduct(
                name = state.name,
                sku = state.sku,
                barcode = state.barcode,
                unit = state.unit,
                costPricePaise = Money.rupeesToPaise(costRupees),
                sellingPricePaise = Money.rupeesToPaise(sellRupees),
                initialStock = initStock,
                minStockThreshold = minThreshold,
                idealReorderQuantity = reorderQty,
                categoryId = state.selectedCategoryId
            )

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to save product"
                )
            }
        }
    }
}

