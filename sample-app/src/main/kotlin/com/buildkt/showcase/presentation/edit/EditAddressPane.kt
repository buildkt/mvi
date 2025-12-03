package com.buildkt.showcase.presentation.edit

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.buildkt.mvi.NavArgument
import com.buildkt.mvi.android.UiEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
@MviScreen(
    uiState = EditAddressUiState::class,
    intent = EditAddressIntent::class
)
fun EditAddressPane(
    @NavArgument addressId: Long,
    state: EditAddressUiState,
    onIntent: (EditAddressIntent) -> Unit,
    uiEvents: Flow<UiEvent>,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(addressId) {
        onIntent(EditAddressIntent.LoadAddress(addressId))
    }

    ScreenScaffold(
        modifier = modifier,
        title = "Edit Address",
        isLoading = state.isLoading,
        onBackClicked = { onIntent(EditAddressIntent.BackClicked) },
        bottomBar = {
            Button(
                onClick = { onIntent(EditAddressIntent.EditAddress) },
                enabled = state.isFormValid,
                isLoading = false, // TODO
                text = "Update Address",
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
    state: EditAddressUiState,
    onIntent: (EditAddressIntent) -> Unit,
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
            onValueChange = { onIntent(EditAddressIntent.StreetChanged(value = it)) },
            label = "Street and house number",
            supportingText = "e.g., Keizersgracht 42",
            errorText = state.streetError,
            leadingIcon = { Icon(imageVector = Icons.Outlined.Business, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            value = state.city,
            onValueChange = { onIntent(EditAddressIntent.CityChanged(value = it)) },
            label = "City",
            supportingText = "e.g., Amsterdam",
            errorText = state.cityError,
            leadingIcon = { Icon(imageVector = Icons.Outlined.LocationCity, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            value = state.zip,
            onValueChange = { onIntent(EditAddressIntent.ZipChanged(value = it)) },
            label = "ZIP code",
            supportingText = "e.g., 1015 CA",
            errorText = state.zipError,
            leadingIcon = { Icon(imageVector = Icons.Outlined.Map, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            value = state.country,
            onValueChange = { onIntent(EditAddressIntent.CountryChanged(value = it)) },
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

@Preview(name = "Edit Address Pane")
@Composable
private fun Preview() =
    ExtendedMaterialTheme {
        EditAddressPane(
            addressId = 123,
            state =
                EditAddressUiState(
                    addressId = 123,
                    street = "Keizersgracht 42",
                    city = "Amsterdam",
                    zip = "1015 CA",
                    country = "Netherlands",
                ),
            onIntent = { },
            uiEvents = emptyFlow(),
        )
    }
