package com.buildkt.showcase.presentation.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.buildkt.showcase.presentation.theme.ExtendedMaterialTheme
import com.buildkt.showcase.presentation.theme.tokens.spacers

/**
 * A themed, opinionated wrapper for Material 3's `CircularProgressIndicator`,
 * designed for displaying a prominent loading state.
 *
 * This component renders a progress indicator and optional supporting text within a
 * semi-transparent, rounded surface (HUD-style). It's ideal for overlaying on top of
 * content when a blocking operation is in progress.
 *
 * @param modifier The [Modifier] to be applied to the container Box.
 * @param supportingText Optional text to display below the progress indicator.
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Use a Surface to create a modern HUD (Heads-Up Display) effect.
        Surface(
            shape = RoundedCornerShape(size = MaterialTheme.spacers.medium),
            tonalElevation = MaterialTheme.spacers.extraSmall,
            shadowElevation = MaterialTheme.spacers.small,
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        horizontal = MaterialTheme.spacers.large,
                        vertical = MaterialTheme.spacers.medium,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = MaterialTheme.spacers.medium),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size = 48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = MaterialTheme.spacers.extraSmall,
                    strokeCap = StrokeCap.Round,
                )
                if (supportingText != null) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    ExtendedMaterialTheme {
        Surface { LoadingIndicator() }
    }
}

@PreviewLightDark
@Composable
private fun WithTextPreview() {
    ExtendedMaterialTheme {
        Surface { LoadingIndicator(supportingText = "Loading data...") }
    }
}
