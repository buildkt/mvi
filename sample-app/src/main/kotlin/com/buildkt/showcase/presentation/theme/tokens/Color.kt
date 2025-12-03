package com.buildkt.showcase.presentation.theme.tokens

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Private base colors used to define the custom color schemes.
private val Pink40 = Color(0xFF7D5260)
private val Pink80 = Color(0xFFEFB8C8)
private val Purple40 = Color(0xFF6650a4)
private val Purple80 = Color(0xFFD0BCFF)
private val PurpleGrey40 = Color(0xFF625b71)
private val PurpleGrey80 = Color(0xFFCCC2DC)

/**
 * The dark color scheme for the buildkt design system.
 *
 * This is used by `ExtendedMaterialTheme` when the system is in dark mode and dynamic color is disabled.
 * It is marked as `internal` as it should only be accessed via the theme.
 */
internal val DarkColorScheme =
    darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80,
    )

/**
 * The light color scheme for the buildkt design system.
 *
 * This is used by `ExtendedMaterialTheme` when the system is not in dark mode and dynamic color is disabled.
 * It is marked as `internal` as it should only be accessed via the theme.
 */
internal val LightColorScheme =
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
    )
