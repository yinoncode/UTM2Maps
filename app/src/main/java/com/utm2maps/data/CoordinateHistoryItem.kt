package com.utm2maps.data

import com.utm2maps.geo.Hemisphere

data class CoordinateHistoryItem(
    val id: String,
    val title: String,
    val rawText: String,
    val easting: Double,
    val shortNorthing: String,
    val fullNorthing: Double,
    val zone: Int,
    val hemisphere: Hemisphere,
    val latitude: Double,
    val longitude: Double,
    val googleMapsUrl: String,
    val createdAt: Long
)

fun upsertAndTrimHistory(existing: List<CoordinateHistoryItem>, newItem: CoordinateHistoryItem): List<CoordinateHistoryItem> {
    val filtered = existing.filterNot { it.id == newItem.id }
    return (listOf(newItem) + filtered).sortedByDescending { it.createdAt }.take(5)
}
