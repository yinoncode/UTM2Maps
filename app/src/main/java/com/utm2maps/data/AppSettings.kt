package com.utm2maps.data

import com.utm2maps.geo.Hemisphere

data class AppSettings(
    val zoneNumber: Int = 36,
    val latitudeBand: String = "R",
    val hemisphere: Hemisphere = Hemisphere.NORTH,
    val northingPrefix: String = "3",
    val autoOpenGoogleMaps: Boolean = false,
    val copyLinkAutomatically: Boolean = true
)
