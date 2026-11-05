package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.InstalledAppInfo
import com.example.data.MediaPlaybackState
import com.example.data.TelemetryState
import com.example.ui.SystemQuickShortcut

@OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun SplitOverviewCard(
    telemetry: TelemetryState,
    mediaState: MediaPlaybackState,
    favoriteApps: List<InstalledAppInfo>,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrev: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onLaunchApp: (InstalledAppInfo) -> Unit,
    onLaunchSystemShortcut: (SystemQuickShortcut) -> Unit,
    onOpenFullAppDrawer: () -> Unit,
    onLongPressEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                shape = RoundedCornerShape(24.dp)
            )
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPressEdit
            )
            .testTag("split_overview_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PODZIELONY EKRAN AAOS • NAWIGACJA, MEDIA I SKRÓTY",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "2 Palce: Głośność / Utwór",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Pane 1: GPS Navigation & Live Compass Map Card
                GpsNavigationSubCard(
                    telemetry = telemetry,
                    onOpenMaps = { onLaunchSystemShortcut(SystemQuickShortcut.MAPS) },
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(min = 250.dp)
                )

                // Pane 2: Media Controller & Audio Equalizer Card
                MediaPlayerSubCard(
                    mediaState = mediaState,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipPrev = onSkipPrev,
                    onSkipNext = onSkipNext,
                    onVolumeChange = onVolumeChange,
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(min = 260.dp)
                )
            }

            // Pane 3: Quick Shortcuts & Pinned Favorite Apps Row
            ShortcutsStripSubCard(
                favoriteApps = favoriteApps,
                onLaunchApp = onLaunchApp,
                onLaunchSystemShortcut = onLaunchSystemShortcut,
                onOpenFullAppDrawer = onOpenFullAppDrawer
            )
        }
    }
}

@Composable
private fun GpsNavigationSubCard(
    telemetry: TelemetryState,
    onOpenMaps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary
    val gridStroke = MaterialTheme.colorScheme.outline

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Navigation,
                        contentDescription = "Nawigacja GPS",
                        tint = accent
                    )
                    Column {
                        Text(
                            text = telemetry.nextManeuverDistance,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = telemetry.nextManeuverInstruction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                FilledTonalIconButton(
                    onClick = onOpenMaps,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("open_maps_shortcut_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Explore,
                        contentDescription = "Otwórz Mapy"
                    )
                }
            }

            // Interactive Vector Radar Map & Compass Bearing Canvas
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A0F16))
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    // Perspective road grid lines
                    for (i in 1..5) {
                        val y = h * (i / 6f)
                        drawLine(
                            color = gridStroke.copy(alpha = 0.28f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    // Route spline
                    val routePath = Path().apply {
                        moveTo(w * 0.25f, h)
                        cubicTo(
                            w * 0.35f, h * 0.6f,
                            w * 0.55f, h * 0.45f,
                            w * 0.68f, h * 0.12f
                        )
                    }
                    drawPath(
                        path = routePath,
                        color = accent,
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Compass rose on right side
                    val compassCenter = Offset(w - 44.dp.toPx(), h / 2f)
                    val compassRadius = 28.dp.toPx()
                    drawCircle(
                        color = gridStroke.copy(alpha = 0.6f),
                        radius = compassRadius,
                        center = compassCenter,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    rotate(degrees = telemetry.compassBearingDeg, pivot = compassCenter) {
                        val arrowPath = Path().apply {
                            moveTo(compassCenter.x, compassCenter.y - compassRadius * 0.75f)
                            lineTo(compassCenter.x + 6.dp.toPx(), compassCenter.y + 8.dp.toPx())
                            lineTo(compassCenter.x, compassCenter.y + 3.dp.toPx())
                            lineTo(compassCenter.x - 6.dp.toPx(), compassCenter.y + 8.dp.toPx())
                            close()
                        }
                        drawPath(arrowPath, color = accent)
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.72f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "KURS: ${telemetry.compassBearingDeg.toInt()}° ${telemetry.compassCardinal}",
                        style = MaterialTheme.typography.labelLarge,
                        color = accent
                    )
                    Text(
                        text = "WYS: ${telemetry.altitudeMeters} m n.p.m.",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaPlayerSubCard(
    mediaState: MediaPlaybackState,
    onTogglePlayPause: () -> Unit,
    onSkipPrev: () -> Unit,
    onSkipNext: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accent.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GraphicEq,
                            contentDescription = "Odtwarzacz",
                            tint = accent
                        )
                    }
                    Column {
                        Text(
                            text = mediaState.trackTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${mediaState.artistName} • ${mediaState.albumName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Progress bar & elapsed time
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { mediaState.progressFraction },
                    color = accent,
                    trackColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = mediaState.elapsedFormatted,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = mediaState.durationFormatted,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Transport buttons + Volume slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalIconButton(
                        onClick = onSkipPrev,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("media_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Poprzedni utwór"
                        )
                    }
                    FilledIconButton(
                        onClick = onTogglePlayPause,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = accent,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("media_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (mediaState.isPlaying) {
                                Icons.Filled.Pause
                            } else {
                                Icons.Filled.PlayArrow
                            },
                            contentDescription = if (mediaState.isPlaying) "Pauza" else "Odtwórz"
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onSkipNext,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("media_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Następny utwór"
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f).padding(start = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Głośność",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Slider(
                        value = mediaState.volumePercent.toFloat(),
                        onValueChange = { onVolumeChange(it.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("media_volume_slider")
                    )
                    Text(
                        text = "${mediaState.volumePercent}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShortcutsStripSubCard(
    favoriteApps: List<InstalledAppInfo>,
    onLaunchApp: (InstalledAppInfo) -> Unit,
    onLaunchSystemShortcut: (SystemQuickShortcut) -> Unit,
    onOpenFullAppDrawer: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "SZYBKIE SKRÓTY KOKPITU I PRZYPIĘTE APLIKACJE",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickTileButton(
                label = "Nawigacja",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Navigation,
                        contentDescription = "Nawigacja",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = { onLaunchSystemShortcut(SystemQuickShortcut.MAPS) },
                testTag = "quick_tile_maps"
            )
            QuickTileButton(
                label = "Telefon",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = "Telefon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = { onLaunchSystemShortcut(SystemQuickShortcut.PHONE) },
                testTag = "quick_tile_phone"
            )
            QuickTileButton(
                label = "Ustawienia Auta",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Ustawienia",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = { onLaunchSystemShortcut(SystemQuickShortcut.SETTINGS) },
                testTag = "quick_tile_settings"
            )

            favoriteApps.take(4).forEach { favApp ->
                QuickTileButton(
                    label = favApp.label,
                    icon = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = favApp.label.take(1).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    onClick = { onLaunchApp(favApp) },
                    testTag = "quick_tile_fav_${favApp.packageName}"
                )
            }

            QuickTileButton(
                label = "Szuflada Appek",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Apps,
                        contentDescription = "Wszystkie aplikacje",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                onClick = onOpenFullAppDrawer,
                testTag = "quick_tile_all_apps"
            )
        }
    }
}

@Composable
private fun QuickTileButton(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .height(52.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
