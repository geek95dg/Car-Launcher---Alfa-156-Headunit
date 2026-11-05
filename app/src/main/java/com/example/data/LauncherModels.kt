package com.example.data

import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CockpitVisualTheme(val titlePl: String, val subtitlePl: String) {
    ALFA_CARBON(
        titlePl = "Alfa Rosso Sport Carbon",
        subtitlePl = "Ciemny karbon, krwistoczerwone akcenty i dynamiczny blask DNA"
    ),
    AAOS_MINIMAL(
        titlePl = "Minimalistyczny AAOS M3",
        subtitlePl = "Nowoczesny styl Android Automotive OS z adaptacyjnym kontrastem"
    ),
    CANNOCCHIALE_CLASSIC(
        titlePl = "Klasyczny Cannocchiale",
        subtitlePl = "Tradycyjne tubusowe tarcze Alfa z bursztynowo-chromowanym podświetleniem"
    )
}

enum class DnaDriveMode(
    val code: String,
    val labelPl: String,
    val descriptionPl: String,
    val throttleMultiplier: Float
) {
    DYNAMIC(
        code = "D",
        labelPl = "DYNAMIC SPORT",
        descriptionPl = "Maksymalna reakcja przepustnicy, twarde zawieszenie, wskaźnik Turbo i G-Force",
        throttleMultiplier = 1.35f
    ),
    NATURAL(
        code = "N",
        labelPl = "NATURAL COMFORT",
        descriptionPl = "Zbalansowana charakterystyka silnika i komfortowe wspomaganie do codziennej jazdy",
        throttleMultiplier = 1.0f
    ),
    ADVANCED(
        code = "A",
        labelPl = "ADVANCED EFFICIENCY",
        descriptionPl = "Zoptymalizowane zużycie energii, żeglowanie skrzyni biegów i przyczepność na mokrym",
        throttleMultiplier = 0.75f
    );

    fun next(): DnaDriveMode = when (this) {
        DYNAMIC -> NATURAL
        NATURAL -> ADVANCED
        ADVANCED -> DYNAMIC
    }
}

enum class CockpitWidgetType(
    val titlePl: String,
    val descriptionPl: String,
    val defaultWide: Boolean
) {
    SPLIT_OVERVIEW(
        titlePl = "Podzielony Ekran: Nawigacja GPS, Media i Skróty",
        descriptionPl = "Zintegrowany moduł kompasu/trasy GPS, odtwarzacza multimediów oraz szybkich skrótów",
        defaultWide = true
    ),
    TELEMETRY_GAUGES(
        titlePl = "Zegary Telemetrii Cannocchiale i Status Pojazdu",
        descriptionPl = "Podwójne zegary prędkości i RPM, przełącznik DNA, ciśnienie Turbo, G-Force i TPMS opon",
        defaultWide = true
    ),
    APP_GRID_WIDGET(
        titlePl = "Siatka Wszystkich Aplikacji z Wyszukiwarką",
        descriptionPl = "Wbudowany widżet zainstalowanych aplikacji Android / AAOS z filtrowaniem i ulubionymi",
        defaultWide = true
    ),
    DOCK_BAR(
        titlePl = "Pasek Dokowania ze Skrótami Systemowymi",
        descriptionPl = "Stały dolny dok systemowy AAOS ze skrótami do ekranu głównego, aplikacji i ustawień",
        defaultWide = true
    )
}

@Entity(tableName = "driver_profiles")
data class DriverProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val badgeTitle: String,
    val visualThemeName: String = CockpitVisualTheme.ALFA_CARBON.name,
    val dnaModeName: String = DnaDriveMode.DYNAMIC.name,
    val isNightMode: Boolean = true,
    val isActive: Boolean = false,
    val showSeatHeatingControls: Boolean = true,
    val leftSeatHeatLevel: Int = 1,
    val rightSeatHeatLevel: Int = 0,
    val showHvacTempPreview: Boolean = false,
    val leftTempCelsius: Float = 21.0f,
    val rightTempCelsius: Float = 21.5f
) {
    val visualTheme: CockpitVisualTheme
        get() = CockpitVisualTheme.entries.find { it.name == visualThemeName }
            ?: CockpitVisualTheme.ALFA_CARBON

    val dnaMode: DnaDriveMode
        get() = DnaDriveMode.entries.find { it.name == dnaModeName }
            ?: DnaDriveMode.DYNAMIC
}

@Entity(
    tableName = "widget_configs",
    primaryKeys = ["profileId", "widgetTypeName"]
)
data class WidgetConfigEntity(
    val profileId: Int,
    val widgetTypeName: String,
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0,
    val isWideSpan: Boolean = true
) {
    val widgetType: CockpitWidgetType
        get() = CockpitWidgetType.entries.find { it.name == widgetTypeName }
            ?: CockpitWidgetType.SPLIT_OVERVIEW
}

@Entity(
    tableName = "favorite_apps",
    primaryKeys = ["profileId", "packageName"]
)
data class FavoriteAppEntity(
    val profileId: Int,
    val packageName: String,
    val label: String,
    val sortOrder: Int = 0
)

data class InstalledAppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val isFavorite: Boolean = false,
    val categoryHint: String = "AAOS"
)

data class TelemetryState(
    val isSimulationMode: Boolean = true,
    val hasGpsPermission: Boolean = false,
    val gpsConnected: Boolean = false,
    val speedKmh: Int = 0,
    val rpm: Int = 850,
    val gear: String = "P",
    val turboBar: Float = 0.0f,
    val lateralG: Float = 0.0f,
    val longitudinalG: Float = 0.0f,
    val compassBearingDeg: Float = 42f,
    val compassCardinal: String = "NE",
    val altitudeMeters: Int = 215,
    val currentStreetOrStatus: String = "Autostrada A2 • Kierunek Wschód",
    val nextManeuverDistance: String = "2.4 km",
    val nextManeuverInstruction: String = "Zjazd na węźle Pruszków / S8",
    val oilTempCelsius: Int = 92,
    val coolantTempCelsius: Int = 88,
    val batteryVoltage: Float = 14.2f,
    val tirePressuresBar: List<Float> = listOf(2.4f, 2.4f, 2.3f, 2.3f)
)

data class MediaPlaybackState(
    val isPlaying: Boolean = true,
    val trackTitle: String = "Monza Veloce Symphony",
    val artistName: String = "Alfa Sound System • FLAC Hi-Res",
    val albumName: String = "Quadrifoglio Sessions",
    val progressFraction: Float = 0.38f,
    val elapsedFormatted: String = "01:42",
    val durationFormatted: String = "04:28",
    val volumePercent: Int = 65,
    val audioSourceLabel: String = "AAOS Media / Bluetooth"
)

data class GestureHudEvent(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val subtitle: String,
    val gestureBadge: String
)
