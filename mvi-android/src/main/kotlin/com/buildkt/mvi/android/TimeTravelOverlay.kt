package com.buildkt.mvi.android

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.buildkt.mvi.StateSnapshot
import com.buildkt.mvi.TimeTravelDebugger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * A draggable floating toolbar for time-travel debugging that can be placed anywhere on screen.
 *
 * Features:
 * - Draggable toolbar that can be moved anywhere on the screen
 * - Horizontal toolbar with navigation controls
 * - Expandable to show state history list
 * - Navigation controls (previous/next) to restore state at a given index
 * - Save button persists current history to disk when [saveHistory] is provided.
 *   Load is handled elsewhere (e.g. at screen/ViewModel creation via [TimeTravelDebugger.loadHistory]).
 * - Collapsible UI
 *
 * State diff in the history list is best-effort and based on [Object.toString] (segment split by ", ").
 * Non–data-class states or custom toString() can make the diff misleading or noisy.
 *
 * @param timeTravelDebugger The TimeTravelDebugger instance that contains the TimeTravelMiddleware.
 * @param modifier Modifier for the overlay container.
 */
@Composable
fun <S, I : Any> TimeTravelOverlay(
    timeTravelDebugger: TimeTravelDebugger<S, I, NavigationEvent, UiEvent>,
    modifier: Modifier = Modifier,
    saveHistory: ((history: List<StateSnapshot<S, I>>) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    var isExpanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var toolbarSize by remember { mutableStateOf(Size.Zero) }
    var parentSize by remember { mutableStateOf(Size.Zero) }
    var initialPosition by remember { mutableStateOf<Offset?>(null) }

    val historyStateFlow = timeTravelDebugger.getHistoryStateFlow()
    val currentIndexStateFlow = timeTravelDebugger.getCurrentIndexStateFlow()

    val history = historyStateFlow.collectAsState().value
    val currentIndex = currentIndexStateFlow.collectAsState().value
    val listState = rememberLazyListState()

    // Auto-scroll to current index when it changes
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0 && currentIndex < history.size && isExpanded) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    // Save history when it changes (debounced to avoid excessive I/O)
    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            delay(AUTO_SAVE_DEBOUNCE_MILLIS)
            saveHistory?.invoke(history)
        }
    }

    // Draggable toolbar
    Card(
        modifier =
            modifier
                .onPlaced { coordinates ->
                    // Get parent container size
                    val parentLayoutCoordinates = coordinates.parentLayoutCoordinates
                    if (parentLayoutCoordinates != null) {
                        val parentBounds = parentLayoutCoordinates.size
                        parentSize = Size(parentBounds.width.toFloat(), parentBounds.height.toFloat())

                        // Get initial position relative to parent (accounts for alignment)
                        val position = coordinates.positionInParent()
                        if (initialPosition == null) {
                            initialPosition = position
                        }
                    }
                }.offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .clip(shape = RoundedCornerShape(size = 8.dp))
                .onSizeChanged { size -> toolbarSize = Size(width = size.width.toFloat(), height = size.height.toFloat()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Calculate new position with bounds checking
                        val newX = offsetX + dragAmount.x
                        val newY = offsetY + dragAmount.y

                        // Constrain X: keep toolbar within parent bounds horizontally
                        val toolbarWidth = if (toolbarSize.width > 0) toolbarSize.width else 400f
                        val maxX =
                            if (parentSize.width > 0) {
                                parentSize.width - toolbarWidth
                            } else {
                                Float.MAX_VALUE // No constraint if parent size unknown
                            }
                        offsetX = newX.coerceIn(0f, maxX.coerceAtLeast(0f))

                        // Constrain Y: keep toolbar within parent bounds vertically
                        // Since we start at the bottom, we need to account for that
                        val toolbarHeight = if (toolbarSize.height > 0) toolbarSize.height else 100f
                        val maxY =
                            if (parentSize.height > 0) {
                                // Max Y is when toolbar is at the top (offset = 0)
                                // Min Y is when toolbar is at the bottom (offset = parentHeight - toolbarHeight - initialY)
                                val initialY = initialPosition?.y ?: 0f
                                parentSize.height - toolbarHeight - initialY
                            } else {
                                Float.MAX_VALUE // No constraint if parent size unknown
                            }
                        // Y offset can be negative (moving up from bottom) or positive (moving down)
                        // But we need to keep it within bounds
                        offsetY = newY.coerceIn(-(initialPosition?.y ?: 0f), maxY.coerceAtLeast(0f))
                    }
                },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Toolbar row - always visible and draggable
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(all = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Expand/collapse button
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                    )
                }
                // History info
                Text(
                    text = "${currentIndex + 1}/${history.size}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )

                // Current intent (visible when collapsed)
                val currentSnapshot = history.getOrNull(currentIndex)
                val intentLabel = currentSnapshot?.intent?.let { it::class.simpleName ?: "Unknown" } ?: "Initial State"
                Text(
                    text = intentLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    maxLines = 1,
                    modifier =
                        Modifier
                            .weight(1f, fill = false)
                            .padding(horizontal = 8.dp),
                )

                // Navigation controls
                IconButton(
                    onClick = {
                        if (currentIndex > 0) {
                            scope.launch {
                                timeTravelDebugger.restoreStateFromHistory(currentIndex - 1)
                            }
                        }
                    },
                    enabled = currentIndex > 0,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous",
                    )
                }

                IconButton(
                    onClick = {
                        if (currentIndex < history.lastIndex) {
                            scope.launch {
                                timeTravelDebugger.restoreStateFromHistory(currentIndex + 1)
                            }
                        }
                    },
                    enabled = currentIndex < history.lastIndex,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                    )
                }

                // Save button
                if (saveHistory != null) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                saveHistory(history)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                        )
                    }
                }
            }

            // Expandable history list
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(HISTORY_LIST_HEIGHT)
                            .padding(horizontal = 8.dp)
                            .padding(top = 8.dp),
                ) {
                    // History header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "State History",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Current: ${currentIndex + 1} / ${(history.lastIndex + 1).coerceAtLeast(0)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // History list
                    LazyColumn(
                        state = listState,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        itemsIndexed(history) { index, stateHistory ->
                            val previousSnapshot = history.getOrNull(index - 1)
                            HistoryItem(
                                stateHistory = stateHistory,
                                previousSnapshot = previousSnapshot,
                                isSelected = index == currentIndex,
                                onClick = {
                                    scope.launch {
                                        timeTravelDebugger.restoreStateFromHistory(index)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <S, I> HistoryItem(
    stateHistory: StateSnapshot<S, I>,
    previousSnapshot: StateSnapshot<S, I>?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    val timeString = dateFormat.format(Date(stateHistory.timestamp))

    val defaultTextColor =
        if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        }
    val addedColor = MaterialTheme.colorScheme.primary
    val removedColor = MaterialTheme.colorScheme.error

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 4.dp else 2.dp,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "#${stateHistory.index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        },
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stateHistory.intent?.let { it::class.simpleName ?: "Unknown" } ?: "Initial State",
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )

            // State with diff highlighting when we have a previous state to compare
            val (stateAnnotatedString, removedLine) =
                remember(stateHistory.state, previousSnapshot?.state) {
                    buildStateDiffAnnotatedString(
                        currentState = stateHistory.state.toString(),
                        previousState = previousSnapshot?.state?.toString(),
                        defaultColor = defaultTextColor,
                        addedColor = addedColor,
                        maxLength = MAX_STATE_STRING_LENGTH,
                    )
                }

            if (removedLine != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = removedLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = removedColor,
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stateAnnotatedString,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * Builds an AnnotatedString that highlights differences between previous and current state strings.
 * Best-effort only: uses segment-based diff (split by ", ") typical of Kotlin data class [Object.toString].
 * Non–data-class states or custom toString() can make the diff misleading or noisy.
 * Returns the annotated current state and an optional "removed" line for segments only in previous.
 */
private fun buildStateDiffAnnotatedString(
    currentState: String,
    previousState: String?,
    defaultColor: Color,
    addedColor: Color,
    maxLength: Int,
): Pair<AnnotatedString, String?> {
    val currentTruncated =
        if (currentState.length > maxLength) {
            currentState.take(maxLength) + "..."
        } else {
            currentState
        }

    if (previousState == null || previousState == currentState) {
        return Pair(
            AnnotatedString(
                text = currentTruncated,
                spanStyle = SpanStyle(color = defaultColor),
            ),
            null,
        )
    }

    val prevSegments = previousState.split(", ").toSet()
    val currSegments = currentState.split(", ")

    val removedSegments = prevSegments - currSegments.toSet()
    val removedLine =
        if (removedSegments.isEmpty()) {
            null
        } else {
            val removedStr = removedSegments.joinToString(", ").take(maxLength)
            "− ${if (removedStr.length >= maxLength) "$removedStr..." else removedStr}"
        }

    return Pair(
        buildAnnotatedString {
            var lengthSoFar = 0
            for ((i, segment) in currSegments.withIndex()) {
                if (lengthSoFar >= maxLength) {
                    append("...")
                    break
                }
                val segmentWithDelim = if (i < currSegments.lastIndex) "$segment, " else segment
                val isAddedOrChanged = segment !in prevSegments
                if (isAddedOrChanged) {
                    withStyle(SpanStyle(color = addedColor, fontWeight = FontWeight.Medium)) {
                        append(segmentWithDelim.take((maxLength - lengthSoFar).coerceAtLeast(0)))
                    }
                } else {
                    withStyle(SpanStyle(color = defaultColor)) {
                        append(segmentWithDelim.take((maxLength - lengthSoFar).coerceAtLeast(0)))
                    }
                }
                lengthSoFar += segmentWithDelim.length
            }
        },
        removedLine,
    )
}

// Constants for UI configuration
private val HISTORY_LIST_HEIGHT = 300.dp
private const val MAX_STATE_STRING_LENGTH = 190
private const val AUTO_SAVE_DEBOUNCE_MILLIS = 500L
