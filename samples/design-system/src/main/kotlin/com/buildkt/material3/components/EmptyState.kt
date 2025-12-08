package com.buildkt.material3.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
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
 * A composable for displaying a generic empty state.
 * Use this when a list or content area has no items to show.
 *
 * @param title The main message to display, e.g., "No addresses found".
 * @param description A more detailed explanation of the empty state.
 * @param icon The icon to display. Defaults to a "search off" icon.
 * @param actionText The text for the optional action button, e.g., "Add New Address".
 * @param onActionClick The lambda to be invoked when the action button is clicked. The button is only shown if this is not null.
 * @param modifier The modifier to be applied to the Column.
 */
@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.SearchOff,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val spacers = MaterialTheme.spacers
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(all = spacers.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Decorative
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.secondary,
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
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(spacers.extraLarge))
            Button(onClick = onActionClick) {
                Text(text = actionText)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() =
    ExtendedMaterialTheme {
        EmptyState(
            title = "No Items Found",
            description = "There are currently no items to display in this section. Try adding one!",
        )
    }

@Preview(showBackground = true)
@Composable
private fun EmptyStateWithActionPreview() =
    ExtendedMaterialTheme {
        EmptyState(
            title = "No Addresses",
            description = "You haven't added any delivery addresses yet.",
            actionText = "Add New Address",
            onActionClick = {},
        )
    }
