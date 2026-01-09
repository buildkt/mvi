package com.buildkt.feature.restaurants.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for restaurant-related data.
 * It abstracts away the data source (network, cache, etc.).
 */
interface RestaurantRepository {

    /**
     * Fetches a paginated stream of restaurants.
     */
    fun getRestaurants(): Flow<PagingData<RestaurantInfo>>

    /**
     * Fetches the detailed information for a specific restaurant.
     */
    suspend fun getRestaurantInfo(restaurantId: Int): RestaurantInfo

    /**
     * Fetches a paginated stream of menu items for a specific restaurant.
     */
    fun getRestaurantMenus(restaurantId: Int): Flow<PagingData<MenuItem>>
}