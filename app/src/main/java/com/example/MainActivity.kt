package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CockpitWidgetType
import com.example.data.WidgetConfigEntity
import com.example.ui.CockpitModalSheet
import com.example.ui.LauncherViewModel
import com.example.ui.components.AaosDockAndHvacBar
import com.example.ui.components.AlfaTopStatusBar
import com.example.ui.components.AppGridWidgetCard
import com.example.ui.components.CannocchialeGaugesCard
import com.example.ui.components.DriverProfilesSheet
import com.example.ui.components.FullAppDrawerSheet
import com.example.ui.components.MultiTouchGestureBox
import com.example.ui.components.SplitOverviewCard
import com.example.ui.components.WidgetCustomizerSheet
import com.example.ui.theme.AlfaLauncherTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val launcherViewModel: LauncherViewModel = viewModel(
                factory = LauncherViewModel.provideFactory(context)
            )
            AlfaLauncherApp(viewModel = launcherViewModel)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlfaLauncherApp(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val widgetConfigs by viewModel.widgetConfigs.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.appSearchQuery.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val mediaState by viewModel.mediaState.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val hudEvent by viewModel.gestureHudEvent.collectAsStateWithLifecycle()
    val activeModalSheet by viewModel.activeModalSheet.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onGpsPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            viewModel.onGpsPermissionResult(true)
        }
    }

    // Back button closes any active sheet or exits edit mode cleanly (AAOS Car Launcher standard)
    BackHandler(enabled = activeModalSheet != null || isEditMode) {
        if (activeModalSheet != null) {
            viewModel.openModalSheet(null)
        } else if (isEditMode) {
            viewModel.toggleEditMode()
        }
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val orderedWidgets = widgetConfigs.sortedBy { it.sortOrder }
    val dockConfig = orderedWidgets.find { it.widgetType == CockpitWidgetType.DOCK_BAR }
    val isDockVisible = dockConfig?.isEnabled ?: true
    val mainWidgets = orderedWidgets.filter {
        it.widgetType != CockpitWidgetType.DOCK_BAR && it.isEnabled
    }

    AlfaLauncherTheme(
        visualTheme = activeProfile.visualTheme,
        dnaMode = activeProfile.dnaMode,
        isNightMode = activeProfile.isNightMode
    ) {
        MultiTouchGestureBox(
            hudEvent = hudEvent,
            onTwoFingerVerticalVolume = { deltaPct ->
                viewModel.adjustVolumeByGesture(deltaPct)
            },
            onTwoFingerHorizontalTrack = { isNext ->
                viewModel.skipTrack(next = isNext, fromGesture = true)
            },
            onThreeFingerDnaTrigger = {
                viewModel.cycleDnaMode(fromGesture = true)
            },
            onThreeFingerDayNightTrigger = {
                viewModel.toggleDayNightMode(fromGesture = true)
            }
        ) {
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    AlfaTopStatusBar(
                        profile = activeProfile,
                        telemetry = telemetry,
                        isEditMode = isEditMode,
                        onCycleDna = { viewModel.cycleDnaMode(fromGesture = true) },
                        onToggleDayNight = { viewModel.toggleDayNightMode(fromGesture = false) },
                        onOpenProfiles = {
                            viewModel.openModalSheet(CockpitModalSheet.DRIVER_PROFILES)
                        },
                        onOpenWidgetCustomizer = {
                            viewModel.openModalSheet(CockpitModalSheet.WIDGET_CUSTOMIZER)
                        },
                        onToggleEditMode = { viewModel.toggleEditMode() }
                    )
                },
                bottomBar = {
                    if (isDockVisible) {
                        AaosDockAndHvacBar(
                            profile = activeProfile,
                            isEditMode = isEditMode,
                            onHomeResetScroll = {
                                coroutineScope.launch { scrollState.animateScrollTo(0) }
                            },
                            onOpenFullAppDrawer = {
                                viewModel.openModalSheet(CockpitModalSheet.FULL_APP_DRAWER)
                            },
                            onCycleLeftSeatHeat = { viewModel.cycleLeftSeatHeat() },
                            onCycleRightSeatHeat = { viewModel.cycleRightSeatHeat() },
                            onToggleEditMode = { viewModel.toggleEditMode() },
                            onOpenWidgetCustomizer = {
                                viewModel.openModalSheet(CockpitModalSheet.WIDGET_CUSTOMIZER)
                            }
                        )
                    }
                }
            ) { innerPadding ->
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                        .padding(innerPadding)
                ) {
                    val isWideScreen = maxWidth >= 700.dp

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Interactive Gesture & Quick Action Strip
                        GestureInteractiveStrip(
                            onSimulateVolUp = { viewModel.adjustVolumeByGesture(10) },
                            onSimulateNextTrack = { viewModel.skipTrack(next = true, fromGesture = true) },
                            onSimulateDna = { viewModel.cycleDnaMode(fromGesture = true) },
                            onOpenModulesSheet = {
                                viewModel.openModalSheet(CockpitModalSheet.WIDGET_CUSTOMIZER)
                            }
                        )

                        // Edit Mode Banner (Drag & Drop + Toggle Visibility)
                        AnimatedVisibility(visible = isEditMode) {
                            EditLayoutBanner(
                                allConfigs = orderedWidgets,
                                onToggleWidgetEnabled = { cfg, enabled ->
                                    viewModel.setWidgetEnabled(cfg, enabled)
                                },
                                onResetDefault = { viewModel.resetWidgetsLayout() },
                                onDoneEdit = { viewModel.toggleEditMode() }
                            )
                        }

                        if (mainWidgets.isEmpty()) {
                            EmptyCockpitStateCard(
                                onEnableAll = { viewModel.resetWidgetsLayout() },
                                onOpenCustomizer = {
                                    viewModel.openModalSheet(CockpitModalSheet.WIDGET_CUSTOMIZER)
                                }
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                mainWidgets.forEachIndexed { idx, config ->
                                    val itemModifier = if (isWideScreen && !config.isWideSpan) {
                                        Modifier
                                            .weight(1f)
                                            .widthIn(min = 320.dp)
                                    } else {
                                        Modifier.fillMaxWidth()
                                    }

                                    EditableCockpitWidgetContainer(
                                        config = config,
                                        isEditMode = isEditMode,
                                        canMoveUp = idx > 0,
                                        canMoveDown = idx < mainWidgets.size - 1,
                                        onMoveUp = { viewModel.moveWidgetOrder(config, true) },
                                        onMoveDown = { viewModel.moveWidgetOrder(config, false) },
                                        onToggleSpan = { viewModel.toggleWidgetSpan(config) },
                                        onDisableWidget = { viewModel.setWidgetEnabled(config, false) },
                                        modifier = itemModifier
                                    ) {
                                        when (config.widgetType) {
                                            CockpitWidgetType.TELEMETRY_GAUGES -> {
                                                CannocchialeGaugesCard(
                                                    telemetry = telemetry,
                                                    dnaMode = activeProfile.dnaMode,
                                                    visualTheme = activeProfile.visualTheme,
                                                    onSelectDnaMode = { viewModel.selectDnaMode(it) },
                                                    onCycleDnaDoubleTap = {
                                                        viewModel.cycleDnaMode(fromGesture = true)
                                                    },
                                                    onToggleSimulationMode = {
                                                        viewModel.toggleSimulationMode()
                                                    },
                                                    onRequestGpsPermission = {
                                                        permissionLauncher.launch(
                                                            arrayOf(
                                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                                            )
                                                        )
                                                    },
                                                    onLongPressEdit = {
                                                        viewModel.enableEditModeFromLongPress()
                                                    }
                                                )
                                            }

                                            CockpitWidgetType.SPLIT_OVERVIEW -> {
                                                SplitOverviewCard(
                                                    telemetry = telemetry,
                                                    mediaState = mediaState,
                                                    favoriteApps = installedApps.filter { it.isFavorite },
                                                    onTogglePlayPause = { viewModel.togglePlayPause() },
                                                    onSkipNext = {
                                                        viewModel.skipTrack(next = true, fromGesture = false)
                                                    },
                                                    onSkipPrev = {
                                                        viewModel.skipTrack(next = false, fromGesture = false)
                                                    },
                                                    onVolumeChange = { viewModel.setVolumeDirect(it) },
                                                    onLaunchApp = { viewModel.launchInstalledApp(it) },
                                                    onLaunchSystemShortcut = {
                                                        viewModel.launchSystemShortcut(it)
                                                    },
                                                    onOpenFullAppDrawer = {
                                                        viewModel.openModalSheet(
                                                            CockpitModalSheet.FULL_APP_DRAWER
                                                        )
                                                    },
                                                    onLongPressEdit = {
                                                        viewModel.enableEditModeFromLongPress()
                                                    }
                                                )
                                            }

                                            CockpitWidgetType.APP_GRID_WIDGET -> {
                                                AppGridWidgetCard(
                                                    apps = installedApps,
                                                    searchQuery = searchQuery,
                                                    onSearchQueryChange = {
                                                        viewModel.updateSearchQuery(it)
                                                    },
                                                    onLaunchApp = { viewModel.launchInstalledApp(it) },
                                                    onToggleFavorite = {
                                                        viewModel.toggleFavoriteApp(it)
                                                    },
                                                    onLongPressEdit = {
                                                        viewModel.enableEditModeFromLongPress()
                                                    }
                                                )
                                            }

                                            CockpitWidgetType.DOCK_BAR -> Unit
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Modal Bottom Sheets
        when (activeModalSheet) {
            CockpitModalSheet.WIDGET_CUSTOMIZER -> {
                WidgetCustomizerSheet(
                    widgetConfigs = orderedWidgets,
                    showSeatHeatingControls = activeProfile.showSeatHeatingControls,
                    onToggleSeatHeatingControls = { enabled ->
                        viewModel.setShowSeatHeatingControls(enabled)
                    },
                    showHvacTempPreview = activeProfile.showHvacTempPreview,
                    onToggleHvacTempPreview = { enabled ->
                        viewModel.setShowHvacTempPreview(enabled)
                    },
                    onToggleWidgetEnabled = { cfg, enabled ->
                        viewModel.setWidgetEnabled(cfg, enabled)
                    },
                    onMoveWidget = { cfg, up ->
                        viewModel.moveWidgetOrder(cfg, up)
                    },
                    onToggleWidgetSpan = { cfg ->
                        viewModel.toggleWidgetSpan(cfg)
                    },
                    onResetWidgets = { viewModel.resetWidgetsLayout() },
                    onDismiss = { viewModel.openModalSheet(null) }
                )
            }

            CockpitModalSheet.DRIVER_PROFILES -> {
                DriverProfilesSheet(
                    profiles = allProfiles,
                    activeProfile = activeProfile,
                    onSelectProfile = { viewModel.switchDriverProfile(it) },
                    onSelectTheme = { viewModel.selectVisualTheme(it) },
                    onToggleHvacTempPreview = { enabled ->
                        viewModel.setShowHvacTempPreview(enabled)
                    },
                    onCreateProfile = { name, theme, dna ->
                        viewModel.createNewDriverProfile(
                            name = name,
                            theme = theme,
                            dna = dna,
                            isNight = activeProfile.isNightMode
                        )
                    },
                    onDismiss = { viewModel.openModalSheet(null) }
                )
            }

            CockpitModalSheet.FULL_APP_DRAWER -> {
                FullAppDrawerSheet(
                    apps = installedApps,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onLaunchApp = { viewModel.launchInstalledApp(it) },
                    onToggleFavorite = { viewModel.toggleFavoriteApp(it) },
                    onDismiss = { viewModel.openModalSheet(null) }
                )
            }

            null -> Unit
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GestureInteractiveStrip(
    onSimulateVolUp: () -> Unit,
    onSimulateNextTrack: () -> Unit,
    onSimulateDna: () -> Unit,
    onOpenModulesSheet: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gesture_helper_strip")
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.TouchApp,
                    contentDescription = "Obsługa gestów",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(id = R.string.gesture_hint_bar),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = false,
                    onClick = onSimulateVolUp,
                    label = { Text("2 Palce: Głośność+") },
                    modifier = Modifier.testTag("sim_gesture_vol_btn")
                )
                FilterChip(
                    selected = false,
                    onClick = onSimulateNextTrack,
                    label = { Text("2 Palce: Utwór ⏭") },
                    modifier = Modifier.testTag("sim_gesture_track_btn")
                )
                FilterChip(
                    selected = false,
                    onClick = onSimulateDna,
                    label = { Text("3 Palce: DNA") },
                    modifier = Modifier.testTag("sim_gesture_dna_btn")
                )
                FilterChip(
                    selected = true,
                    onClick = onOpenModulesSheet,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.DashboardCustomize,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Moduły (Wł/Wył)") },
                    modifier = Modifier.testTag("quick_open_modules_btn")
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditLayoutBanner(
    allConfigs: List<WidgetConfigEntity>,
    onToggleWidgetEnabled: (WidgetConfigEntity, Boolean) -> Unit,
    onResetDefault: () -> Unit,
    onDoneEdit: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("edit_mode_banner")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.edit_layout_active),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDoneEdit,
                        modifier = Modifier.testTag("finish_edit_mode_btn")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Zapisz Układ")
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allConfigs.forEach { cfg ->
                    FilterChip(
                        selected = cfg.isEnabled,
                        onClick = { onToggleWidgetEnabled(cfg, !cfg.isEnabled) },
                        label = { Text(cfg.widgetType.titlePl.substringBefore(":")) },
                        modifier = Modifier.testTag("banner_toggle_${cfg.widgetType.name}")
                    )
                }
            }
        }
    }
}

@Composable
private fun EditableCockpitWidgetContainer(
    config: WidgetConfigEntity,
    isEditMode: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleSpan: () -> Unit,
    onDisableWidget: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var dragAccumulatorY by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .then(
                if (isEditMode) {
                    Modifier
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(26.dp)
                        )
                        .padding(6.dp)
                        .pointerInput(config.widgetTypeName) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { dragAccumulatorY = 0f },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragAccumulatorY += dragAmount.y
                                    if (abs(dragAccumulatorY) > 90f) {
                                        if (dragAccumulatorY < 0f && canMoveUp) {
                                            onMoveUp()
                                        } else if (dragAccumulatorY > 0f && canMoveDown) {
                                            onMoveDown()
                                        }
                                        dragAccumulatorY = 0f
                                    }
                                }
                            )
                        }
                } else {
                    Modifier
                }
            )
    ) {
        AnimatedVisibility(visible = isEditMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DragIndicator,
                        contentDescription = "Przeciągnij kafelek",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = config.widgetType.titlePl.substringBefore(":"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = config.isWideSpan,
                        onClick = onToggleSpan,
                        label = { Text(if (config.isWideSpan) "2x Szeroki" else "1x Kafelek") }
                    )
                    IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                        Icon(Icons.Filled.ArrowUpward, contentDescription = "Przesuń w górę")
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                        Icon(Icons.Filled.ArrowDownward, contentDescription = "Przesuń w dół")
                    }
                    IconButton(onClick = onDisableWidget) {
                        Icon(
                            imageVector = Icons.Filled.VisibilityOff,
                            contentDescription = "Ukryj widżet",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        content()
    }
}

@Composable
private fun EmptyCockpitStateCard(
    onEnableAll: () -> Unit,
    onOpenCustomizer: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(28.dp)
        ) {
            Text(
                text = "Wszystkie widżety ekranu głównego są wyłączone",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Włącz wybrane moduły w konfiguratorze lub przywróć domyślny układ kokpitu Alfa AAOS.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onEnableAll) {
                    Text("Włącz Wszystkie Moduły")
                }
                Button(onClick = onOpenCustomizer) {
                    Text("Konfigurator Widżetów")
                }
            }
        }
    }
}
