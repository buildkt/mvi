package com.buildkt.showcase.presentation.theme.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.buildkt.showcase.presentation.theme.ExtendedMaterialTheme
import com.buildkt.showcase.presentation.theme.tokens.spacers

/**
 * An enumeration of the available styles for a [Button].
 *
 * Each variant maps to a specific Material 3 button type and provides opinionated
 * color defaults from the [ExtendedMaterialTheme].
 */
sealed interface ButtonStyle {
    /** A high-emphasis, filled button. Maps to Material 3 `Button`. */
    data object Primary : ButtonStyle

    /** A medium-emphasis, outlined button. Maps to Material 3 `OutlinedButton`. */
    data object Outlined : ButtonStyle

    /** A low-emphasis, text-only button. Maps to Material 3 `TextButton`. */
    data object Text : ButtonStyle
}

/**
 * A themed, opinionated wrapper around Material 3 buttons, designed to be the default
 * button implementation within the buildkt design system.
 *
 * This component simplifies button usage by providing predefined styles ([ButtonStyle])
 * that automatically apply the correct colors and typography. It also includes built-in
 * support for a loading state, displaying a progress indicator and disabling the button.
 *
 * @param onClick The callback to be invoked when this button is clicked.
 * @param modifier The [Modifier] to be applied to this button.
 * @param style The [ButtonStyle] variant to use for this button. Defaults to [ButtonStyle.PRIMARY].
 * @param isLoading Whether the button is in a loading state. When `true`, it displays a
 *                  progress indicator and is disabled. Defaults to `false`.
 * @param enabled Controls the enabled state of the button. When `false`, this button will not
 *                be clickable. This is overridden by `isLoading`.
 * @param content The composable content to be displayed inside the button when not loading.
 */
@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    // The button is effectively disabled if it's in the loading state or explicitly disabled.
    val isEffectivelyEnabled = enabled && !isLoading

    val buttonContent: @Composable RowScope.() -> Unit = {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "button_loading_animation",
        ) { loading ->
            if (loading) {
                Box(
                    contentAlignment = Alignment.Center,
                    // Use default button text style to measure and match the height.
                    modifier = Modifier.size(ButtonDefaults.MinHeight),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        strokeCap = StrokeCap.Round,
                    )
                }
            } else {
                content()
            }
        }
    }

    when (style) {
        ButtonStyle.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = isEffectivelyEnabled,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                content = buttonContent,
            )
        }
        ButtonStyle.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = isEffectivelyEnabled,
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                content = buttonContent,
            )
        }
        ButtonStyle.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = isEffectivelyEnabled,
                colors =
                    ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                content = buttonContent,
            )
        }
    }
}

/**
 * An overload of [Button] that accepts a simple [String] for its content.
 *
 * This is a convenience function for the common use case of a button with only a text label.
 *
 * @param onClick The callback to be invoked when this button is clicked.
 * @param text The string label to be displayed inside the button.
 * @param modifier The [Modifier] to be applied to this button.
 * @param style The [ButtonStyle] variant to use for this button. Defaults to [ButtonStyle.Primary].
 * @param leadingIcon An optional [ImageVector] to be displayed at the start of the button's content.
 * @param isLoading Whether the button is in a loading state. When `true`, it displays a
 *                  progress indicator and is disabled. Defaults to `false`.
 * @param enabled Controls the enabled state of the button. When `false`, this button will not be clickable.
 */
@Composable
fun Button(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    leadingIcon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        style = style,
        isLoading = isLoading,
        enabled = enabled,
    ) {
        Row(
            modifier = Modifier.defaultMinSize(minHeight = ButtonDefaults.MinHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = MaterialTheme.spacers.extraSmall),
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
