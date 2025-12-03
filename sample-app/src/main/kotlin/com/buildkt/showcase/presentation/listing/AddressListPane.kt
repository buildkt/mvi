package com.buildkt.showcase.presentation.listing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.buildkt.showcase.domain.Address
import com.buildkt.showcase.presentation.theme.ExtendedMaterialTheme
import com.buildkt.showcase.presentation.theme.components.Button
import com.buildkt.showcase.presentation.theme.components.EmptyState
import com.buildkt.showcase.presentation.theme.components.ListItem
import com.buildkt.showcase.presentation.theme.components.ListItemPlaceholder
import com.buildkt.showcase.presentation.theme.components.ScreenScaffold
import com.buildkt.showcase.presentation.theme.components.Text
import com.buildkt.showcase.presentation.theme.components.TextStyle
import com.buildkt.showcase.presentation.theme.components.placeholder
import com.buildkt.showcase.presentation.theme.tokens.spacers
import com.buildkt.mvi.MviScreen
import com.buildkt.mvi.android.CollectUiEvents
import com.buildkt.mvi.android.UiEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

@MviScreen(
    uiState = AddressListUiState::class,
    intent = AddressListIntent::class
)
@Composable
fun AddressListPane(
    state: AddressListUiState,
    onIntent: (AddressListIntent) -> Unit,
    uiEvents: Flow<UiEvent>,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) { onIntent(AddressListIntent.PaneLaunched) }
    val snackbarHostState = remember { SnackbarHostState() }
    CollectUiEvents(uiEvents, snackbarHostState)

    ScreenScaffold(
        title = "Addresses",
        onBackClicked = { onIntent(AddressListIntent.BackClicked) },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        floatingActionButton = {
            Button(
                text = "New address",
                onClick = { onIntent(AddressListIntent.AddNewAddress) },
                leadingIcon = Icons.Default.Add,
            )
        },
    ) { paddingValues ->
        val addresses by state.addresses.collectAsState(initial = emptyList())

        when {
            state.isLoading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    userScrollEnabled = false,
                ) {
                    item {
                        Box(
                            modifier =
                                Modifier
                                    .padding(
                                        horizontal = MaterialTheme.spacers.medium,
                                        vertical = MaterialTheme.spacers.medium,
                                    ).fillMaxWidth(fraction = 0.4f)
                                    .height(height = 24.dp)
                                    .placeholder(),
                        )
                    }
                    items(count = 4) {
                        ListItemPlaceholder()
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                        )
                    }
                }
            }
            addresses.isEmpty() -> {
                EmptyState(
                    title = "No addresses yet",
                    description = "When you add a new address, it will appear here.",
                    icon = Icons.Outlined.Inbox,
                    modifier = Modifier.padding(paddingValues),
                )
            }
            else -> {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                ) {
                    item {
                        Text(
                            text = "Saved addresses",
                            style = TextStyle.Title,
                            modifier = Modifier.padding(all = MaterialTheme.spacers.medium),
                        )
                    }
                    items(items = addresses) { address ->
                        // TODO key = { it.address.id }
                        AddressItemRow(
                            address = address,
                            onAddressClicked = { onIntent(AddressListIntent.AddressSelected(addressId = address.address.id)) },
                            onEditClicked = { onIntent(AddressListIntent.EditAddress(addressId = address.address.id)) },
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressItemRow(
    address: AddressListUiState.AddressItem,
    onAddressClicked: () -> Unit,
    onEditClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        onClick = onAddressClicked,
        leadingContent = {
            val tint = if (address.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Address",
                modifier = Modifier.size(size = 24.dp),
                tint = tint,
            )
        },
        trailingContent = {
            IconButton(onClick = onEditClicked) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit address",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        content = {
            val primaryColor = if (address.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            val secondaryColor = if (address.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            Text(
                text = address.address.street,
                style = TextStyle.Body,
                color = primaryColor,
            )
            Text(
                text = address.address.city,
                style = TextStyle.BodySubtle,
                color = secondaryColor,
            )
        },
    )
}

@Preview(name = "Loaded State")
@Composable
private fun AddressListPanePreview() =
    ExtendedMaterialTheme {
        AddressListPane(
            state =
                AddressListUiState(
                    isLoading = false,
                    addresses =
                        flowOf(
                            listOf(
                                AddressListUiState.AddressItem(
                                    address = Address(
                                        1,
                                        "Main Street 123",
                                        "Berlin, 10115",
                                        "",
                                        ""
                                    ),
                                    isSelected = true,
                                ),
                                AddressListUiState.AddressItem(
                                    address = Address(
                                        2,
                                        "Alexanderplatz 1",
                                        "Berlin, 10178",
                                        "",
                                        ""
                                    ),
                                    isSelected = false,
                                ),
                                AddressListUiState.AddressItem(
                                    address = Address(3, "Side Alley 45", "Hamburg, 20457", "", ""),
                                    isSelected = false,
                                ),
                                AddressListUiState.AddressItem(
                                    address = Address(
                                        4,
                                        "Ilpendamstraat 10",
                                        "Zaandam, 1507 JZ",
                                        "",
                                        ""
                                    ),
                                    isSelected = false,
                                ),
                            ),
                        ),
                ),
            onIntent = {},
            uiEvents = emptyFlow(),
        )
    }

@Preview(name = "Loading State")
@Composable
private fun AddressListPaneLoadingPreview() =
    ExtendedMaterialTheme {
        AddressListPane(
            state = AddressListUiState(isLoading = true),
            onIntent = {},
            uiEvents = emptyFlow(),
        )
    }

@Preview(name = "Empty State")
@Composable
private fun AddressListPaneEmptyPreview() =
    ExtendedMaterialTheme {
        AddressListPane(
            state =
                AddressListUiState(
                    isLoading = false,
                    addresses = flowOf(value = emptyList()),
                ),
            onIntent = {},
            uiEvents = emptyFlow(),
        )
    }
