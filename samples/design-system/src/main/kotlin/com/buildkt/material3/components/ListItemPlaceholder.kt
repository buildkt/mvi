package com.buildkt.material3.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.buildkt.material3.ExtendedMaterialTheme
import com.buildkt.material3.tokens.spacers
import com.valentinilk.shimmer.shimmer

/**
 * A generic placeholder for a list item, typically used to build shimmer loading skeletons.
 * It mimics the common structure of a ListItem with leading, content, and trailing sections.
 *
 * @param modifier The modifier to be applied to the underlying ListItem.
 */
@Composable
fun ListItemPlaceholder(modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier,
        leadingContent = {
            Box(
                modifier =
                    Modifier
                        .size(size = 24.dp)
                        .placeholder(shape = MaterialTheme.shapes.extraLarge),
            )
        },
        trailingContent = {
            Box(
                modifier =
                    Modifier
                        .size(size = 24.dp)
                        .placeholder(shape = MaterialTheme.shapes.extraLarge),
            )
        },
        content = {
            // Placeholder for the main text line
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(fraction = 0.7f)
                        .height(height = 20.dp)
                        .placeholder(),
            )
            Spacer(Modifier.height(height = MaterialTheme.spacers.extraSmall))
            // Placeholder for the subtitle line
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(fraction = 0.4f)
                        .height(height = 16.dp)
                        .placeholder(),
            )
        },
    )
}

@Composable
fun Modifier.placeholder(
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    shape: Shape = MaterialTheme.shapes.small,
): Modifier = if (enabled) this.shimmer().background(color = color, shape = shape) else this

@Preview(showBackground = true)
@Composable
private fun ListItemPlaceholderPreview() =
    ExtendedMaterialTheme {
        ListItemPlaceholder()
    }
