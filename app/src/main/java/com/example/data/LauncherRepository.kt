package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LauncherRepository(
    private val context: Context,
    private val dao: LauncherDao
) {
    val allProfiles: Flow<List<DriverProfileEntity>> = dao.observeAllProfiles()
    val activeProfile: Flow<DriverProfileEntity?> = dao.observeActiveProfile()

    fun observeWidgets(profileId: Int): Flow<List<WidgetConfigEntity>> =
        dao.observeWidgetsForProfile(profileId)

    fun observeFavorites(profileId: Int): Flow<List<FavoriteAppEntity>> =
        dao.observeFavoritesForProfile(profileId)

    suspend fun ensureDefaultProfilesSeeded() {
        val existing = dao.getAllProfilesOnce()
        if (existing.isNotEmpty()) return

        val sportProfileId = dao.insertProfile(
            DriverProfileEntity(
                name = "Kierowca 1 • Quadrifoglio",
                badgeTitle = "SPORT CARBON",
                visualThemeName = CockpitVisualTheme.ALFA_CARBON.name,
                dnaModeName = DnaDriveMode.DYNAMIC.name,
                isNightMode = true,
                isActive = true,
                showSeatHeatingControls = true,
                leftSeatHeatLevel = 1,
                rightSeatHeatLevel = 0,
                showHvacTempPreview = false,
                leftTempCelsius = 20.5f,
                rightTempCelsius = 21.0f
            )
        ).toInt()

        val minimalProfileId = dao.insertProfile(
            DriverProfileEntity(
                name = "Trasa • AAOS Komfort",
                badgeTitle = "AAOS M3",
                visualThemeName = CockpitVisualTheme.AAOS_MINIMAL.name,
                dnaModeName = DnaDriveMode.NATURAL.name,
                isNightMode = true,
                isActive = false,
                showSeatHeatingControls = true,
                leftSeatHeatLevel = 0,
                rightSeatHeatLevel = 0,
                showHvacTempPreview = false,
                leftTempCelsius = 22.0f,
                rightTempCelsius = 22.0f
            )
        ).toInt()

        val classicProfileId = dao.insertProfile(
            DriverProfileEntity(
                name = "Gran Turismo • Classic",
                badgeTitle = "CANNOCCHIALE",
                visualThemeName = CockpitVisualTheme.CANNOCCHIALE_CLASSIC.name,
                dnaModeName = DnaDriveMode.ADVANCED.name,
                isNightMode = true,
                isActive = false,
                showSeatHeatingControls = true,
                leftSeatHeatLevel = 2,
                rightSeatHeatLevel = 1,
                showHvacTempPreview = false,
                leftTempCelsius = 21.5f,
                rightTempCelsius = 21.5f
            )
        ).toInt()

        listOf(sportProfileId, minimalProfileId, classicProfileId).forEach { pid ->
            dao.insertWidgetConfigs(defaultWidgetsForProfile(pid))
        }
    }

    private fun defaultWidgetsForProfile(profileId: Int): List<WidgetConfigEntity> {
        return CockpitWidgetType.entries.mapIndexed { index, type ->
            WidgetConfigEntity(
                profileId = profileId,
                widgetTypeName = type.name,
                isEnabled = type != CockpitWidgetType.DOCK_BAR,
                sortOrder = index,
                isWideSpan = type.defaultWide
            )
        }
    }

    suspend fun createNewProfile(
        name: String,
        visualTheme: CockpitVisualTheme,
        dnaMode: DnaDriveMode,
        isNightMode: Boolean
    ) {
        val newId = dao.insertProfile(
            DriverProfileEntity(
                name = name.ifBlank { "Profil Własny" },
                badgeTitle = visualTheme.name.take(10),
                visualThemeName = visualTheme.name,
                dnaModeName = dnaMode.name,
                isNightMode = isNightMode,
                isActive = false
            )
        ).toInt()
        dao.insertWidgetConfigs(defaultWidgetsForProfile(newId))
        dao.switchActiveProfile(newId)
    }

    suspend fun switchProfile(profileId: Int) {
        val widgets = dao.getWidgetsForProfileOnce(profileId)
        if (widgets.isEmpty()) {
            dao.insertWidgetConfigs(defaultWidgetsForProfile(profileId))
        }
        dao.switchActiveProfile(profileId)
    }

    suspend fun updateProfile(profile: DriverProfileEntity) {
        dao.updateProfile(profile)
    }

    suspend fun setWidgetEnabled(widget: WidgetConfigEntity, enabled: Boolean) {
        dao.updateWidgetConfig(widget.copy(isEnabled = enabled))
    }

    suspend fun toggleWidgetSpan(widget: WidgetConfigEntity) {
        dao.updateWidgetConfig(widget.copy(isWideSpan = !widget.isWideSpan))
    }

    suspend fun swapWidgetOrder(
        profileId: Int,
        fromWidgetTypeName: String,
        toWidgetTypeName: String
    ) {
        val current = dao.getWidgetsForProfileOnce(profileId).sortedBy { it.sortOrder }.toMutableList()
        val fromIdx = current.indexOfFirst { it.widgetTypeName == fromWidgetTypeName }
        val toIdx = current.indexOfFirst { it.widgetTypeName == toWidgetTypeName }
        if (fromIdx == -1 || toIdx == -1 || fromIdx == toIdx) return

        val moved = current.removeAt(fromIdx)
        current.add(toIdx, moved)
        val reindexed = current.mapIndexed { idx, item -> item.copy(sortOrder = idx) }
        dao.insertWidgetConfigs(reindexed)
    }

    suspend fun resetWidgetsToDefault(profileId: Int) {
        dao.insertWidgetConfigs(defaultWidgetsForProfile(profileId))
    }

    suspend fun toggleFavoriteApp(profileId: Int, app: InstalledAppInfo, currentOrderCount: Int) {
        if (app.isFavorite) {
            dao.removeFavoriteApp(profileId, app.packageName)
        } else {
            dao.insertFavoriteApp(
                FavoriteAppEntity(
                    profileId = profileId,
                    packageName = app.packageName,
                    label = app.label,
                    sortOrder = currentOrderCount
                )
            )
        }
    }

    suspend fun loadInstalledLaunchableApps(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
        val ownPkg = context.packageName

        val resolvedApps = resolveInfos
            .mapNotNull { info ->
                val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                if (pkg == ownPkg) return@mapNotNull null
                val label = info.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: pkg
                val icon = runCatching { info.loadIcon(pm) }.getOrNull()
                val category = when {
                    pkg.contains("maps", ignoreCase = true) ||
                        pkg.contains("nav", ignoreCase = true) -> "Nawigacja"
                    pkg.contains("music", ignoreCase = true) ||
                        pkg.contains("audio", ignoreCase = true) ||
                        pkg.contains("spotify", ignoreCase = true) ||
                        pkg.contains("youtube", ignoreCase = true) -> "Multimedia"
                    pkg.contains("dialer", ignoreCase = true) ||
                        pkg.contains("phone", ignoreCase = true) ||
                        pkg.contains("contacts", ignoreCase = true) -> "Komunikacja"
                    else -> "System AAOS"
                }
                InstalledAppInfo(
                    packageName = pkg,
                    label = label,
                    icon = icon,
                    isFavorite = false,
                    categoryHint = category
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }

        resolvedApps
    }

    fun launchApp(packageName: String): Boolean {
        return runCatching {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        }.getOrDefault(false)
    }

    fun launchSystemActionIntent(action: String): Boolean {
        return runCatching {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
