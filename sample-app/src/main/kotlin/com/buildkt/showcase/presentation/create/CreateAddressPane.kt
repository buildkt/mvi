package com.buildkt.showcase.presentation.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.buildkt.showcase.presentation.theme.ExtendedMaterialTheme
import com.buildkt.showcase.presentation.theme.components.Button
import com.buildkt.showcase.presentation.theme.components.ScreenScaffold
import com.buildkt.showcase.presentation.theme.components.Text
import com.buildkt.showcase.presentation.theme.components.TextField
import com.buildkt.showcase.presentation.theme.components.TextStyle
import com.buildkt.showcase.presentation.theme.tokens.spacers
import com.buildkt.mvi.MviScreen
import com.buildkt.mvi.android.CollectUiEvents
import com.buildkt.mvi.android.UiEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
@MviScreen(
    uiState = CreateAddressUiState::class,
    intent = CreateAddressIntent::class
)
fun CreateAddressPane(
    state: CreateAddressUiState,
    onIntent: (intent: CreateAddressIntent) -> Unit,
    uiEvents: Flow<UiEvent>,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    CollectUiEvents(uiEvents, snackbarHostState)

    ScreenScaffold(
        modifier = modifier,
        title = "New Address",
        isLoading = state.isLoading,
        onBackClicked = { onIntent(CreateAddressIntent.BackClicked) },
        snackbarHostState = snackbarHostState,
        bottomBar = {
            Button(
                onClick = { onIntent(CreateAddressIntent.SaveAddress) },
                enabled = state.isFormValid,
                isLoading = false, // TODO
                text = "Save Address",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(all = MaterialTheme.spacers.medium),
            )
        },
        content = { contentPadding ->
            ContentAddress(
                state = state,
                onIntent = onIntent,
                modifier =
                    Modifier
                        .padding(paddingValues = contentPadding)
                        .padding(all = MaterialTheme.spacers.medium),
            )
        },
    )
}

@Composable
private fun ContentAddress(
    state: CreateAddressUiState,
    onIntent: (CreateAddressIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(space = MaterialTheme.spacers.medium),
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState()),
    ) {
        TextField(
            value = state.street,
            onValueChange = { onIntent(CreateAddressIntent.StreetChanged(value = it)) },
            label = "Street and house number",
            supportingText = "e.g., Keizersgracht 42",
            errorText = state.streetError,
            leadingIcon = { Icon(imageVector = Icons.Outlined.Business, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            value = state.city,
            onValueChange = { onIntent(CreateAddressIntent.CityChanged(value = it)) },
            label = "City",
            supportingText = "e.g., Amsterdam",
            errorText = state.cityError,
            leadingIcon = { Icon(imageVector = Icons.Outlined.LocationCity, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            value = state.zip,
            onValueChange = { onIntent(CreateAddressIntent.ZipChanged(value = it)) },
            label = "ZIP code",
            supportingText = "e.g., 1015 CA",
            errorText = state.zipError,
            leadingIcon = { Icon(imageVector = Icons.Outlined.Map, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            value = state.country,
            onValueChange = { onIntent(CreateAddressIntent.CountryChanged(value = it)) },
            label = "Country",
            supportingText = "e.g., Netherlands",
            errorText = state.countryError,
            leadingIcon = { Icon(imageVector = Icons.Outlined.Public, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.saveErrorMessage != null) {
            Text(
                text = state.saveErrorMessage,
                style = TextStyle.BodySubtle,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview(name = "Create Address Pane")
@Composable
private fun Preview() {
    ExtendedMaterialTheme {
        CreateAddressPane(state = CreateAddressUiState(), onIntent = { }, uiEvents = emptyFlow())
    }
}
