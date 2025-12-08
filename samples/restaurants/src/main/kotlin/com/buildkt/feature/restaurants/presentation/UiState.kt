package com.buildkt.feature.restaurants.presentation

import androidx.paging.PagingData
import com.buildkt.feature.restaurants.domain.RestaurantInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class RestaurantsUiState(
    val restaurants: Flow<PagingData<RestaurantInfo>> = emptyFlow()
)
