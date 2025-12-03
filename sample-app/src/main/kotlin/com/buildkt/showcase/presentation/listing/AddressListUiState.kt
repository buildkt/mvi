package com.buildkt.showcase.presentation.listing

import androidx.compose.runtime.Immutable
import com.buildkt.showcase.domain.Address
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
