package com.buildkt.feature.restaurants.presentation.detail

import androidx.paging.PagingData
import com.buildkt.feature.restaurants.domain.MenuItem
import com.buildkt.feature.restaurants.domain.RestaurantInfo
import kotlinx.coroutines.flow.Flow

sealed interface RestaurantDetailIntent {

    data class PaneLaunched(val restaurantId: Int) : RestaurantDetailIntent

    data object BackClicked : RestaurantDetailIntent

    data class RestaurantInfoLoaded(val info: RestaurantInfo) : RestaurantDetailIntent

    data class MenusLoaded(val menus: Flow<PagingData<MenuItem>>) : RestaurantDetailIntent

    data class MenuItemSelected(val menuItemId: String) : RestaurantDetailIntent
}
