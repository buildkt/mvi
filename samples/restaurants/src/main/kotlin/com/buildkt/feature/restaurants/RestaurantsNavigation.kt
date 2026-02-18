package com.buildkt.feature.restaurants

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.buildkt.feature.restaurants.data.RestaurantRepositoryImpl
import com.buildkt.feature.restaurants.domain.RestaurantRepository
import com.buildkt.feature.restaurants.presentation.RestaurantsIntent
import com.buildkt.feature.restaurants.presentation.RestaurantsIntent.RestaurantSelected
import com.buildkt.feature.restaurants.presentation.RestaurantsUiState
import com.buildkt.feature.restaurants.presentation.detail.loadPaginatedMenus
import com.buildkt.feature.restaurants.presentation.detail.loadRestaurantInfo
import com.buildkt.feature.restaurants.presentation.detail.restaurantDetailPane
import com.buildkt.feature.restaurants.presentation.detail.restaurantDetailReducer
import com.buildkt.feature.restaurants.presentation.loadPaginatedRestaurants
import com.buildkt.feature.restaurants.presentation.restaurantsPane
import com.buildkt.feature.restaurants.presentation.restaurantsReducer
import com.buildkt.mvi.android.logMiddleware
import com.buildkt.mvi.android.navigateToRoute
import com.buildkt.mvi.parallelSideEffect

fun NavGraphBuilder.restaurantsFlowNavigation(
    navController: NavController,
    route: String,
    restaurantRepository: RestaurantRepository,
) = navigation(
    route = route,
    startDestination = LISTING_PANE_ROUTE,
) {
    restaurantsPane(navController = navController, route = LISTING_PANE_ROUTE) {
        middlewares += logMiddleware()

        reducer = restaurantsReducer()

        sideEffects {
            paneLaunched = loadPaginatedRestaurants(repository = restaurantRepository)

            restaurantSelected = navigateToRoute<RestaurantsUiState, RestaurantsIntent, RestaurantSelected> { _, intent ->
                detailPaneRoute(restaurantId = intent.restaurantId)
            }
        }
    }
    restaurantDetailPane(navController, route = DETAIL_PANE_ROUTE) {
        middlewares += logMiddleware()

        reducer = restaurantDetailReducer()

        sideEffects {
            paneLaunched = parallelSideEffect(
                loadRestaurantInfo(repository = RestaurantRepositoryImpl()),
                loadPaginatedMenus(repository = RestaurantRepositoryImpl())
            )
        }
    }
}

const val RESTAURANTS_FLOW_ROUTE = "restaurant"

private const val LISTING_PANE_ROUTE = "restaurant/listing"
private const val DETAIL_PANE_ROUTE = "restaurant?restaurantId={restaurantId}"

private fun detailPaneRoute(restaurantId: Int) = DETAIL_PANE_ROUTE.replace(oldValue = "{restaurantId}", newValue = restaurantId.toString())
