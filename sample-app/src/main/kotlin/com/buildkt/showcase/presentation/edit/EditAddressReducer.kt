package com.buildkt.showcase.presentation.edit

import com.buildkt.mvi.Reducer

fun editAddressReducer() : Reducer<EditAddressUiState, EditAddressIntent> = Reducer { state, intent ->
    when (intent) {
        is EditAddressIntent.LoadAddress -> state.copy(isLoading = true, addressId = intent.addressId)
        is EditAddressIntent.LoadAddressResult.Success ->
            state.copy(
                isLoading = false,
                street = intent.address.street,
                city = intent.address.city,
                zip = intent.address.zip,
                country = intent.address.country,
                )
        is EditAddressIntent.LoadAddressResult.Failure -> state.copy(isLoading = false, saveErrorMessage = intent.message)

        is EditAddressIntent.StreetChanged -> state.copy(street = intent.value)
        is EditAddressIntent.CityChanged -> state.copy(city = intent.value)
        is EditAddressIntent.ZipChanged -> state.copy(zip = intent.value)
        is EditAddressIntent.CountryChanged -> state.copy(country = intent.value)

        is EditAddressIntent.StreetValidationError -> state.copy(streetError = intent.error)
        is EditAddressIntent.CityValidationError -> state.copy(cityError = intent.error)
        is EditAddressIntent.ZipValidationError -> state.copy(zipError = intent.error)
        is EditAddressIntent.CountryValidationError -> state.copy(countryError = intent.error)

        is EditAddressIntent.EditAddress -> state.copy(isLoading = true, saveErrorMessage = null)
        is EditAddressIntent.EditAddressResult.Success -> state.copy(isLoading = false)
        is EditAddressIntent.EditAddressResult.Failure -> state.copy(isLoading = false, saveErrorMessage = intent.message)
        is EditAddressIntent.BackClicked -> state
    }
}
