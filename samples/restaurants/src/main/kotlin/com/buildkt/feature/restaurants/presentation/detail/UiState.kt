package com.buildkt.feature.restaurants.presentation.detail

import androidx.paging.PagingData
import com.buildkt.feature.restaurants.domain.MenuItem
import com.buildkt.feature.restaurants.domain.RestaurantInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class RestaurantDetailUiState(
    val restaurantInfo: RestaurantInfo = RestaurantInfo(),
    val isRestaurantInfoLoading: Boolean = true,

    val menuItems: Flow<PagingData<MenuItem>> = emptyFlow(),
    val isMenuItemsLoading: Boolean = true,
)
