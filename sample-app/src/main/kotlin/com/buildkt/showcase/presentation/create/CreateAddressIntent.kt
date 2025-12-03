package com.buildkt.showcase.presentation.create

import com.buildkt.mvi.TriggersSideEffect

sealed interface CreateAddressIntent {
    // Pane actions
    @TriggersSideEffect
    data object BackClicked : CreateAddressIntent

    // Input has changed
    @TriggersSideEffect
    data class StreetChanged(
        val value: String,
    ) : CreateAddressIntent

    @TriggersSideEffect
    data class CityChanged(
        val value: String,
    ) : CreateAddressIntent

    @TriggersSideEffect
    data class ZipChanged(
        val value: String,
    ) : CreateAddressIntent

    @TriggersSideEffect
    data class CountryChanged(
        val value: String,
    ) : CreateAddressIntent

    // Validation results
    data class StreetValidationError(
        val error: String?,
    ) : CreateAddressIntent

    data class CityValidationError(
        val error: String?,
    ) : CreateAddressIntent

    data class ZipValidationError(
        val error: String?,
    ) : CreateAddressIntent

    data class CountryValidationError(
        val error: String?,
    ) : CreateAddressIntent

    // Save action and results
    @TriggersSideEffect
    data object SaveAddress : CreateAddressIntent

    sealed interface SaveAddressResult : CreateAddressIntent {
        @TriggersSideEffect
        data object Success : SaveAddressResult

        @TriggersSideEffect
        data class Failure(
            val message: String,
        ) : SaveAddressResult
    }
}
