package com.buildkt.feature.restaurants.presentation.detail

import androidx.paging.PagingData
import com.buildkt.feature.restaurants.domain.MenuItem
import com.buildkt.feature.restaurants.domain.RestaurantInfo
import com.buildkt.mvi.TriggersSideEffect
import kotlinx.coroutines.flow.Flow

sealed interface RestaurantDetailIntent {
    @TriggersSideEffect
    data class PaneLaunched(val restaurantId: Int) : RestaurantDetailIntent
    @TriggersSideEffect
    data object BackClicked : RestaurantDetailIntent
    @TriggersSideEffect
    data class RestaurantInfoLoaded(val info: RestaurantInfo) : RestaurantDetailIntent
    @TriggersSideEffect
    data class MenusLoaded(val menus: Flow<PagingData<MenuItem>>) : RestaurantDetailIntent
    @TriggersSideEffect
    data class MenuItemSelected(val menuItemId: String) : RestaurantDetailIntent
}
