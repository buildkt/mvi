package com.buildkt.feature.restaurants.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.buildkt.feature.restaurants.domain.MenuItem
import com.buildkt.feature.restaurants.domain.RestaurantInfo
import com.buildkt.feature.restaurants.presentation.detail.RestaurantDetailIntent.PaneLaunched
import com.buildkt.material3.components.ScreenScaffold
import com.buildkt.material3.components.Text
import com.buildkt.material3.components.TextStyle
import com.buildkt.material3.tokens.spacers
import com.buildkt.mvi.MviScreen
import com.buildkt.mvi.NavArgument
import com.buildkt.mvi.android.CollectUiEvents
import com.buildkt.mvi.android.UiEvent
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@MviScreen(
    uiState = RestaurantDetailUiState::class,
    intent = RestaurantDetailIntent::class
)
fun RestaurantDetailPane(
    @NavArgument restaurantId: Int,
    state: RestaurantDetailUiState,
    onIntent: (RestaurantDetailIntent) -> Unit,
    uiEvents: Flow<UiEvent>,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(restaurantId) { onIntent(PaneLaunched(restaurantId)) }
    val snackbarHostState = remember { SnackbarHostState() }
    CollectUiEvents(uiEvents, snackbarHostState)

    ScreenScaffold(
        title = if (state.isRestaurantInfoLoading) "" else state.restaurantInfo.name,
        onBackClicked = { onIntent(RestaurantDetailIntent.BackClicked) },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    ) { paddingValues ->
        val menuItems = state.menuItems.collectAsLazyPagingItems()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = MaterialTheme.spacers.large)
        ) {
            item {
                RestaurantHeader(
                    isLoading = state.isRestaurantInfoLoading,
                    restaurant = state.restaurantInfo
                )
            }

            // Handle loading/error for the list itself
            when (menuItems.loadState.refresh) {
                is LoadState.Loading -> {
                    items(count = 20) {
                        MenuItemRowPlaceholder()
                        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
                is LoadState.Error -> {
                    item { Text(text = "Error loading menu.", modifier = Modifier.padding(MaterialTheme.spacers.medium)) }
                }
                else -> {
                    items(
                        count = menuItems.itemCount,
                        key = menuItems.itemKey { it.id }
                    ) { index ->
                        menuItems[index]?.let { item ->
                            MenuItemRow(
                                item = item,
                                onItemClicked = { onIntent(RestaurantDetailIntent.MenuItemSelected(menuItemId = item.id)) },
                            )
                            Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RestaurantHeader(
    isLoading: Boolean,
    restaurant: RestaurantInfo = RestaurantInfo(),
) {
    Column(
        modifier = Modifier.padding(all = MaterialTheme.spacers.medium)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 140.dp)
                .placeholder(enabled = isLoading, shape = MaterialTheme.shapes.medium)
                .clip(shape = MaterialTheme.shapes.medium)
        ) {
            if (!isLoading) {
                AsyncImage(
                    model = ImageRequest
                        .Builder(context = LocalContext.current)
                        .data(data = restaurant.imageUrl)
                        .crossfade(enable = true)
                        .build(),
                    contentDescription = "${restaurant.name} image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(height = MaterialTheme.spacers.medium))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = MaterialTheme.spacers.small)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                tint = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (isLoading) "   " else restaurant.rating,
                style = TextStyle.Body,
                modifier = Modifier.placeholder(enabled = isLoading)
            )

            Text(
                text = if (isLoading) "       " else "(${restaurant.reviewCount})",
                style = TextStyle.Body,
                modifier = Modifier.placeholder(enabled = isLoading)
            )
        }

        Spacer(modifier = Modifier.height(height = MaterialTheme.spacers.small))

        Text(
            text = if (isLoading) "                                      " else restaurant.deliveryInfo,
            style = TextStyle.BodySubtle,
            modifier =  Modifier.placeholder(enabled = isLoading)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacers.extraSmall))

        Text(
            text = if (isLoading) "               " else restaurant.minOrderValue,
            style = TextStyle.BodySubtle,
            modifier = Modifier.placeholder(enabled = isLoading)
        )
    }
}

@Composable
private fun MenuItemRow(
    item: MenuItem,
    onItemClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClicked)
            .padding(MaterialTheme.spacers.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(MaterialTheme.spacers.small))
            Text(item.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(MaterialTheme.spacers.small))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.price, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                item.originalPrice?.let {
                    Spacer(modifier = Modifier.width(MaterialTheme.spacers.small))
                    Text(it, style = MaterialTheme.typography.bodyMedium, textDecoration = TextDecoration.LineThrough, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (item.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacers.medium))
                Row {
                    item.tags.forEach { tag ->
                        Tag(text = tag, isHighlight = tag.contains("OFF"))
                        Spacer(modifier = Modifier.width(MaterialTheme.spacers.small))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(MaterialTheme.spacers.medium))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl).crossfade(true).build(),
            contentDescription = item.name,
            modifier = Modifier
                .size(100.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun MenuItemRowPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = MaterialTheme.spacers.medium),
        ) {
        Box(modifier = Modifier
            .fillMaxWidth(fraction = 0.6f)
            .height(20.dp)
            .placeholder(enabled = true)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacers.small))

        Box(modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(16.dp)
            .placeholder(enabled = true)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacers.extraSmall))

        Box(modifier = Modifier
            .fillMaxWidth(0.5f)
            .height(16.dp)
            .placeholder(enabled = true)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacers.small))

        Box(modifier = Modifier
            .fillMaxWidth(0.2f)
            .height(16.dp)
            .placeholder(enabled = true)
        )
    }
}

@Composable
private fun Tag(text: String, isHighlight: Boolean) {
    val backgroundColor = if (isHighlight) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isHighlight) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        text = text,
        color = textColor,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(backgroundColor, MaterialTheme.shapes.small)
            .padding(
                horizontal = MaterialTheme.spacers.small,
                vertical = MaterialTheme.spacers.extraSmall
            )
    )
}

@Composable
private fun Modifier.placeholder(
    enabled: Boolean,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    shape: Shape = MaterialTheme.shapes.small
): Modifier = if (enabled) this.shimmer().background(color = color, shape = shape) else this

@Preview(showBackground = true, name = "Loaded State")
@Composable
private fun RestaurantDetailPanePreview() {
//    ExtendedMaterialTheme {
//        val fakeMenu = flowOf(PagingData.from(listOf(
//            MenuItem("1", "Fried mandu veggies", "Delicious crispy Korean dumplings with a vegetable filling. 6 pieces.", "€4.96", "€6.20", "url", listOf("20% OFF"))
//        )))
//        RestaurantDetailPane(
//            restaurantId = "123",
//            state = RestaurantDetailUiState(
//                isRestaurantInfoLoading = false,
//                menuItems = fakeMenu,
//                restaurantInfo = RestaurantInfo(
//                    name = "Gochu Gang | Korean Fried Chicken",
//                    rating = "3.3",
//                    reviewCount = "100+",
//                    deliveryInfo = "€0 Delivery Fee • Uber One • 1.9 km",
//                    minOrderValue = "Min. order value for this store is €19.95"
//                ),
//            ),
//            onIntent = {}
//        )
//    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun RestaurantDetailPaneLoadingPreview() {
//    ExtendedMaterialTheme {
//        RestaurantDetailPane(
//            restaurantId = "123",
//            state = RestaurantDetailUiState(
//                isRestaurantInfoLoading = true,
//                menuItems = flowOf(PagingData.empty())
//            ),
//            onIntent = {}
//        )
//    }
}
