package com.buildkt.material3.tokens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A data class representing the set of spacing tokens for the design system.
 *
 * These tokens provide a consistent scale for margins, padding, and spacers throughout the application,
 * ensuring visual rhythm and harmony. Marked as [Immutable] for Compose performance optimizations.
 */
@Immutable
data class Spacers(
    val none: Dp,
    val extraSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
)

/**
 * An extension property on [MaterialTheme] that provides convenient access to the current [Spacers]
 * instance from within any Composable.
 *
 * Example usage: `Spacer(modifier = Modifier.height(MaterialTheme.spacers.medium))`
 */
val MaterialTheme.spacers: Spacers
    @Composable get() = LocalSpacers.current

/**
 * A [CompositionLocal] that holds the current [Spacers] instance for the theme.
 * This allows the `ExtendedMaterialTheme` to provide the spacing tokens down the composition tree.
 * It is marked as `internal` as it's an implementation detail of the theming system.
 */
internal val LocalSpacers = staticCompositionLocalOf { DefaultSpacers }

/**
 * The default set of spacing values for the buildkt design system.
 * Used as a fallback and as the default for `ExtendedMaterialTheme`.
 * It is marked as `internal` as it's an implementation detail.
 */
internal val DefaultSpacers =
    Spacers(
        none = 0.dp,
        extraSmall = 4.dp,
        small = 8.dp,
        medium = 16.dp,
        large = 32.dp,
        extraLarge = 48.dp,
    )
