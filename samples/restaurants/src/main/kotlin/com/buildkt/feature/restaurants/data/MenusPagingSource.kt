package com.buildkt.feature.restaurants.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.buildkt.feature.restaurants.domain.MenuItem
import kotlinx.coroutines.delay

class MenusPagingSource(
    private val restaurantId: Int
) : PagingSource<Int, MenuItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MenuItem> {
        val currentPage = params.key ?: 1
        delay(300) // Simulate network latency

        return try {
            LoadResult.Page(
                data = MockMenus(restaurantId),
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(throwable = e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MenuItem>): Int? = state.anchorPosition
}

private fun MockMenus(restaurantId: Int) = listOf(
    MenuItem("$restaurantId-1", "Fried mandu veggies", "Delicious crispy Korean dumplings with a vegetable filling. 6 pieces.", "€4.96", "€6.20", "url", listOf("20% OFF")),
    MenuItem("$restaurantId-2", "(Veg) Gochu NoChicken Coleslaw Bun", "Discover the flavors of Korea. Featuring a veggie NoChicken burger, gochujang may...", "€11.92", "€14.90", "url", listOf("(Veg)", "20% OFF")),
    MenuItem("$restaurantId-3", "(Veg) Kimchi NoChicken Cheese Bun", "A crispy veggie NoChicken burger coated in your choice of coating, gochujang may...", "€13.44", "€16.80", "url", listOf("(Veg)", "20% OFF")),
    MenuItem("$restaurantId-4", "Mochi Mango (2pc)", "Delicious rice dough filled with flavoured ice cream.", "€4.90", null, "url"),
    MenuItem("$restaurantId-5", "Crunchy Sweet Potato Wedges", "Sweet potato wedges in a crunchy coating", "€4.95", null, "url")
)