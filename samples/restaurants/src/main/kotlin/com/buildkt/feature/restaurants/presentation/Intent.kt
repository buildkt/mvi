package com.buildkt.feature.restaurants.presentation

import androidx.paging.PagingData
import com.buildkt.feature.restaurants.domain.RestaurantInfo
import kotlinx.coroutines.flow.Flow

sealed interface RestaurantsIntent {

    data object PaneLaunched : RestaurantsIntent

    class LoadRestaurants(val restaurants: Flow<PagingData<RestaurantInfo>>) : RestaurantsIntent

    data class RestaurantSelected(val restaurantId: Int) : RestaurantsIntent
}

