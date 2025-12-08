package com.buildkt.material3.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * A themed, opinionated wrapper around Material 3's `TopAppBar`, designed to be the
 * standard app bar within the buildkt design system.
 *
 * This component is system-aware. It automatically detects its own background color and
 * updates the system status bar icons to ensure proper contrast (light icons on dark
 * backgrounds, and vice-versa), supporting a seamless edge-to-edge UI.
 *
 * @param title The text title to be displayed in the app bar.
 * @param modifier The [Modifier] to be applied to this app bar.
 * @param navigationIcon A composable slot for the navigation icon.
 * @param actions A composable slot for actions to be displayed at the end of the app bar.
 * @param colors The [TopAppBarColors] to be used. Defaults to colors from the theme.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
) {
    val view = LocalView.current
    val containerColor = colors.containerColor

    // This effect runs when the color changes or the view is disposed.
    // It is responsible for setting the status bar icon color.
    DisposableEffect(containerColor) {
        val window = (view.context as? android.app.Activity)?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            // Set icon color based on the luminance of the app bar's background.
            // isAppearanceLightStatusBars = true means "show dark icons".
            insetsController.isAppearanceLightStatusBars = containerColor.luminance() > 0.5f
        }
        onDispose {}
    }

    TopAppBar(
        title = { Text(text = title, style = TextStyle.Headline, color = colors.titleContentColor) },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
    )
}

/**
 * A convenience overload of [TopAppBar] that simplifies the common use case of
 * displaying a back arrow as the navigation icon.
 *
 * @param title The text title to be displayed in the app bar.
 * @param onBackClicked The lambda to be invoked when the back arrow is clicked. If this
 *                      lambda is null, the back arrow will not be displayed.
 * @param backArrowIcon The [ImageVector] to use for the back arrow.
 * @param modifier The [Modifier] to be applied to this app bar.
 * @param actions A composable slot for actions to be displayed at the end of the app bar.
 * @param colors The [TopAppBarColors] to be used. Defaults to colors from the theme.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: String,
    onBackClicked: (() -> Unit)?,
    modifier: Modifier = Modifier,
    backArrowIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = {
            if (onBackClicked != null) {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        imageVector = backArrowIcon,
                        contentDescription = "Back",
                    )
                }
            }
        },
        actions = actions,
        colors = colors,
    )
}
