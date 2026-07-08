package com.buildkt.feature.address.presentation.edit

import com.buildkt.feature.address.domain.Address

sealed interface EditAddressIntent {
    // Pane actions

    data object BackClicked : EditAddressIntent

    // Load existing address
    data class LoadAddress(
        val addressId: Long,
    ) : EditAddressIntent

    sealed interface LoadAddressResult : EditAddressIntent {
        data class Success(
            val address: Address,
        ) : LoadAddressResult

        data class Failure(
            val message: String,
        ) : LoadAddressResult
    }

    // Input has changed
    data class StreetChanged(
        val value: String,
    ) : EditAddressIntent

    data class CityChanged(
        val value: String,
    ) : EditAddressIntent

    data class ZipChanged(
        val value: String,
    ) : EditAddressIntent

    data class CountryChanged(
        val value: String,
    ) : EditAddressIntent

    // Validation results
    data class StreetValidationError(
        val error: String?,
    ) : EditAddressIntent

    data class CityValidationError(
        val error: String?,
    ) : EditAddressIntent

    data class ZipValidationError(
        val error: String?,
    ) : EditAddressIntent

    data class CountryValidationError(
        val error: String?,
    ) : EditAddressIntent

    // Save action and results

    data object EditAddress : EditAddressIntent

    sealed interface EditAddressResult : EditAddressIntent {

        data object Success : EditAddressResult

        data class Failure(
            val message: String,
        ) : EditAddressResult
    }
}
