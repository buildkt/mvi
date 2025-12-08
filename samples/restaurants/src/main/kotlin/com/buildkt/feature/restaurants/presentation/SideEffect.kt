package com.buildkt.feature.restaurants.presentation

import com.buildkt.feature.restaurants.domain.RestaurantRepository
import com.buildkt.mvi.sideEffect

fun loadPaginatedRestaurants(repository: RestaurantRepository) = sideEffect<RestaurantsUiState, RestaurantsIntent> { _, _ ->
    val restaurants = repository.getRestaurants()

    RestaurantsIntent.LoadRestaurants(restaurants)
}
