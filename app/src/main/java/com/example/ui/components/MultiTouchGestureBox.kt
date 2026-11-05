package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.GestureHudEvent
import kotlin.math.abs

/**
 * Global multi-touch gesture container for AAOS Cockpit:
 * - 2-finger vertical swipe -> adjusts system audio volume up/down
 * - 2-finger horizontal swipe -> skips track Next / Previous
 * - 3-finger swipe or tap -> cycles Alfa DNA drive mode (or toggles Day/Night on vertical 3-finger swipe)
 * Single-finger events pass seamlessly to underlying scrollable lists, buttons, and drag handles.
 */
@Composable
fun MultiTouchGestureBox(
    hudEvent: GestureHudEvent?,
    onTwoFingerVerticalVolume: (deltaPercent: Int) -> Unit,
    onTwoFingerHorizontalTrack: (nextTrack: Boolean) -> Unit,
    onThreeFingerDnaTrigger: () -> Unit,
    onThreeFingerDayNightTrigger: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("cockpit_multitouch_container")
            .pointerInput(Unit) {
                awaitEachGesture {
                    var maxPointersSeen = 0
                    var accumulatedCentroidDelta = Offset.Zero
                    var gestureTriggered = false
                    var lastCentroid: Offset? = null

                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val activeChanges = event.changes.filter { it.pressed }
                        if (activeChanges.isEmpty()) break

                        val count = activeChanges.size
                        if (count > maxPointersSeen) {
                            maxPointersSeen = count
                        }

                        if (count >= 2 && !gestureTriggered) {
                            val avgX = activeChanges.map { it.position.x }.average().toFloat()
                            val avgY = activeChanges.map { it.position.y }.average().toFloat()
                            val currentCentroid = Offset(avgX, avgY)

                            val prev = lastCentroid
                            if (prev != null) {
                                accumulatedCentroidDelta += (currentCentroid - prev)
                            }
                            lastCentroid = currentCentroid

                            // Consume multi-touch moves so inner scrollables don't jitter
                            activeChanges.forEach { it.consume() }

                            val thresholdPx = 72f
                            if (count == 2) {
                                if (abs(accumulatedCentroidDelta.x) > thresholdPx &&
                                    abs(accumulatedCentroidDelta.x) > abs(accumulatedCentroidDelta.y) * 1.2f
                                ) {
                                    gestureTriggered = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val isNext = accumulatedCentroidDelta.x < 0f
                                    onTwoFingerHorizontalTrack(isNext)
                                } else if (abs(accumulatedCentroidDelta.y) > thresholdPx &&
                                    abs(accumulatedCentroidDelta.y) > abs(accumulatedCentroidDelta.x) * 1.2f
                                ) {
                                    gestureTriggered = true
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    // Swipe UP (negative Y) increases volume
                                    val deltaPct = if (accumulatedCentroidDelta.y < 0f) 10 else -10
                                    onTwoFingerVerticalVolume(deltaPct)
                                }
                            } else if (count >= 3) {
                                if (abs(accumulatedCentroidDelta.y) > thresholdPx) {
                                    gestureTriggered = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onThreeFingerDayNightTrigger()
                                } else if (abs(accumulatedCentroidDelta.x) > thresholdPx) {
                                    gestureTriggered = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onThreeFingerDnaTrigger()
                                }
                            }
                        } else {
                            lastCentroid = null
                        }
                    }

                    // If 3 fingers tapped quickly without large drag, cycle DNA mode
                    if (maxPointersSeen >= 3 && !gestureTriggered) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onThreeFingerDnaTrigger()
                    }
                }
            }
    ) {
        content()

        // Floating HUD Overlay for Gesture & Cockpit Feedback
        AnimatedVisibility(
            visible = hudEvent != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 68.dp)
        ) {
            hudEvent?.let { event ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 12.dp,
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .testTag("gesture_hud_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TouchApp,
                                    contentDescription = "Gest",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = event.gestureBadge,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Column {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = event.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
