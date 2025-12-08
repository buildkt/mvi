package com.buildkt.material3.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.buildkt.material3.ExtendedMaterialTheme
import com.buildkt.material3.tokens.spacers

/**
 * A composable for displaying a generic error state, typically after a failed network call.
 * It always includes a "Retry" button to allow the user to re-trigger the action.
 *
 * @param title The main error message, e.g., "Something went wrong".
 * @param description A more detailed explanation of the error.
 * @param onRetryClick The lambda to be invoked when the "Retry" button is clicked.
 * @param icon The icon to display. Defaults to a "cloud off" icon.
 * @param modifier The modifier to be applied to the Column.
 */
@Composable
fun ErrorState(
    title: String,
    description: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.CloudOff,
) {
    val spacers = MaterialTheme.spacers
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(spacers.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Decorative
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(spacers.large))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(spacers.small))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(spacers.extraLarge))
        Button(onClick = onRetryClick) {
            Text(text = "Retry")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() =
    ExtendedMaterialTheme {
        ErrorState(
            title = "Connection Failed",
            description = "We couldn't connect to our servers. Please check your internet connection and try again.",
            onRetryClick = {},
        )
    }
