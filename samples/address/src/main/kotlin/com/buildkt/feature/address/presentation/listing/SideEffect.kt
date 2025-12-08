package com.buildkt.feature.address.presentation.listing

import com.buildkt.feature.address.domain.AddressRepository
import com.buildkt.mvi.sideEffect
import kotlinx.coroutines.flow.map

fun loadAddresses(repository: AddressRepository) =
    sideEffect<AddressListUiState, AddressListIntent> { _, _ ->
        try {
            val addresses = repository.obtainAddresses()

            AddressListIntent.LoadAddressesResult.Success(
                addresses =
                    addresses.map { addresses ->
                        addresses.map { address -> AddressListUiState.AddressItem(address, isSelected = false) }
                    },
            )
        } catch (e: Exception) {
            AddressListIntent.LoadAddressesResult.Failure(message = e.message.orEmpty())
        }
    }
