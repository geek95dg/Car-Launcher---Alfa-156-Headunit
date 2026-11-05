package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.CockpitVisualTheme
import com.example.data.DnaDriveMode

@Composable
fun AlfaLauncherTheme(
    visualTheme: CockpitVisualTheme = CockpitVisualTheme.ALFA_CARBON,
    dnaMode: DnaDriveMode = DnaDriveMode.DYNAMIC,
    isNightMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val dnaAccent = when (dnaMode) {
        DnaDriveMode.DYNAMIC -> DnaDynamicRed
        DnaDriveMode.NATURAL -> DnaNaturalBlue
        DnaDriveMode.ADVANCED -> DnaAdvancedGreen
    }

    val colorScheme = when (visualTheme) {
        CockpitVisualTheme.ALFA_CARBON -> {
            if (isNightMode) {
                darkColorScheme(
                    primary = dnaAccent,
                    onPrimary = Color.White,
                    secondary = AlfaRossoBright,
                    onSecondary = Color.White,
                    tertiary = TelemetryOkGreen,
                    background = AlfaCarbonBg,
                    onBackground = TextPrimaryDark,
                    surface = AlfaCarbonSurface,
                    onSurface = TextPrimaryDark,
                    surfaceVariant = AlfaCarbonElevated,
                    onSurfaceVariant = TextSecondaryDark,
                    outline = SteelBorder
                )
            } else {
                lightColorScheme(
                    primary = AlfaRosso,
                    onPrimary = Color.White,
                    secondary = dnaAccent,
                    onSecondary = Color.White,
                    tertiary = TelemetryOkGreen,
                    background = Color(0xFFECEFF4),
                    onBackground = TextPrimaryLight,
                    surface = Color(0xFFFFFFFF),
                    onSurface = TextPrimaryLight,
                    surfaceVariant = Color(0xFFE0E5EC),
                    onSurfaceVariant = TextSecondaryLight,
                    outline = Color(0xFFB8C2CC)
                )
            }
        }

        CockpitVisualTheme.AAOS_MINIMAL -> {
            if (isNightMode) {
                darkColorScheme(
                    primary = AaosCyanAccent,
                    onPrimary = Color(0xFF002635),
                    secondary = dnaAccent,
                    onSecondary = Color.White,
                    tertiary = TelemetryOkGreen,
                    background = AaosDarkBg,
                    onBackground = TextPrimaryDark,
                    surface = AaosDarkSurface,
                    onSurface = TextPrimaryDark,
                    surfaceVariant = Color(0xFF212C38),
                    onSurfaceVariant = TextSecondaryDark,
                    outline = Color(0xFF334456)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF0277BD),
                    onPrimary = Color.White,
                    secondary = dnaAccent,
                    onSecondary = Color.White,
                    tertiary = Color(0xFF00897B),
                    background = AaosLightBg,
                    onBackground = TextPrimaryLight,
                    surface = AaosLightSurface,
                    onSurface = TextPrimaryLight,
                    surfaceVariant = Color(0xFFE3EAF2),
                    onSurfaceVariant = TextSecondaryLight,
                    outline = Color(0xFFBCC8D6)
                )
            }
        }

        CockpitVisualTheme.CANNOCCHIALE_CLASSIC -> {
            if (isNightMode) {
                darkColorScheme(
                    primary = CannocchialeAmber,
                    onPrimary = Color(0xFF261600),
                    secondary = CannocchialeWarmRed,
                    onSecondary = Color.White,
                    tertiary = dnaAccent,
                    background = CannocchialeBg,
                    onBackground = Color(0xFFFFF8E7),
                    surface = CannocchialeSurface,
                    onSurface = Color(0xFFFFF8E7),
                    surfaceVariant = Color(0xFF27211E),
                    onSurfaceVariant = CannocchialeChrome,
                    outline = Color(0xFF463B35)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFFC66900),
                    onPrimary = Color.White,
                    secondary = AlfaRosso,
                    onSecondary = Color.White,
                    tertiary = dnaAccent,
                    background = Color(0xFFF6F1EA),
                    onBackground = Color(0xFF1F1A17),
                    surface = Color(0xFFFFFBF7),
                    onSurface = Color(0xFF1F1A17),
                    surfaceVariant = Color(0xFFEAE0D5),
                    onSurfaceVariant = Color(0xFF5C5048),
                    outline = Color(0xFFC5B6A8)
                )
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
