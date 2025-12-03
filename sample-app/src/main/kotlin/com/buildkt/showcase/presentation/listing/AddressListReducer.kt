package com.buildkt.showcase.presentation.listing

import com.buildkt.mvi.Reducer

fun addressListReducer() : Reducer<AddressListUiState, AddressListIntent> = Reducer { state, intent ->
    when (intent) {
        is AddressListIntent.PaneLaunched -> state.copy(isLoading = true)
        is AddressListIntent.LoadAddressesResult.Success -> state.copy(isLoading = false, addresses = intent.addresses)
        is AddressListIntent.LoadAddressesResult.Failure -> state.copy(isLoading = false, error = intent.message)
        else -> state
    }
}
