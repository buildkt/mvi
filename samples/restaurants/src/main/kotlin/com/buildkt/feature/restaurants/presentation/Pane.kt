package com.buildkt.feature.restaurants.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.buildkt.feature.restaurants.domain.RestaurantInfo
import com.buildkt.feature.restaurants.presentation.RestaurantsIntent.RestaurantSelected
import com.buildkt.material3.ExtendedMaterialTheme
import com.buildkt.material3.components.Button
import com.buildkt.material3.components.LoadingIndicator
import com.buildkt.material3.components.Text
import com.buildkt.material3.components.TextStyle
import com.buildkt.material3.components.TopAppBar
import com.buildkt.material3.tokens.spacers
import com.buildkt.mvi.MviScreen
import com.buildkt.mvi.android.UiEvent
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.flow.Flow

@Composable
@MviScreen(
    uiState = RestaurantsUiState::class,
    intent = RestaurantsIntent::class
)
fun RestaurantsPane(
    state: RestaurantsUiState,
    onIntent: (intent: RestaurantsIntent) -> Unit,
    uiEvents: Flow<UiEvent>,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(true) { onIntent(RestaurantsIntent.PaneLaunched) }

    val restaurantItems = state.restaurants.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = "Restaurants") },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = contentPadding),
            contentPadding = PaddingValues(all = MaterialTheme.spacers.medium),
            verticalArrangement = Arrangement.spacedBy(space = MaterialTheme.spacers.medium)
        ) {
            when (val refreshState = restaurantItems.loadState.refresh) {
                is LoadState.Loading -> {
                    items(5) {
                        RestaurantCardPlaceholder()
                    }
                }

                is LoadState.Error -> {
                    item {
                        ErrorItem(
                            message = "Could not load restaurants.",
                            onRetry = { restaurantItems.retry() },
                            modifier = Modifier.fillParentMaxSize()
                        )
                    }
                }

                else -> {
                    items(
                        count = restaurantItems.itemCount,
                        key = restaurantItems.itemKey { it.id }
                    ) { index ->
                        val restaurant = restaurantItems[index]
                        if (restaurant != null) {
                            RestaurantCard(
                                restaurant = restaurant,
                                onClick = { onIntent(RestaurantSelected(restaurantId = restaurant.id)) }
                            )
                        }
                    }

                    item {
                        when (restaurantItems.loadState.append) {
                            is LoadState.Loading -> LoadingIndicator()
                            is LoadState.Error -> ErrorItem(
                                message = "Could not load more.",
                                onRetry = { restaurantItems.retry() },
                                modifier = Modifier.fillMaxWidth()
                            )

                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RestaurantCard(restaurant: RestaurantInfo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.clickable(onClick = onClick)) {
        Column {
            AsyncImage(
                model = ImageRequest
                    .Builder(LocalContext.current)
                    .data(restaurant.imageUrl)
                    .crossfade(enable = true)
                    .build(),
                contentDescription = "${restaurant.name} image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 140.dp)
                    .clip(shape = MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(all = MaterialTheme.spacers.medium),
                verticalArrangement = Arrangement.spacedBy(space = MaterialTheme.spacers.extraSmall)
            ) {
                Text(text = restaurant.name, style = TextStyle.Title)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${restaurant.deliveryFee} • ", style = TextStyle.BodySubtle)
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(size = 16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(text = "${restaurant.rating} • ", style = TextStyle.BodySubtle)
                    Text(text = restaurant.deliveryTime, style = TextStyle.BodySubtle)
                }
            }
        }
    }
}

@Composable
private fun RestaurantCardPlaceholder(modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 140.dp)
                    .placeholder(true, color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Column(
                modifier = Modifier.padding(all = MaterialTheme.spacers.medium),
                verticalArrangement = Arrangement.spacedBy(space = MaterialTheme.spacers.extraSmall)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(26.dp)
                        .placeholder(true, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(20.dp)
                        .placeholder(true, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
    }
}

@Composable
private fun ErrorItem(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(all = MaterialTheme.spacers.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(height = MaterialTheme.spacers.small))
        Button(onClick = onRetry, text = "Retry")
    }
}

@Composable
private fun Modifier.placeholder(
    enabled: Boolean,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    shape: Shape = MaterialTheme.shapes.small
): Modifier = if (enabled) {
    this
        .shimmer()
        .background(color = color, shape = shape)
} else {
    this
}

@Preview(showBackground = true, name = "Restaurants Pane Loaded")
@Composable
private fun RestaurantsPanePreview() {
//    ExtendedMaterialTheme {
//        val fakeRestaurants = flowOf(
//            PagingData.from(
//                listOf(
//                    RestaurantInfo("1", "Subway", "url", "€0.99 Delivery Fee", "4.4 (490+)", "10 min"),
//                    RestaurantInfo("2", "Bubble House", "url", "€1.49 Delivery Fee", "4.0 (10)", "18 min")
//                )
//            )
//        )
//        RestaurantsPane(state = RestaurantsUiState(restaurants = fakeRestaurants), onIntent = {})
//    }
}

@Preview(showBackground = true, name = "Restaurants Pane Loading")
@Composable
private fun RestaurantsPaneLoadingPreview() {
    ExtendedMaterialTheme {
        //val loadingRestaurants = flowOf(PagingData.empty<RestaurantInfo>(LoadState.Loading))
        //RestaurantsPane(state = RestaurantsUiState(restaurants = loadingRestaurants), onIntent = {})
    }
}
