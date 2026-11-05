package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DriverProfileEntity
import com.example.data.InstalledAppInfo
import com.example.data.TelemetryState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AlfaTopStatusBar(
    profile: DriverProfileEntity,
    telemetry: TelemetryState,
    isEditMode: Boolean,
    onCycleDna: () -> Unit,
    onToggleDayNight: () -> Unit,
    onOpenProfiles: () -> Unit,
    onOpenWidgetCustomizer: () -> Unit,
    onToggleEditMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeText by remember { mutableStateOf("08:45") }
    LaunchedEffect(Unit) {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTimeText = formatter.format(Date())
            delay(10_000L)
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()

            .combinedClickable(
                onClick = {},
                onDoubleClick = onCycleDna
            )
            .testTag("alfa_top_status_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand + Clock + GPS Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_alfa_scudetto),
                    contentDescription = "Alfa Launcher Logo",
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = currentTimeText,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    onClick = onCycleDna,
                    modifier = Modifier.testTag("top_bar_dna_badge")
                ) {
                    Text(
                        text = "DNA • ${profile.dnaMode.code}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Right Controls: Active Driver Profile, Day/Night, Widget Customizer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = onOpenProfiles,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("open_profiles_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profil kierowcy",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = profile.badgeTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                FilledTonalIconButton(
                    onClick = onToggleDayNight,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("toggle_day_night_button")
                ) {
                    Icon(
                        imageVector = if (profile.isNightMode) {
                            Icons.Filled.DarkMode
                        } else {
                            Icons.Filled.LightMode
                        },
                        contentDescription = "Przełącz tryb dzień/noc"
                    )
                }

                FilledTonalIconButton(
                    onClick = onOpenWidgetCustomizer,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("open_widget_customizer_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DashboardCustomize,
                        contentDescription = "Włącz lub wyłącz moduły ekranu głównego",
                        tint = if (isEditMode) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

@OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun AppGridWidgetCard(
    apps: List<InstalledAppInfo>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onLaunchApp: (InstalledAppInfo) -> Unit,
    onToggleFavorite: (InstalledAppInfo) -> Unit,
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
            .testTag("app_grid_widget_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SIATKA WSZYSTKICH APLIKACJI AAOS & ULUBIONE",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Zainstalowane aplikacje (${apps.size}) • Kliknij gwiazdkę, aby przypiąć do kokpitu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                placeholder = {
                    Text("Szukaj aplikacji po nazwie lub kategorii (np. Nawigacja, Multimedia)...")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Szukaj"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Wyczyść wyszukiwanie"
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_grid_search_input")
            )

            if (apps.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Text(
                        text = "Brak aplikacji pasujących do zapytania \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    apps.take(12).forEach { app ->
                        AppTileItem(
                            app = app,
                            onLaunch = { onLaunchApp(app) },
                            onToggleFavorite = { onToggleFavorite(app) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppTileItem(
    app: InstalledAppInfo,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onLaunch,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        modifier = modifier
            .width(156.dp)
            .border(
                width = if (app.isFavorite) 1.5.dp else 1.dp,
                color = if (app.isFavorite) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("app_tile_${app.packageName}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                ) {
                    Text(
                        text = app.label.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("fav_star_${app.packageName}")
                ) {
                    Icon(
                        imageVector = if (app.isFavorite) {
                            Icons.Filled.Star
                        } else {
                            Icons.Outlined.StarBorder
                        },
                        contentDescription = "Przypnij do ulubionych",
                        tint = if (app.isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.categoryHint,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AaosDockAndHvacBar(
    profile: DriverProfileEntity,
    isEditMode: Boolean,
    onHomeResetScroll: () -> Unit,
    onOpenFullAppDrawer: () -> Unit,
    onCycleLeftSeatHeat: () -> Unit,
    onCycleRightSeatHeat: () -> Unit,
    onToggleEditMode: () -> Unit,
    onOpenWidgetCustomizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 10.dp,
        shadowElevation = 12.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            )

            .testTag("aaos_bottom_dock_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Home & App Drawer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onHomeResetScroll,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("dock_home_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Ekran główny AAOS"
                    )
                }

                FilledTonalIconButton(
                    onClick = onOpenFullAppDrawer,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("dock_app_drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Apps,
                        contentDescription = "Szuflada wszystkich aplikacji"
                    )
                }
            }

            // Center: AAOS 3-Stage Seat Heating (L/R) + Optional Read-Only HVAC Temperature Preview
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (profile.showSeatHeatingControls) {
                    SeatHeatingDockButton(
                        seatLabel = "Fotel L",
                        heatLevel = profile.leftSeatHeatLevel,
                        onClick = onCycleLeftSeatHeat,
                        testTag = "dock_seat_heat_left"
                    )
                }

                if (profile.showHvacTempPreview) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("dock_hvac_readonly_preview")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AcUnit,
                                contentDescription = "Podgląd temperatury HVAC",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "L: ${"%.1f".format(profile.leftTempCelsius)}°C",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "P: ${"%.1f".format(profile.rightTempCelsius)}°C",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else if (!profile.showSeatHeatingControls) {
                    Text(
                        text = "ALFA ROMEO AAOS • ${profile.dnaMode.labelPl}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (profile.showSeatHeatingControls) {
                    SeatHeatingDockButton(
                        seatLabel = "Fotel P",
                        heatLevel = profile.rightSeatHeatLevel,
                        onClick = onCycleRightSeatHeat,
                        testTag = "dock_seat_heat_right"
                    )
                }
            }

            // Right: Launcher Settings & Layout Edit Mode Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onOpenWidgetCustomizer,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("dock_open_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DashboardCustomize,
                        contentDescription = "Ustawienia launchera i widżetów"
                    )
                }

                FilledTonalIconButton(
                    onClick = onToggleEditMode,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("dock_toggle_edit_mode_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Tryb edycji układu",
                        tint = if (isEditMode) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SeatHeatingDockButton(
    seatLabel: String,
    heatLevel: Int,
    onClick: () -> Unit,
    testTag: String
) {
    val activeHeatColor = when (heatLevel) {
        1 -> Color(0xFFFFB300)
        2 -> Color(0xFFFF6D00)
        3 -> Color(0xFFFF1E38)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (heatLevel > 0) {
            activeHeatColor.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier
            .height(48.dp)
            .border(
                width = if (heatLevel > 0) 1.5.dp else 1.dp,
                color = if (heatLevel > 0) {
                    activeHeatColor
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                },
                shape = RoundedCornerShape(14.dp)
            )
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.EventSeat,
                contentDescription = "Podgrzewanie $seatLabel",
                tint = activeHeatColor,
                modifier = Modifier.size(20.dp)
            )
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = seatLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // 3-Stage LED indicator bars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (stage in 1..3) {
                        Box(
                            modifier = Modifier
                                .size(width = 8.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (stage <= heatLevel) {
                                        activeHeatColor
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}
