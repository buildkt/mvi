package com.buildkt.showcase.presentation.create

import com.buildkt.mvi.Reducer

fun createAddressReducer() : Reducer<CreateAddressUiState, CreateAddressIntent> = Reducer { state, intent ->
    when (intent) {
        is CreateAddressIntent.BackClicked -> state

        is CreateAddressIntent.StreetChanged -> state.copy(street = intent.value)
        is CreateAddressIntent.CityChanged -> state.copy(city = intent.value)
        is CreateAddressIntent.ZipChanged -> state.copy(zip = intent.value)
        is CreateAddressIntent.CountryChanged -> state.copy(country = intent.value)

        is CreateAddressIntent.StreetValidationError -> state.copy(streetError = intent.error)
        is CreateAddressIntent.CityValidationError -> state.copy(cityError = intent.error)
        is CreateAddressIntent.ZipValidationError -> state.copy(zipError = intent.error)
        is CreateAddressIntent.CountryValidationError -> state.copy(countryError = intent.error)

        is CreateAddressIntent.SaveAddress -> state.copy(isLoading = true, saveErrorMessage = null)
        is CreateAddressIntent.SaveAddressResult.Success -> state.copy(isLoading = false)
        is CreateAddressIntent.SaveAddressResult.Failure -> state.copy(isLoading = false, saveErrorMessage = intent.message)
    }
}
