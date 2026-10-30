package com.subzero.stockq.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subzero.stockq.core.database.entity.BusinessProfile
import com.subzero.stockq.data.repository.BusinessProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileFormState(
    val storeName: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val gstin: String = "",
    val address: String = "",
    val currencySymbol: String = "₹",
    val isSaving: Boolean = false,
    val message: String? = null
)

class SettingsViewModel(
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    val profile: StateFlow<BusinessProfile?> = businessProfileRepository.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _formState = MutableStateFlow(ProfileFormState())
    val formState: StateFlow<ProfileFormState> = _formState.asStateFlow()

    init {
        viewModelScope.launch {
            businessProfileRepository.getProfile().collect { prof ->
                prof?.let {
                    _formState.value = ProfileFormState(
                        storeName = it.storeName,
                        ownerName = it.ownerName,
                        phone = it.phone,
                        email = it.email,
                        gstin = it.gstin,
                        address = it.address,
                        currencySymbol = it.currencySymbol
                    )
                }
            }
        }
    }

    fun onStoreNameChange(v: String) { _formState.value = _formState.value.copy(storeName = v, message = null) }
    fun onOwnerNameChange(v: String) { _formState.value = _formState.value.copy(ownerName = v, message = null) }
    fun onPhoneChange(v: String) { _formState.value = _formState.value.copy(phone = v, message = null) }
    fun onEmailChange(v: String) { _formState.value = _formState.value.copy(email = v, message = null) }
    fun onGstinChange(v: String) { _formState.value = _formState.value.copy(gstin = v, message = null) }
    fun onAddressChange(v: String) { _formState.value = _formState.value.copy(address = v, message = null) }
    fun onCurrencyChange(v: String) { _formState.value = _formState.value.copy(currencySymbol = v, message = null) }

    fun saveProfile() {
        val state = _formState.value
        _formState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            val updated = BusinessProfile(
                id = 1L,
                storeName = state.storeName.trim().ifBlank { "My Business Store" },
                ownerName = state.ownerName.trim(),
                phone = state.phone.trim(),
                email = state.email.trim(),
                gstin = state.gstin.trim(),
                address = state.address.trim(),
                currencySymbol = state.currencySymbol.trim().ifBlank { "₹" }
            )
            businessProfileRepository.updateProfile(updated)
            _formState.value = state.copy(
                isSaving = false,
                message = "Business profile saved successfully!"
            )
        }
    }
}

