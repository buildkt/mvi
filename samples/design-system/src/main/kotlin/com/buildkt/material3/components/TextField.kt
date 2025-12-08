package com.buildkt.material3.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import com.buildkt.material3.ExtendedMaterialTheme

/**
 * A themed, opinionated wrapper around Material 3's `OutlinedTextField`, designed to be the
 * default text field implementation within the buildkt design system.
 *
 * This component simplifies text field usage by being pre-configured with colors and shapes
 * from the [ExtendedMaterialTheme]. It enforces a consistent look and feel for all text
 * input across the application. It also provides built-in support for displaying helper
 * text and error messages.
 *
 * @param value The input text to be shown in the text field.
 * @param onValueChange The callback that is triggered when the input service updates the text. An
 *                      updated text comes as a parameter of the callback.
 * @param modifier The [Modifier] to be applied to this text field.
 * @param label A short descriptive label that appears inside the text field when it is empty and
 *              floats above it when it has content.
 * @param enabled Controls the enabled state of the text field. When `false`, the text field will be
 *                uneditable and visually disabled.
 * @param supportingText Optional helper text to be displayed below the text field. This is hidden
 *                       if [errorText] is not null.
 * @param errorText Optional error text to be displayed below the text field. If not null, the
 *                  field will be rendered in an error state.
 * @param keyboardOptions Software keyboard options that contains configuration such as [androidx.compose.ui.text.input.KeyboardType].
 * @param visualTransformation Transforms the visual representation of the input value. Useful for
 *                             password fields.
 * @param leadingIcon An optional icon to be displayed at the beginning of the text field container.
 * @param trailingIcon An optional icon to be displayed at the end of the text field container.
 * @param colors The [TextFieldColors] to be used. Defaults to themed colors from [OutlinedTextFieldDefaults].
 */
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
    supportingText: String? = null,
    errorText: String? = null,
    minLines: Int = 1,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: TextFieldColors =
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
) {
    val isError = !errorText.isNullOrBlank()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        label = label?.let { { Text(it) } },
        isError = isError,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        minLines = minLines,
        singleLine = singleLine,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = MaterialTheme.shapes.medium,
        colors = colors,
        supportingText = {
            Column {
                // Determine which text to show below the field. Error text takes priority.
                val textToShow = if (isError) errorText else supportingText

                // Animate the appearance of the supporting/error text.
                AnimatedVisibility(
                    visible = !textToShow.isNullOrBlank(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Text(
                        text = textToShow!!,
                        color =
                            if (isError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
    )
}
