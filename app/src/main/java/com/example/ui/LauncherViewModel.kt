package com.example.ui

import android.content.Context
import android.provider.Settings
import android.view.KeyEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CockpitVisualTheme
import com.example.data.CockpitWidgetType
import com.example.data.DnaDriveMode
import com.example.data.DriverProfileEntity
import com.example.data.FavoriteAppEntity
import com.example.data.GestureHudEvent
import com.example.data.InstalledAppInfo
import com.example.data.LauncherDatabase
import com.example.data.LauncherRepository
import com.example.data.MediaPlaybackState
import com.example.data.TelemetryState
import com.example.data.WidgetConfigEntity
import com.example.sensors.TelemetryAndMediaController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class DemoTrack(
    val title: String,
    val artist: String,
    val album: String,
    val durationSec: Int
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LauncherViewModel(
    private val repository: LauncherRepository,
    private val telemetryMediaController: TelemetryAndMediaController
) : ViewModel() {

    private val demoPlaylist = listOf(
        DemoTrack(
            title = "Monza Veloce Symphony",
            artist = "Alfa Sound System • FLAC Hi-Res",
            album = "Quadrifoglio Sessions",
            durationSec = 268
        ),
        DemoTrack(
            title = "Passo dello Stelvio Drive",
            artist = "Milano Synth Orchestra",
            album = "Arese Heritage Vol. II",
            durationSec = 234
        ),
        DemoTrack(
            title = "Rosso Competizione 2.9 V6",
            artist = "Balocco Telemetry Audio",
            album = "Autodromo Nights",
            durationSec = 310
        ),
        DemoTrack(
            title = "Turismo Internazionale",
            artist = "Italo Electro GT",
            album = "Stradale 1967–2026",
            durationSec = 205
        )
    )
    private var currentTrackIndex = 0
    private var currentTrackElapsedSec = 102

    val allProfiles: StateFlow<List<DriverProfileEntity>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<DriverProfileEntity> = repository.activeProfile
        .combine(allProfiles) { active, list ->
            active ?: list.firstOrNull() ?: DriverProfileEntity(
                id = 1,
                name = "Kierowca 1 • Quadrifoglio",
                badgeTitle = "SPORT CARBON",
                isActive = true
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DriverProfileEntity(
                id = 1,
                name = "Kierowca 1 • Quadrifoglio",
                badgeTitle = "SPORT CARBON",
                isActive = true
            )
        )

    val widgetConfigs: StateFlow<List<WidgetConfigEntity>> = activeProfile
        .flatMapLatest { profile ->
            if (profile.id <= 0) flowOf(emptyList())
            else repository.observeWidgets(profile.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val favoriteEntities: StateFlow<List<FavoriteAppEntity>> = activeProfile
        .flatMapLatest { profile ->
            if (profile.id <= 0) flowOf(emptyList())
            else repository.observeFavorites(profile.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _rawInstalledApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    private val _appSearchQuery = MutableStateFlow("")
    val appSearchQuery: StateFlow<String> = _appSearchQuery.asStateFlow()

    val installedApps: StateFlow<List<InstalledAppInfo>> = combine(
        _rawInstalledApps,
        favoriteEntities,
        _appSearchQuery
    ) { rawApps, favs, query ->
        val favSet = favs.map { it.packageName }.toSet()
        val enriched = rawApps.map { app ->
            app.copy(isFavorite = app.packageName in favSet)
        }
        val filtered = if (query.isBlank()) {
            enriched
        } else {
            val q = query.trim().lowercase()
            enriched.filter {
                it.label.lowercase().contains(q) ||
                    it.categoryHint.lowercase().contains(q) ||
                    it.packageName.lowercase().contains(q)
            }
        }
        filtered.sortedWith(
            compareByDescending<InstalledAppInfo> { it.isFavorite }.thenBy { it.label.lowercase() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _telemetry = MutableStateFlow(TelemetryState())
    val telemetry: StateFlow<TelemetryState> = _telemetry.asStateFlow()

    private val _mediaState = MutableStateFlow(
        MediaPlaybackState(volumePercent = telemetryMediaController.getVolumePercent())
    )
    val mediaState: StateFlow<MediaPlaybackState> = _mediaState.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _gestureHudEvent = MutableStateFlow<GestureHudEvent?>(null)
    val gestureHudEvent: StateFlow<GestureHudEvent?> = _gestureHudEvent.asStateFlow()

    private val _activeModalSheet = MutableStateFlow<CockpitModalSheet?>(null)
    val activeModalSheet: StateFlow<CockpitModalSheet?> = _activeModalSheet.asStateFlow()

    private var hudClearJob: Job? = null
    private var demoLoopTick = 0L

    init {
        viewModelScope.launch {
            repository.ensureDefaultProfilesSeeded()
            refreshInstalledApps()
        }
        startSensorAndTelemetryLoop()
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            val apps = repository.loadInstalledLaunchableApps()
            _rawInstalledApps.value = apps
        }
    }

    private fun startSensorAndTelemetryLoop() {
        telemetryMediaController.startSensorTracking { latG, lonG, azimuth ->
            if (!_telemetry.value.isSimulationMode) {
                _telemetry.update { state ->
                    val newBearing = azimuth ?: state.compassBearingDeg
                    state.copy(
                        lateralG = latG,
                        longitudinalG = lonG,
                        compassBearingDeg = newBearing,
                        compassCardinal = TelemetryAndMediaController.cardinalFromDegrees(newBearing)
                    )
                }
            }
        }

        viewModelScope.launch {
            while (isActive) {
                delay(250L)
                demoLoopTick++

                if (_telemetry.value.isSimulationMode) {
                    val dna = activeProfile.value.dnaMode
                    val frame = TelemetryAndMediaController.computeDemoTelemetryStep(
                        tick = demoLoopTick,
                        throttleMultiplier = dna.throttleMultiplier
                    )
                    _telemetry.update { state ->
                        state.copy(
                            speedKmh = frame.speedKmh,
                            rpm = frame.rpm,
                            gear = frame.gear,
                            turboBar = frame.turboBar,
                            lateralG = frame.lateralG,
                            longitudinalG = frame.longitudinalG,
                            compassBearingDeg = frame.bearingDeg,
                            compassCardinal = frame.cardinal,
                            currentStreetOrStatus = frame.streetStatus
                        )
                    }
                }

                if (demoLoopTick % 4L == 0L && _mediaState.value.isPlaying) {
                    val track = demoPlaylist[currentTrackIndex]
                    currentTrackElapsedSec = (currentTrackElapsedSec + 1) % track.durationSec.coerceAtLeast(1)
                    val progress = currentTrackElapsedSec.toFloat() / track.durationSec.toFloat()
                    _mediaState.update {
                        it.copy(
                            progressFraction = progress.coerceIn(0f, 1f),
                            elapsedFormatted = formatSeconds(currentTrackElapsedSec),
                            durationFormatted = formatSeconds(track.durationSec)
                        )
                    }
                }
            }
        }
    }

    fun onGpsPermissionResult(granted: Boolean) {
        _telemetry.update { it.copy(hasGpsPermission = granted) }
        if (granted) {
            val started = telemetryMediaController.startRealGpsTracking { speedKmh, bearing, altitude ->
                if (!_telemetry.value.isSimulationMode) {
                    val estimatedRpm = if (speedKmh == 0) 800 else (1400 + speedKmh * 34).coerceIn(800, 6800)
                    val gear = when {
                        speedKmh == 0 -> "D1"
                        speedKmh < 25 -> "D2"
                        speedKmh < 50 -> "D3"
                        speedKmh < 80 -> "D4"
                        speedKmh < 110 -> "D5"
                        else -> "D6"
                    }
                    _telemetry.update { state ->
                        val nextBearing = bearing ?: state.compassBearingDeg
                        state.copy(
                            gpsConnected = true,
                            speedKmh = speedKmh,
                            rpm = estimatedRpm,
                            gear = gear,
                            turboBar = ((estimatedRpm - 1200).coerceAtLeast(0) / 5000f).coerceIn(0f, 1.5f),
                            compassBearingDeg = nextBearing,
                            compassCardinal = TelemetryAndMediaController.cardinalFromDegrees(nextBearing),
                            altitudeMeters = altitude,
                            currentStreetOrStatus = "Sygnał GPS Aktywny • Pomiar na żywo"
                        )
                    }
                }
            }
            if (started && !_telemetry.value.isSimulationMode) {
                showHudEvent(
                    title = "Telemetria GPS Live",
                    subtitle = "Połączono z odbiornikiem GPS pojazdu",
                    badge = "GPS"
                )
            }
        }
    }

    fun toggleSimulationMode() {
        val nextSim = !_telemetry.value.isSimulationMode
        _telemetry.update {
            it.copy(
                isSimulationMode = nextSim,
                speedKmh = if (nextSim) it.speedKmh else 0,
                rpm = if (nextSim) it.rpm else 820,
                gear = if (nextSim) "D3" else "D1",
                currentStreetOrStatus = if (nextSim) {
                    "Tryb Symulacji Demo • Telemetria Aktywna"
                } else {
                    "Oczekiwanie na prędkość z czujnika GPS..."
                }
            )
        }
        showHudEvent(
            title = if (nextSim) "Tryb Symulacji Demo" else "Prawdziwy Czujnik GPS",
            subtitle = if (nextSim) "Dynamiczna symulacja zegarów i G-Force" else "Pomiar rzeczywistej prędkości GPS",
            badge = if (nextSim) "DEMO" else "GPS"
        )
    }

    fun cycleDnaMode(fromGesture: Boolean = false) {
        val currentProfile = activeProfile.value
        val nextDna = currentProfile.dnaMode.next()
        viewModelScope.launch {
            repository.updateProfile(currentProfile.copy(dnaModeName = nextDna.name))
        }
        showHudEvent(
            title = "Tryb Alfa DNA: ${nextDna.labelPl}",
            subtitle = nextDna.descriptionPl,
            badge = if (fromGesture) "GEST 3 PALCE / 2x TAP" else "DNA ${nextDna.code}"
        )
    }

    fun selectDnaMode(mode: DnaDriveMode) {
        val currentProfile = activeProfile.value
        viewModelScope.launch {
            repository.updateProfile(currentProfile.copy(dnaModeName = mode.name))
        }
        showHudEvent(
            title = "Tryb Alfa DNA: ${mode.labelPl}",
            subtitle = mode.descriptionPl,
            badge = "DNA ${mode.code}"
        )
    }

    fun toggleDayNightMode(fromGesture: Boolean = false) {
        val currentProfile = activeProfile.value
        val nextNight = !currentProfile.isNightMode
        viewModelScope.launch {
            repository.updateProfile(currentProfile.copy(isNightMode = nextNight))
        }
        showHudEvent(
            title = if (nextNight) "Kokpit Nocny (Night Mode)" else "Kokpit Dzienny (Day Mode)",
            subtitle = "Dostosowano luminancję zegarów i kontrast kart",
            badge = if (fromGesture) "GEST KOKPITU" else "EKRAN"
        )
    }

    fun selectVisualTheme(theme: CockpitVisualTheme) {
        val currentProfile = activeProfile.value
        viewModelScope.launch {
            repository.updateProfile(
                currentProfile.copy(
                    visualThemeName = theme.name,
                    badgeTitle = when (theme) {
                        CockpitVisualTheme.ALFA_CARBON -> "SPORT CARBON"
                        CockpitVisualTheme.AAOS_MINIMAL -> "AAOS M3"
                        CockpitVisualTheme.CANNOCCHIALE_CLASSIC -> "CANNOCCHIALE"
                    }
                )
            )
        }
        showHudEvent(
            title = "Motyw: ${theme.titlePl}",
            subtitle = theme.subtitlePl,
            badge = "STYL"
        )
    }

    fun adjustVolumeByGesture(deltaPercent: Int) {
        val newVol = telemetryMediaController.adjustVolumeByDeltaPercent(deltaPercent)
        _mediaState.update { it.copy(volumePercent = newVol) }
        val sign = if (deltaPercent >= 0) "+" else ""
        showHudEvent(
            title = "Głośność Systemu: $newVol%",
            subtitle = "Zmiana o $sign$deltaPercent% (Przesunięcie 2 palcami Góra/Dół)",
            badge = "2 PALCE • AUDIO"
        )
    }

    fun setVolumeDirect(targetPercent: Int) {
        val delta = targetPercent - _mediaState.value.volumePercent
        val newVol = telemetryMediaController.adjustVolumeByDeltaPercent(delta)
        _mediaState.update { it.copy(volumePercent = newVol) }
    }

    fun skipTrack(next: Boolean, fromGesture: Boolean = false) {
        telemetryMediaController.dispatchMediaKey(
            if (next) KeyEvent.KEYCODE_MEDIA_NEXT else KeyEvent.KEYCODE_MEDIA_PREVIOUS
        )
        currentTrackIndex = if (next) {
            (currentTrackIndex + 1) % demoPlaylist.size
        } else {
            (currentTrackIndex - 1 + demoPlaylist.size) % demoPlaylist.size
        }
        currentTrackElapsedSec = 0
        val track = demoPlaylist[currentTrackIndex]
        _mediaState.update {
            it.copy(
                isPlaying = true,
                trackTitle = track.title,
                artistName = track.artist,
                albumName = track.album,
                progressFraction = 0f,
                elapsedFormatted = "00:00",
                durationFormatted = formatSeconds(track.durationSec)
            )
        }
        showHudEvent(
            title = if (next) "Następny: ${track.title}" else "Poprzedni: ${track.title}",
            subtitle = track.artist,
            badge = if (fromGesture) "2 PALCE • UTWÓR" else "MEDIA"
        )
    }

    fun togglePlayPause() {
        telemetryMediaController.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        val nextPlaying = !_mediaState.value.isPlaying
        _mediaState.update { it.copy(isPlaying = nextPlaying) }
        showHudEvent(
            title = if (nextPlaying) "Odtwarzanie Wznowione" else "Odtwarzanie Wstrzymane",
            subtitle = _mediaState.value.trackTitle,
            badge = "MEDIA"
        )
    }

    fun toggleEditMode() {
        val next = !_isEditMode.value
        _isEditMode.value = next
        showHudEvent(
            title = if (next) "Tryb Edycji Ekranu Głównego" else "Zapisano Układ Kokpitu",
            subtitle = if (next) {
                "Przeciągaj kafelki, zmieniaj ich szerokość lub włączaj/wyłączaj moduły"
            } else {
                "Konfiguracja zapisana w profilu: ${activeProfile.value.name}"
            },
            badge = "UKŁAD"
        )
    }

    fun enableEditModeFromLongPress() {
        if (!_isEditMode.value) {
            _isEditMode.value = true
            showHudEvent(
                title = "Tryb Edycji Aktywny",
                subtitle = "Użyj strzałek/przeciągania aby zmienić kolejność lub przełącz widoczność modułów",
                badge = "LONG PRESS"
            )
        }
    }

    fun setWidgetEnabled(widget: WidgetConfigEntity, enabled: Boolean) {
        viewModelScope.launch {
            repository.setWidgetEnabled(widget, enabled)
        }
    }

    fun toggleWidgetSpan(widget: WidgetConfigEntity) {
        viewModelScope.launch {
            repository.toggleWidgetSpan(widget)
        }
    }

    fun moveWidgetOrder(widget: WidgetConfigEntity, moveUpOrLeft: Boolean) {
        val currentList = widgetConfigs.value.sortedBy { it.sortOrder }
        val index = currentList.indexOfFirst { it.widgetTypeName == widget.widgetTypeName }
        if (index == -1) return
        val targetIndex = if (moveUpOrLeft) index - 1 else index + 1
        if (targetIndex !in currentList.indices) return
        val targetWidget = currentList[targetIndex]
        viewModelScope.launch {
            repository.swapWidgetOrder(
                profileId = activeProfile.value.id,
                fromWidgetTypeName = widget.widgetTypeName,
                toWidgetTypeName = targetWidget.widgetTypeName
            )
        }
    }

    fun swapWidgetsByType(fromType: CockpitWidgetType, toType: CockpitWidgetType) {
        if (fromType == toType) return
        viewModelScope.launch {
            repository.swapWidgetOrder(
                profileId = activeProfile.value.id,
                fromWidgetTypeName = fromType.name,
                toWidgetTypeName = toType.name
            )
        }
    }

    fun resetWidgetsLayout() {
        viewModelScope.launch {
            repository.resetWidgetsToDefault(activeProfile.value.id)
            showHudEvent(
                title = "Przywrócono Domyślny Układ",
                subtitle = "Wszystkie 4 moduły ekranu głównego zostały włączone",
                badge = "RESET"
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _appSearchQuery.value = query
    }

    fun toggleFavoriteApp(app: InstalledAppInfo) {
        viewModelScope.launch {
            repository.toggleFavoriteApp(
                profileId = activeProfile.value.id,
                app = app,
                currentOrderCount = favoriteEntities.value.size
            )
        }
    }

    fun launchInstalledApp(app: InstalledAppInfo) {
        val ok = repository.launchApp(app.packageName)
        if (!ok) {
            showHudEvent(
                title = "Uruchamianie: ${app.label}",
                subtitle = "Pakiet: ${app.packageName}",
                badge = "AAOS APP"
            )
        }
    }

    fun launchSystemShortcut(shortcutType: SystemQuickShortcut) {
        val action = when (shortcutType) {
            SystemQuickShortcut.MAPS -> android.content.Intent.ACTION_VIEW
            SystemQuickShortcut.PHONE -> android.content.Intent.ACTION_DIAL
            SystemQuickShortcut.SETTINGS -> Settings.ACTION_SETTINGS
            SystemQuickShortcut.SOUND_SETTINGS -> Settings.ACTION_SOUND_SETTINGS
        }
        val launched = repository.launchSystemActionIntent(action)
        if (!launched) {
            showHudEvent(
                title = shortcutType.labelPl,
                subtitle = "Skrót systemowy AAOS aktywowany",
                badge = "SYSTEM"
            )
        }
    }

    // AAOS Seat Heating Controls (3-stage L/R directly on AAOS dock)
    fun cycleLeftSeatHeat() {
        val p = activeProfile.value
        val nextLevel = (p.leftSeatHeatLevel + 1) % 4
        viewModelScope.launch {
            repository.updateProfile(p.copy(leftSeatHeatLevel = nextLevel))
        }
        val levelLabel = when (nextLevel) {
            0 -> "Wyłączone (0/3)"
            1 -> "Stopień 1 • Niski"
            2 -> "Stopień 2 • Średni"
            else -> "Stopień 3 • Maksymalny"
        }
        showHudEvent(
            title = "Fotel Kierowcy (L): $levelLabel",
            subtitle = "AAOS VehicleProperty: HVAC_SEAT_TEMPERATURE (Strefa Lewa)",
            badge = "FOTEL L"
        )
    }

    fun cycleRightSeatHeat() {
        val p = activeProfile.value
        val nextLevel = (p.rightSeatHeatLevel + 1) % 4
        viewModelScope.launch {
            repository.updateProfile(p.copy(rightSeatHeatLevel = nextLevel))
        }
        val levelLabel = when (nextLevel) {
            0 -> "Wyłączone (0/3)"
            1 -> "Stopień 1 • Niski"
            2 -> "Stopień 2 • Średni"
            else -> "Stopień 3 • Maksymalny"
        }
        showHudEvent(
            title = "Fotel Pasażera (P): $levelLabel",
            subtitle = "AAOS VehicleProperty: HVAC_SEAT_TEMPERATURE (Strefa Prawa)",
            badge = "FOTEL P"
        )
    }

    fun setShowSeatHeatingControls(enabled: Boolean) {
        val p = activeProfile.value
        viewModelScope.launch {
            repository.updateProfile(p.copy(showSeatHeatingControls = enabled))
        }
        showHudEvent(
            title = if (enabled) "Podgrzewanie Foteli AAOS: Włączone" else "Podgrzewanie Foteli AAOS: Ukryte",
            subtitle = "Przyciski foteli L/P na dolnym pasku dokowania",
            badge = "USTAWIENIA"
        )
    }

    // Optional read-only HVAC temperature preview setting (disabled by default)
    fun setShowHvacTempPreview(enabled: Boolean) {
        val p = activeProfile.value
        viewModelScope.launch {
            repository.updateProfile(p.copy(showHvacTempPreview = enabled))
        }
        showHudEvent(
            title = if (enabled) "Podgląd Temperatur HVAC: Włączony" else "Podgląd Temperatur HVAC: Wyłączony",
            subtitle = if (enabled) {
                "Pasywny wskaźnik temperatur L/P widoczny na pasku dokowania"
            } else {
                "Ukryto wskaźnik temperatur na pasku dokowania"
            },
            badge = "USTAWIENIA"
        )
    }

    // Profiles management
    fun switchDriverProfile(profileId: Int) {
        viewModelScope.launch {
            repository.switchProfile(profileId)
            val chosen = allProfiles.value.find { it.id == profileId }
            if (chosen != null) {
                showHudEvent(
                    title = "Wczytano Profil: ${chosen.name}",
                    subtitle = "Motyw: ${chosen.visualTheme.titlePl} • DNA: ${chosen.dnaMode.code}",
                    badge = "PROFIL"
                )
            }
        }
    }

    fun createNewDriverProfile(
        name: String,
        theme: CockpitVisualTheme,
        dna: DnaDriveMode,
        isNight: Boolean
    ) {
        viewModelScope.launch {
            repository.createNewProfile(name, theme, dna, isNight)
            showHudEvent(
                title = "Utworzono Profil: $name",
                subtitle = "Zapisano w bazie danych urządzenia",
                badge = "NOWY PROFIL"
            )
        }
    }

    fun openModalSheet(sheet: CockpitModalSheet?) {
        _activeModalSheet.value = sheet
    }

    private fun showHudEvent(title: String, subtitle: String, badge: String) {
        _gestureHudEvent.value = GestureHudEvent(
            title = title,
            subtitle = subtitle,
            gestureBadge = badge
        )
        hudClearJob?.cancel()
        hudClearJob = viewModelScope.launch {
            delay(2300L)
            _gestureHudEvent.value = null
        }
    }

    private fun formatSeconds(totalSec: Int): String {
        val mins = totalSec / 60
        val secs = totalSec % 60
        return "%02d:%02d".format(mins, secs)
    }

    override fun onCleared() {
        super.onCleared()
        telemetryMediaController.stopRealGpsTracking()
        telemetryMediaController.stopSensorTracking()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = LauncherDatabase.getInstance(context)
                    val repo = LauncherRepository(context.applicationContext, db.launcherDao())
                    val controller = TelemetryAndMediaController(context.applicationContext)
                    return LauncherViewModel(repo, controller) as T
                }
            }
        }
    }
}

enum class CockpitModalSheet {
    WIDGET_CUSTOMIZER,
    DRIVER_PROFILES,
    FULL_APP_DRAWER
}

enum class SystemQuickShortcut(val labelPl: String) {
    MAPS("Nawigacja"),
    PHONE("Telefon"),
    SETTINGS("System AAOS"),
    SOUND_SETTINGS("DSP Audio")
}
