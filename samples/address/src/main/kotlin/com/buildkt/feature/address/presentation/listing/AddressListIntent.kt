package com.buildkt.feature.address.presentation.listing

import kotlinx.coroutines.flow.Flow

sealed interface AddressListIntent {
    // Pane actions

    data object BackClicked : AddressListIntent

    data object PaneLaunched : AddressListIntent

    data class AddressSelected(
        val addressId: Long,
    ) : AddressListIntent

    data object AddNewAddress : AddressListIntent

    data class EditAddress(
        val addressId: Long,
    ) : AddressListIntent

    sealed interface LoadAddressesResult : AddressListIntent {
        data class Success(
            val addresses: Flow<List<AddressListUiState.AddressItem>>,
        ) : LoadAddressesResult

        data class Failure(
            val message: String,
        ) : LoadAddressesResult
    }
}
