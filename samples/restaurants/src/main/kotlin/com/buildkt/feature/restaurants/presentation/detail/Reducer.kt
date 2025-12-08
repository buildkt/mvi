package com.buildkt.feature.restaurants.presentation.detail

import com.buildkt.mvi.Reducer

fun restaurantDetailReducer() = Reducer<RestaurantDetailUiState, RestaurantDetailIntent> { state, intent ->
    when (intent) {
        is RestaurantDetailIntent.PaneLaunched -> state
        is RestaurantDetailIntent.BackClicked -> state
        is RestaurantDetailIntent.RestaurantInfoLoaded -> state.copy(restaurantInfo = intent.info, isRestaurantInfoLoading = false)
        is RestaurantDetailIntent.MenuItemSelected -> state
        is RestaurantDetailIntent.MenusLoaded -> state.copy(menuItems = intent.menus, isMenuItemsLoading = false)
    }
}
