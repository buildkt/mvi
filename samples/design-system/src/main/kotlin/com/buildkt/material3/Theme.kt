package com.buildkt.material3

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.buildkt.material3.tokens.DarkColorScheme
import com.buildkt.material3.tokens.DefaultSpacers
import com.buildkt.material3.tokens.LightColorScheme
import com.buildkt.material3.tokens.LocalSpacers
import com.buildkt.material3.tokens.Spacers

/**
 * The primary theme for buildkt applications, providing an extended version of Material 3 theming.
 *
 * This Composable function simplifies theme setup by automatically handling dark/light modes
 * and dynamic color (on Android 12+). It also injects custom design tokens, like [Spacers],
 * into the composition tree, making them available throughout the app.
 *
 * @param darkTheme Whether the theme should be in dark mode. Defaults to the system setting.
 * @param dynamicColor Whether to use dynamic colors generated from the user's wallpaper.
 *                     Defaults to `true`. Has no effect on Android versions below 12.
 * @param spacers The set of custom spacing tokens to be used in the theme. Defaults to [DefaultSpacers].
 * @param content The Composable content to be themed.
 */
@Composable
fun ExtendedMaterialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Dynamic color is available on Android 12+
    spacers: Spacers = DefaultSpacers,
    content: @Composable () -> Unit,
) {
    ExtendedMaterialTheme(
        colorScheme =
            when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }

                darkTheme -> DarkColorScheme
                else -> LightColorScheme
            },
        spacers = spacers,
        content = content,
    )
}

/**
 * An advanced overload of [ExtendedMaterialTheme] that allows for explicit control over the theme's properties.
 *
 * This function is useful when you need to provide a completely custom [ColorScheme] instead of
 * relying on the automatic dark/light or dynamic color logic.
 *
 * @param colorScheme The exact [ColorScheme] to be applied.
 * @param spacers The set of custom spacing tokens to be used in the theme. Defaults to [DefaultSpacers].
 * @param content The Composable content to be themed.
 */
@Composable
fun ExtendedMaterialTheme(
    colorScheme: ColorScheme,
    spacers: Spacers = DefaultSpacers,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSpacers provides spacers,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
