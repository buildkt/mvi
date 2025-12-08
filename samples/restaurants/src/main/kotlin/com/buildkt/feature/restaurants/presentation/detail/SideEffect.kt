package com.buildkt.feature.restaurants.presentation.detail

import com.buildkt.feature.restaurants.domain.RestaurantRepository
import com.buildkt.mvi.sideEffect

fun loadPaginatedMenus(repository: RestaurantRepository) = sideEffect<RestaurantDetailUiState, RestaurantDetailIntent> { _, intent ->
    intent as RestaurantDetailIntent.PaneLaunched
    val menus = repository.getRestaurantMenus(restaurantId = intent.restaurantId)

    RestaurantDetailIntent.MenusLoaded(menus)
}

fun loadRestaurantInfo(repository: RestaurantRepository) = sideEffect<RestaurantDetailUiState, RestaurantDetailIntent> { _, intent ->
    intent as RestaurantDetailIntent.PaneLaunched
    val restaurantInfo = repository.getRestaurantInfo(restaurantId = intent.restaurantId)

    RestaurantDetailIntent.RestaurantInfoLoaded(info = restaurantInfo)
}