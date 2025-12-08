package com.buildkt.feature.address.presentation.listing

import androidx.compose.runtime.Immutable
import com.buildkt.feature.address.domain.Address
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Immutable
data class AddressListUiState(
    val isLoading: Boolean = true,
    val addresses: Flow<List<AddressItem>> = emptyFlow(),
    val error: String? = null,
) {
    @Immutable
    data class AddressItem(
        val address: Address,
        val isSelected: Boolean,
    )
}
