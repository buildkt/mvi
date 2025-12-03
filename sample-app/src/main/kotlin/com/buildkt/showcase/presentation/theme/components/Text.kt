package com.buildkt.showcase.presentation.theme.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.buildkt.showcase.presentation.theme.ExtendedMaterialTheme

/**
 * A semantic enumeration of the primary text styles available in the buildkt design system.
 *
 * Using these styles ensures that text across the application is consistent and follows the
 * established typographic scale. Each case maps to a specific [TextStyle] from the
 * [ExtendedMaterialTheme].
 */
sealed interface TextStyle {
    /** A primary, high-emphasis style for screen headlines. Maps to `MaterialTheme.typography.headlineSmall`. */
    data object Headline : TextStyle

    /** A primary, high-emphasis style for screen titles. Maps to `MaterialTheme.typography.titleLarge`. */
    data object Title : TextStyle

    /** The default, main style for body content. Maps to `MaterialTheme.typography.bodyLarge`. */
    data object Body : TextStyle

    /** A secondary, de-emphasized style for helper text or captions. Maps to `MaterialTheme.typography.bodyMedium`. */
    data object BodySubtle : TextStyle
}

/**
 * An opinionated wrapper for the Material 3 [Text] composable that enforces the buildkt design
 * system's typography and color standards.
 *
 * This component should be used in place of the standard [Text] composable for all static
 * text to ensure consistency. It provides a simplified API by using semantic styles defined
 * in [TextStyle] and applying a smart default color based on the chosen style.
 *
 * @param text The text to be displayed.
 * @param modifier The [Modifier] to be applied to this text component.
 * @param style The semantic [TextStyle] to apply. Defaults to [TextStyle.Body].
 * @param color The color to apply to the text. Defaults to the recommended color for the
 *              given [style], but can be overridden.
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Body,
    color: Color = style.defaultColor(),
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.toTextStyle(),
        color = color,
        textAlign = textAlign,
    )
}

/**
 * Internal extension function that maps a semantic [TextStyle] to a concrete
 * [TextStyle] from the current [MaterialTheme].
 *
 * This encapsulates the design system's typographic decisions in one place.
 *
 * @return The corresponding [TextStyle].
 */
@Composable
private fun TextStyle.toTextStyle(): androidx.compose.ui.text.TextStyle =
    when (this) {
        TextStyle.Headline -> MaterialTheme.typography.headlineSmall
        TextStyle.Title -> MaterialTheme.typography.titleLarge
        TextStyle.Body -> MaterialTheme.typography.bodyLarge
        TextStyle.BodySubtle -> MaterialTheme.typography.bodyMedium
    }

/**
 * Internal extension function that provides a smart default [Color] for a given
 * [TextStyle] based on the current [MaterialTheme].
 *
 * This ensures that text styles have appropriate and consistent colors by default.
 *
 * @return The recommended default [Color] for the style.
 */
@Composable
private fun TextStyle.defaultColor(): Color =
    when (this) {
        TextStyle.Headline -> MaterialTheme.colorScheme.onSurface
        TextStyle.Title -> MaterialTheme.colorScheme.onSurface
        TextStyle.Body -> MaterialTheme.colorScheme.onSurface
        TextStyle.BodySubtle -> MaterialTheme.colorScheme.onSurfaceVariant
    }
