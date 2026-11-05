package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CockpitVisualTheme
import com.example.data.DnaDriveMode
import com.example.data.TelemetryState
import com.example.ui.theme.DnaAdvancedGreen
import com.example.ui.theme.DnaDynamicRed
import com.example.ui.theme.DnaNaturalBlue
import kotlin.math.cos
import kotlin.math.sin

@OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun CannocchialeGaugesCard(
    telemetry: TelemetryState,
    dnaMode: DnaDriveMode,
    visualTheme: CockpitVisualTheme,
    onSelectDnaMode: (DnaDriveMode) -> Unit,
    onCycleDnaDoubleTap: () -> Unit,
    onToggleSimulationMode: () -> Unit,
    onRequestGpsPermission: () -> Unit,
    onLongPressEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dnaColor = when (dnaMode) {
        DnaDriveMode.DYNAMIC -> DnaDynamicRed
        DnaDriveMode.NATURAL -> DnaNaturalBlue
        DnaDriveMode.ADVANCED -> DnaAdvancedGreen
    }

    val animatedSpeed by animateFloatAsState(
        targetValue = telemetry.speedKmh.toFloat(),
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "speed_anim"
    )

    val animatedRpm by animateFloatAsState(
        targetValue = telemetry.rpm.toFloat(),
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "rpm_anim"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        dnaColor.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        dnaColor.copy(alpha = 0.7f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .combinedClickable(
                onClick = {},
                onDoubleClick = onCycleDnaDoubleTap,
                onLongClick = onLongPressEdit
            )
            .testTag("telemetry_gauges_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Title + DNA Mode Selector + GPS/Demo Switch
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_alfa_scudetto),
                        contentDescription = "Emblemat Scudetto",
                        modifier = Modifier.size(34.dp)
                    )
                    Column {
                        Text(
                            text = when (visualTheme) {
                                CockpitVisualTheme.CANNOCCHIALE_CLASSIC -> "CANNOCCHIALE STORICO TELEMETRIA"
                                CockpitVisualTheme.AAOS_MINIMAL -> "AAOS VEHICLE TELEMETRY CLUSTER"
                                CockpitVisualTheme.ALFA_CARBON -> "ALFA CORSE TELEMETRIA & DNA"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = dnaColor
                        )
                        Text(
                            text = telemetry.currentStreetOrStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = telemetry.isSimulationMode,
                        onClick = {
                            if (telemetry.isSimulationMode && !telemetry.hasGpsPermission) {
                                onRequestGpsPermission()
                            }
                            onToggleSimulationMode()
                        },
                        label = {
                            Text(
                                text = if (telemetry.isSimulationMode) "TRYB DEMO" else "GPS LIVE",
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (telemetry.isSimulationMode) {
                                    Icons.Filled.Speed
                                } else {
                                    Icons.Filled.GpsFixed
                                },
                                contentDescription = "Przełącznik źródła telemetrii",
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = dnaColor.copy(alpha = 0.22f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("toggle_gps_demo_chip")
                    )
                }
            }

            // Twin Cannocchiale Dials + Center DNA & G-Force Cluster
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Tube: Speedometer (0 - 280 km/h)
                CannocchialeDial(
                    value = animatedSpeed,
                    maxValue = 280f,
                    mainReadout = "${telemetry.speedKmh}",
                    unitLabel = "km/h",
                    subReadout = "BIEG ${telemetry.gear}",
                    accentColor = dnaColor,
                    isClassicStyle = visualTheme == CockpitVisualTheme.CANNOCCHIALE_CLASSIC,
                    modifier = Modifier.size(188.dp)
                )

                // Center Column: Alfa DNA Selector + G-Force Meter + Turbo Bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .width(230.dp)
                        .padding(vertical = 4.dp)
                ) {
                    // DNA Mode Selector Pill
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DnaDriveMode.entries.forEach { mode ->
                            val isSelected = mode == dnaMode
                            val modeColor = when (mode) {
                                DnaDriveMode.DYNAMIC -> DnaDynamicRed
                                DnaDriveMode.NATURAL -> DnaNaturalBlue
                                DnaDriveMode.ADVANCED -> DnaAdvancedGreen
                            }
                            Surface(
                                onClick = { onSelectDnaMode(mode) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) modeColor else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(width = 64.dp, height = 48.dp)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color.White else modeColor.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .testTag("dna_mode_button_${mode.code}")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = mode.code,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = when (mode) {
                                            DnaDriveMode.DYNAMIC -> "SPORT"
                                            DnaDriveMode.NATURAL -> "COMFORT"
                                            DnaDriveMode.ADVANCED -> "ECO"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) {
                                            Color.White.copy(alpha = 0.9f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // G-Force Radar + Vehicle Top-Down TPMS
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GForceMeter(
                            lateralG = telemetry.lateralG,
                            longitudinalG = telemetry.longitudinalG,
                            accentColor = dnaColor,
                            modifier = Modifier.size(86.dp)
                        )

                        VehicleTpmsMiniView(
                            tirePressures = telemetry.tirePressuresBar,
                            oilTemp = telemetry.oilTempCelsius
                        )
                    }

                    // Turbo Boost Progress Bar
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TURBO BOOST",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${"%.2f".format(telemetry.turboBar)} bar",
                                style = MaterialTheme.typography.labelLarge,
                                color = dnaColor
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (telemetry.turboBar / 1.8f).coerceIn(0f, 1f) },
                            color = dnaColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }

                // Right Tube: Tachometer (0 - 8000 RPM)
                CannocchialeDial(
                    value = animatedRpm,
                    maxValue = 8000f,
                    mainReadout = "${telemetry.rpm}",
                    unitLabel = "RPM",
                    subReadout = "${telemetry.oilTempCelsius}°C OLEJ",
                    accentColor = dnaColor,
                    isClassicStyle = visualTheme == CockpitVisualTheme.CANNOCCHIALE_CLASSIC,
                    isRpmDial = true,
                    modifier = Modifier.size(188.dp)
                )
            }
        }
    }
}

@Composable
private fun CannocchialeDial(
    value: Float,
    maxValue: Float,
    mainReadout: String,
    unitLabel: String,
    subReadout: String,
    accentColor: Color,
    isClassicStyle: Boolean,
    isRpmDial: Boolean = false,
    modifier: Modifier = Modifier
) {
    val ringColor = MaterialTheme.colorScheme.outline
    val surfaceBg = MaterialTheme.colorScheme.surfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 14.dp.toPx()
            val outerBezelWidth = 4.dp.toPx()
            val diameter = size.minDimension - strokeWidth - outerBezelWidth * 2
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            // Deep Cannocchiale metallic tube shadow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        surfaceBg.copy(alpha = 0.25f),
                        Color.Black.copy(alpha = 0.72f)
                    )
                ),
                radius = size.minDimension / 2f
            )

            // Outer bezel ring
            drawCircle(
                color = if (isClassicStyle) Color(0xFFB0A090) else ringColor,
                radius = (size.minDimension - outerBezelWidth) / 2f,
                style = Stroke(width = outerBezelWidth)
            )

            // Background gauge track (135 deg to +270 deg sweep)
            drawArc(
                color = ringColor.copy(alpha = 0.35f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Redline zone on RPM dial
            if (isRpmDial) {
                drawArc(
                    color = DnaDynamicRed.copy(alpha = 0.45f),
                    startAngle = 135f + 270f * 0.78f,
                    sweepAngle = 270f * 0.22f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
            }

            // Active progress arc
            val fraction = (value / maxValue).coerceIn(0f, 1f)
            val activeSweep = 270f * fraction
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.55f),
                        accentColor,
                        if (isRpmDial && fraction > 0.8f) DnaDynamicRed else accentColor
                    )
                ),
                startAngle = 135f,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Tick marks around the dial
            val tickCount = 24
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerTickRadius = diameter / 2f - strokeWidth * 0.8f
            val innerTickRadius = outerTickRadius - 8.dp.toPx()

            for (i in 0..tickCount) {
                val angleDeg = 135f + (270f / tickCount) * i
                val rad = Math.toRadians(angleDeg.toDouble())
                val start = Offset(
                    x = center.x + (innerTickRadius * cos(rad)).toFloat(),
                    y = center.y + (innerTickRadius * sin(rad)).toFloat()
                )
                val end = Offset(
                    x = center.x + (outerTickRadius * cos(rad)).toFloat(),
                    y = center.y + (outerTickRadius * sin(rad)).toFloat()
                )
                drawLine(
                    color = if (i.toFloat() / tickCount <= fraction) {
                        accentColor
                    } else {
                        ringColor.copy(alpha = 0.6f)
                    },
                    start = start,
                    end = end,
                    strokeWidth = if (i % 4 == 0) 2.5.dp.toPx() else 1.2.dp.toPx()
                )
            }

            // Needle pointer
            val needleAngleRad = Math.toRadians((135f + activeSweep).toDouble())
            val needleLength = diameter / 2f - 10.dp.toPx()
            val needleTip = Offset(
                x = center.x + (needleLength * cos(needleAngleRad)).toFloat(),
                y = center.y + (needleLength * sin(needleAngleRad)).toFloat()
            )
            drawLine(
                color = if (isClassicStyle) Color(0xFFFF3D00) else accentColor,
                start = center,
                end = needleTip,
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = accentColor,
                radius = 6.dp.toPx(),
                center = center
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 14.dp)
        ) {
            Text(
                text = mainReadout,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unitLabel,
                style = MaterialTheme.typography.labelLarge,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = subReadout,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GForceMeter(
    lateralG: Float,
    longitudinalG: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outline
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 4.dp.toPx()

            drawCircle(
                color = Color.Black.copy(alpha = 0.45f),
                radius = radius
            )
            drawCircle(
                color = gridColor.copy(alpha = 0.6f),
                radius = radius,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = gridColor.copy(alpha = 0.35f),
                radius = radius * 0.5f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawLine(
                color = gridColor.copy(alpha = 0.35f),
                start = Offset(center.x - radius, center.y),
                end = Offset(center.x + radius, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = gridColor.copy(alpha = 0.35f),
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x, center.y + radius),
                strokeWidth = 1.dp.toPx()
            )

            val dotX = center.x + (lateralG.coerceIn(-1.2f, 1.2f) / 1.2f) * (radius * 0.78f)
            val dotY = center.y + (longitudinalG.coerceIn(-1.2f, 1.2f) / 1.2f) * (radius * 0.78f)
            drawCircle(
                color = accentColor,
                radius = 5.dp.toPx(),
                center = Offset(dotX, dotY)
            )
        }

        Text(
            text = "${"%.1f".format(kotlin.math.hypot(lateralG, longitudinalG))}G",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
        )
    }
}

@Composable
private fun VehicleTpmsMiniView(
    tirePressures: List<Float>,
    oilTemp: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${tirePressures.getOrElse(0) { 2.4f }}",
                style = MaterialTheme.typography.labelMedium,
                color = DnaAdvancedGreen
            )
            Text(
                text = "${tirePressures.getOrElse(2) { 2.3f }}",
                style = MaterialTheme.typography.labelMedium,
                color = DnaAdvancedGreen
            )
        }

        Image(
            painter = painterResource(id = R.drawable.ic_car_top_telemetry),
            contentDescription = "Podgląd ciśnienia opon TPMS",
            modifier = Modifier.size(width = 40.dp, height = 68.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "${tirePressures.getOrElse(1) { 2.4f }}",
                style = MaterialTheme.typography.labelMedium,
                color = DnaAdvancedGreen
            )
            Text(
                text = "${tirePressures.getOrElse(3) { 2.3f }}",
                style = MaterialTheme.typography.labelMedium,
                color = DnaAdvancedGreen
            )
        }
    }
}
