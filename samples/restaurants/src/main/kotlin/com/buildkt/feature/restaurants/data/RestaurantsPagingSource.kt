package com.buildkt.feature.restaurants.data

import androidx.paging.PagingSource
import com.buildkt.feature.restaurants.domain.RestaurantInfo
import kotlinx.coroutines.delay

class RestaurantsPagingSource : PagingSource<Int, RestaurantInfo>() {

    private val cache: MutableMap<Int, List<RestaurantInfo>> = emptyMap<Int, List<RestaurantInfo>>().toMutableMap()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RestaurantInfo> {
        val currentPage = params.key ?: 1

        return try {
            val cachedRestaurants = cache[currentPage]
            if (cachedRestaurants == null) {
                delay(timeMillis = 200) // Simulate network delay
                val restaurants = loadFakeRestaurants(currentPage, params)

                cache[currentPage] = restaurants
                load(params)
            } else {
                LoadResult.Page(
                    data = cachedRestaurants,
                    prevKey = if (currentPage == 1) null else currentPage - 1,
                    nextKey = currentPage + 1
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(throwable = e)
        }
    }

    override fun getRefreshKey(state: androidx.paging.PagingState<Int, RestaurantInfo>): Int? = state.anchorPosition

    private fun loadFakeRestaurants(currentPage: Int, params: LoadParams<Int>) = (1..params.loadSize).map {
        val id = (currentPage - 1) * params.loadSize + it

        RestaurantInfo(
            id = id,
            name = "Restaurant #$id",
            deliveryInfo = "€0 Delivery Fee • Uber One • 1.9 km",
            deliveryTime = "${10 + (id % 20)} min",
            deliveryFee = "€1.99",
            minOrderValue = "Min. order value for this store is €$id",
            rating = "4.${id % 10}",
            reviewCount = "100+",
            imageUrl = "https://picsum.photos/seed/$id/400/200",
        )
    }
}
