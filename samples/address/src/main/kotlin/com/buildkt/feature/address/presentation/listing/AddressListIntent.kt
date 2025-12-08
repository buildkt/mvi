package com.buildkt.feature.address.presentation.listing

import com.buildkt.mvi.TriggersSideEffect
import kotlinx.coroutines.flow.Flow

sealed interface AddressListIntent {
    // Pane actions
    @TriggersSideEffect
    data object BackClicked : AddressListIntent

    @TriggersSideEffect
    data object PaneLaunched : AddressListIntent

    @TriggersSideEffect
    data class AddressSelected(
        val addressId: Long,
    ) : AddressListIntent

    @TriggersSideEffect
    data object AddNewAddress : AddressListIntent

    @TriggersSideEffect
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
