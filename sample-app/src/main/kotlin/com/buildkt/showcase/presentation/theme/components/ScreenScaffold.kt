package com.buildkt.showcase.presentation.theme.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A high-level, opinionated screen scaffold that standardizes the layout for most feature screens
 * within a buildkt application.
 *
 * This component encapsulates a `Scaffold`, a `TopAppBar`, and a full-screen loading indicator,
 * providing a consistent structure for loading states, navigation, and content display.
 * It simplifies feature development by handling the common screen boilerplate.
 *
 * @param modifier The [Modifier] to be applied to the underlying `Scaffold`.
 * @param title The text to be displayed in the `TopAppBar`.
 * @param isLoading Whether the screen is in a loading state. When `true`, a centered
 * [LoadingIndicator] is displayed over the content area. Defaults to `false`.
 * @param onBackClicked An optional callback to be invoked when the back navigation icon in the
 * `TopAppBar` is clicked. If `null`, the navigation icon is not shown.
 * @param floatingActionButton An optional composable for a floating action button.
 * @param snackbarHostState An optional state for showing snackbars.
 * @param content The main content of the screen to be displayed when not loading. It is provided
 * with a `PaddingValues` that accounts for the `TopAppBar`'s height.
 */
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    title: String,
    isLoading: Boolean = false,
    onBackClicked: (() -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = title,
                onBackClicked = onBackClicked,
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = FabPosition.End,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Crossfade(
            targetState = isLoading,
            animationSpec = tween(durationMillis = 300),
            label = "ScreenScaffoldLoading",
        ) { screenIsLoading ->
            if (screenIsLoading) {
                LoadingIndicator()
            } else {
                content(paddingValues)
            }
        }
    }
}
