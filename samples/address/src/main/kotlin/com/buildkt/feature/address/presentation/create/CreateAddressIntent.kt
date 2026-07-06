package com.buildkt.feature.address.presentation.create

sealed interface CreateAddressIntent {
    // Pane actions
    data object BackClicked : CreateAddressIntent

    // Input has changed
    data class StreetChanged(val value: String) : CreateAddressIntent

    data class CityChanged(val value: String) : CreateAddressIntent

    data class ZipChanged(val value: String) : CreateAddressIntent

    data class CountryChanged(val value: String) : CreateAddressIntent

    // Validation results
    data class StreetValidationError(val error: String?) : CreateAddressIntent

    data class CityValidationError(val error: String?) : CreateAddressIntent

    data class ZipValidationError(val error: String?) : CreateAddressIntent

    data class CountryValidationError(val error: String?) : CreateAddressIntent

    // Save action and results
    data object SaveAddress : CreateAddressIntent

    sealed interface SaveAddressResult : CreateAddressIntent {

        data object Success : SaveAddressResult


        data class Failure(val message: String) : SaveAddressResult
    }
}
