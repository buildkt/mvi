package com.buildkt.feature.restaurants.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.buildkt.feature.restaurants.domain.RestaurantRepository
import com.buildkt.feature.restaurants.domain.MenuItem
import com.buildkt.feature.restaurants.domain.RestaurantInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class RestaurantRepositoryImpl : RestaurantRepository {

    override fun getRestaurants(): Flow<PagingData<RestaurantInfo>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { RestaurantsPagingSource() }
    ).flow

    override suspend fun getRestaurantInfo(restaurantId: Int): RestaurantInfo {
        delay(timeMillis = 2000) // Simulate network latency

        return RestaurantInfo(
            id = restaurantId,
            name = "Restaurant $restaurantId",
            deliveryFee = "€1.99",
            deliveryInfo = "€0 Delivery Fee • Uber One • 1.9 km",
            deliveryTime = "${10 + (restaurantId.toInt() % 20)} min",
            minOrderValue = "Min. order value for this store is €$restaurantId",
            rating = "4.${restaurantId.toInt() % 10}",
            reviewCount = "100+",
            imageUrl = "https://picsum.photos/seed/$restaurantId/400/200",
        )
    }

    override fun getRestaurantMenus(restaurantId: Int): Flow<PagingData<MenuItem>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { MenusPagingSource(restaurantId = restaurantId) }
    ).flow

}