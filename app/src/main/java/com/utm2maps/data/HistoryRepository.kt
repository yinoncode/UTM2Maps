package com.utm2maps.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.utm2maps.geo.Hemisphere
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.historyDataStore: DataStore<Preferences> by preferencesDataStore(name = "utm2maps_history")

class HistoryRepository(context: Context) {
    private val dataStore = context.applicationContext.historyDataStore

    val history: Flow<List<CoordinateHistoryItem>> = dataStore.data.map { prefs ->
        decodeHistory(prefs[HISTORY_JSON]).sortedByDescending { it.createdAt }.take(5)
    }

    suspend fun save(item: CoordinateHistoryItem) {
        dataStore.edit { prefs ->
            val existing = decodeHistory(prefs[HISTORY_JSON])
            prefs[HISTORY_JSON] = encodeHistory(upsertAndTrimHistory(existing, item))
        }
    }

    suspend fun delete(id: String) {
        dataStore.edit { prefs ->
            val updated = decodeHistory(prefs[HISTORY_JSON]).filterNot { it.id == id }
            prefs[HISTORY_JSON] = encodeHistory(updated)
        }
    }

    suspend fun updateTitle(id: String, newTitle: String) {
        dataStore.edit { prefs ->
            val updated = decodeHistory(prefs[HISTORY_JSON]).map {
                if (it.id == id) it.copy(title = newTitle) else it
            }
            prefs[HISTORY_JSON] = encodeHistory(updated)
        }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(HISTORY_JSON) }
    }

    private fun encodeHistory(items: List<CoordinateHistoryItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            arr.put(
                JSONObject()
                    .put("id", item.id)
                    .put("title", item.title)
                    .put("rawText", item.rawText)
                    .put("easting", item.easting)
                    .put("shortNorthing", item.shortNorthing)
                    .put("fullNorthing", item.fullNorthing)
                    .put("zone", item.zone)
                    .put("hemisphere", item.hemisphere.name)
                    .put("latitude", item.latitude)
                    .put("longitude", item.longitude)
                    .put("googleMapsUrl", item.googleMapsUrl)
                    .put("createdAt", item.createdAt)
            )
        }
        return arr.toString()
    }

    private fun decodeHistory(raw: String?): List<CoordinateHistoryItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        CoordinateHistoryItem(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            rawText = obj.getString("rawText"),
                            easting = obj.getDouble("easting"),
                            shortNorthing = obj.getString("shortNorthing"),
                            fullNorthing = obj.getDouble("fullNorthing"),
                            zone = obj.getInt("zone"),
                            hemisphere = Hemisphere.valueOf(obj.getString("hemisphere")),
                            latitude = obj.getDouble("latitude"),
                            longitude = obj.getDouble("longitude"),
                            googleMapsUrl = obj.getString("googleMapsUrl"),
                            createdAt = obj.getLong("createdAt")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private companion object {
        val HISTORY_JSON = stringPreferencesKey("coordinate_history_json")
    }
}
