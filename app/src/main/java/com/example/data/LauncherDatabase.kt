package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDao {
    @Query("SELECT * FROM driver_profiles ORDER BY id ASC")
    fun observeAllProfiles(): Flow<List<DriverProfileEntity>>

    @Query("SELECT * FROM driver_profiles WHERE isActive = 1 LIMIT 1")
    fun observeActiveProfile(): Flow<DriverProfileEntity?>

    @Query("SELECT * FROM driver_profiles ORDER BY id ASC")
    suspend fun getAllProfilesOnce(): List<DriverProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: DriverProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: DriverProfileEntity)

    @Query("UPDATE driver_profiles SET isActive = CASE WHEN id = :profileId THEN 1 ELSE 0 END")
    suspend fun setActiveProfileId(profileId: Int)

    @Query("SELECT * FROM widget_configs WHERE profileId = :profileId ORDER BY sortOrder ASC")
    fun observeWidgetsForProfile(profileId: Int): Flow<List<WidgetConfigEntity>>

    @Query("SELECT * FROM widget_configs WHERE profileId = :profileId ORDER BY sortOrder ASC")
    suspend fun getWidgetsForProfileOnce(profileId: Int): List<WidgetConfigEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidgetConfigs(configs: List<WidgetConfigEntity>)

    @Update
    suspend fun updateWidgetConfig(config: WidgetConfigEntity)

    @Query("SELECT * FROM favorite_apps WHERE profileId = :profileId ORDER BY sortOrder ASC")
    fun observeFavoritesForProfile(profileId: Int): Flow<List<FavoriteAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteApp(app: FavoriteAppEntity)

    @Query("DELETE FROM favorite_apps WHERE profileId = :profileId AND packageName = :packageName")
    suspend fun removeFavoriteApp(profileId: Int, packageName: String)

    @Transaction
    suspend fun switchActiveProfile(profileId: Int) {
        setActiveProfileId(profileId)
    }
}

@Database(
    entities = [
        DriverProfileEntity::class,
        WidgetConfigEntity::class,
        FavoriteAppEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun launcherDao(): LauncherDao

    companion object {
        @Volatile
        private var INSTANCE: LauncherDatabase? = null

        fun getInstance(context: Context): LauncherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LauncherDatabase::class.java,
                    "alfa_aaos_launcher.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
