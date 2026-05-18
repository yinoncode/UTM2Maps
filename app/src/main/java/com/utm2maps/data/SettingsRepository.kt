package com.utm2maps.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.utm2maps.geo.Hemisphere
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "utm2maps_settings")

class SettingsRepository(context: Context) {
    private val dataStore = context.applicationContext.settingsDataStore

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            zoneNumber = prefs[ZONE_NUMBER] ?: 36,
            latitudeBand = prefs[LATITUDE_BAND] ?: "R",
            hemisphere = enumPreference(prefs[HEMISPHERE], Hemisphere.NORTH),
            northingPrefix = prefs[NORTHING_PREFIX] ?: "3",
            autoOpenGoogleMaps = prefs[AUTO_OPEN_MAPS] ?: false,
            copyLinkAutomatically = prefs[COPY_LINK] ?: true,
            interfaceLanguage = enumPreference(prefs[INTERFACE_LANGUAGE], InterfaceLanguage.HEBREW)
            hemisphere = runCatching { Hemisphere.valueOf(prefs[HEMISPHERE] ?: Hemisphere.NORTH.name) }
                .getOrDefault(Hemisphere.NORTH),
            northingPrefix = prefs[NORTHING_PREFIX] ?: "3",
            autoOpenGoogleMaps = prefs[AUTO_OPEN_MAPS] ?: false,
            copyLinkAutomatically = prefs[COPY_LINK] ?: true
        )
    }

    suspend fun save(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[ZONE_NUMBER] = settings.zoneNumber
            prefs[LATITUDE_BAND] = settings.latitudeBand.uppercase()
            prefs[HEMISPHERE] = settings.hemisphere.name
            prefs[NORTHING_PREFIX] = settings.northingPrefix
            prefs[AUTO_OPEN_MAPS] = settings.autoOpenGoogleMaps
            prefs[COPY_LINK] = settings.copyLinkAutomatically
            prefs[INTERFACE_LANGUAGE] = settings.interfaceLanguage.name
        }
    }

    private inline fun <reified T : Enum<T>> enumPreference(value: String?, default: T): T =
        runCatching { enumValueOf<T>(value ?: default.name) }.getOrDefault(default)

        }
    }

    private companion object {
        val ZONE_NUMBER = intPreferencesKey("zone_number")
        val LATITUDE_BAND = stringPreferencesKey("latitude_band")
        val HEMISPHERE = stringPreferencesKey("hemisphere")
        val NORTHING_PREFIX = stringPreferencesKey("northing_prefix")
        val AUTO_OPEN_MAPS = booleanPreferencesKey("auto_open_maps")
        val COPY_LINK = booleanPreferencesKey("copy_link")
        val INTERFACE_LANGUAGE = stringPreferencesKey("interface_language")
    }
}
