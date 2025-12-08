package com.buildkt.feature.restaurants.presentation

import com.buildkt.mvi.Reducer

fun restaurantsReducer() = Reducer<RestaurantsUiState, RestaurantsIntent> { state, intent ->
    when (intent) {
        is RestaurantsIntent.PaneLaunched -> state
        is RestaurantsIntent.LoadRestaurants -> state.copy(restaurants = intent.restaurants)
        is RestaurantsIntent.RestaurantSelected -> state
    }
}
