package com.buildkt.showcase.presentation.create

import com.buildkt.showcase.domain.Address
import com.buildkt.showcase.domain.AddressRepository
import com.buildkt.mvi.sideEffect

/**
 * A side effect to validate the street field.
 * In a real app, this could check for minimum length or specific patterns.
 */
val createAddressValidateStreet =
    sideEffect<CreateAddressUiState, CreateAddressIntent> { state, _ ->
        val error =
            when {
                state.street.isBlank() -> "Street name cannot be empty."
                state.street.length < 3 -> "Street name must be at least 3 characters."
                else -> null
            }
        CreateAddressIntent.StreetValidationError(error = error)
    }

/**
 * A side effect to validate the city field.
 */
val createAddressValidateCity =
    sideEffect<CreateAddressUiState, CreateAddressIntent> { state, _ ->
        val error =
            when {
                state.city.isBlank() -> "City cannot be empty."
                state.city.length < 2 -> "City name seems too short."
                else -> null
            }
        CreateAddressIntent.CityValidationError(error = error)
    }

/**
 * A side effect to validate the ZIP code field.
 * This validation is still basic but provides a better structure for a real implementation
 * which would likely use a country-specific regex.
 */
val createAddressValidateZip =
    sideEffect<CreateAddressUiState, CreateAddressIntent> { state, _ ->
        val error =
            when {
                state.zip.isBlank() -> "ZIP code cannot be empty."
                state.zip.any { it.isLetter() } && state.zip.any { it.isDigit() }.not() -> "Invalid ZIP code format."
                state.zip.length < 4 -> "ZIP code seems too short."
                else -> null
            }
        CreateAddressIntent.ZipValidationError(error = error)
    }

/**
 * A side effect to validate the country field.
 */
val createAddressValidateCountry =
    sideEffect<CreateAddressUiState, CreateAddressIntent> { state, _ ->
        val error = if (state.country.isBlank()) "Country cannot be empty." else null
        CreateAddressIntent.CountryValidationError(error = error)
    }

/**
 * A simulated side effect for saving the address.
 * It introduces a delay to mimic a network request and can return a success or error result.
 */
fun createAddress(repository: AddressRepository) =
    sideEffect<CreateAddressUiState, CreateAddressIntent> { state, _ ->
        try {
            if (state.addressId == null) {
                repository.insertAddress(street = state.street, city = state.city, zip = state.zip, country = state.country)
            } else {
                val address =
                    Address(
                        id = state.addressId,
                        street = state.street,
                        city = state.city,
                        zip = state.zip,
                        country = state.country
                    )
                repository.updateAddress(address)
            }
            CreateAddressIntent.SaveAddressResult.Success
        } catch (_: Exception) {
            CreateAddressIntent.SaveAddressResult.Failure("A network error occurred. Please try again.")
        }
    }
