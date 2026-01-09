package com.buildkt.feature.restaurants.presentation

import androidx.paging.PagingData
import com.buildkt.feature.restaurants.domain.RestaurantInfo
import com.buildkt.mvi.TriggersSideEffect
import kotlinx.coroutines.flow.Flow

sealed interface RestaurantsIntent {
    @TriggersSideEffect
    data object PaneLaunched : RestaurantsIntent
    @TriggersSideEffect
    class LoadRestaurants(val restaurants: Flow<PagingData<RestaurantInfo>>) : RestaurantsIntent
    @TriggersSideEffect
    data class RestaurantSelected(val restaurantId: Int) : RestaurantsIntent
}

