package com.buildkt.showcase.presentation.edit

import androidx.compose.runtime.Immutable

@Immutable
data class EditAddressUiState(
    val addressId: Long? = null,
    val street: String = "",
    val city: String = "",
    val zip: String = "",
    val country: String = "",
    val streetError: String? = null,
    val cityError: String? = null,
    val zipError: String? = null,
    val countryError: String? = null,
    val isLoading: Boolean = false,
    val saveErrorMessage: String? = null,
) {
    val isFormValid: Boolean
        get() =
            street.isNotBlank() &&
                city.isNotBlank() &&
                zip.isNotBlank() &&
                country.isNotBlank() &&
                streetError == null &&
                cityError == null &&
                zipError == null &&
                countryError == null
}
