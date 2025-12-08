package com.buildkt.feature.address.presentation.edit

import com.buildkt.feature.address.domain.Address
import com.buildkt.mvi.TriggersSideEffect

sealed interface EditAddressIntent {
    // Pane actions
    @TriggersSideEffect
    data object BackClicked : EditAddressIntent

    // Load existing address
    @TriggersSideEffect
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
    @TriggersSideEffect
    data class StreetChanged(
        val value: String,
    ) : EditAddressIntent

    @TriggersSideEffect
    data class CityChanged(
        val value: String,
    ) : EditAddressIntent

    @TriggersSideEffect
    data class ZipChanged(
        val value: String,
    ) : EditAddressIntent

    @TriggersSideEffect
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
    @TriggersSideEffect
    data object EditAddress : EditAddressIntent

    sealed interface EditAddressResult : EditAddressIntent {
        @TriggersSideEffect
        data object Success : EditAddressResult

        @TriggersSideEffect
        data class Failure(
            val message: String,
        ) : EditAddressResult
    }
}
