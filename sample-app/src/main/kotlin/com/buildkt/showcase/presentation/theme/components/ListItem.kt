package com.buildkt.showcase.presentation.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.buildkt.showcase.presentation.theme.ExtendedMaterialTheme
import com.buildkt.showcase.presentation.theme.tokens.spacers

/**
 * A highly reusable and composable list item component for the buildkt design system.
 *
 * It provides a standardized structure for rows within a `LazyColumn`, ensuring visual consistency
 * across different feature screens. It is built with slots to offer maximum flexibility for
 * its leading, main, and trailing content.
 *
 * @param modifier The [Modifier] to be applied to the root of the list item.
 * @param onClick An optional callback to be invoked when the list item is clicked. If provided,
 *                the item will have a clickable effect.
 * @param leadingContent An optional composable slot for content to be displayed at the start of the
 *                       list item, typically an `Icon` or `Avatar`.
 * @param trailingContent An optional composable slot for content to be displayed at the end of the
 *                        list item, such as a `Switch`, `Checkbox`, or trailing icon.
 * @param content The main content of the list item, typically containing one or more `Text`
 *                composables for the title and subtitle. This content will occupy the central
 *                space of the row.
 */
@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leadingContent: @Composable (RowScope.() -> Unit)? = null,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val clickableModifier =
        if (onClick != null) {
            Modifier.clickable(onClick = onClick)
        } else {
            Modifier
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.medium)
                .then(other = clickableModifier)
                .padding(
                    vertical = MaterialTheme.spacers.small,
                    horizontal = MaterialTheme.spacers.medium,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingContent != null) {
            leadingContent()
            Spacer(modifier = Modifier.width(width = MaterialTheme.spacers.medium))
        }

        // Main content area that takes up the available space
        Column(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(space = MaterialTheme.spacers.extraSmall),
        ) {
            content()
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(width = MaterialTheme.spacers.medium))
            trailingContent()
        }
    }
}

@Preview
@Composable
private fun ListItemPreview() =
    ExtendedMaterialTheme {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Address",
                    modifier = Modifier.size(size = 24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingContent = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit address",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            content = {
                Text(
                    text = "Ilpendamstraat 10",
                    style = TextStyle.Body,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Zaandam",
                    style = TextStyle.BodySubtle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }
