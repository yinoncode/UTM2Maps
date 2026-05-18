package com.utm2maps.geo

data class UtmCandidate(
    val rawText: String,
    val easting: Double,
    val shortNorthing: String,
    val fullNorthing: Double,
    val zone: Int,
    val hemisphere: Hemisphere
)

data class LatLon(
    val latitude: Double,
    val longitude: Double
)

enum class Hemisphere {
    NORTH, SOUTH
}
