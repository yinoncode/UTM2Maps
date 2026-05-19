package com.utm2maps.geo

import kotlin.math.*

object UtmConverter {
    private const val WGS84_A = 6378137.0
    private const val WGS84_ECC_SQUARED = 0.0066943799901413165
    private const val K0 = 0.9996

    /**
     * Converts UTM coordinates to WGS84 latitude/longitude using the standard inverse
     * Transverse Mercator projection. This is fully local and does not call any service.
     */
    fun toLatLon(easting: Double, northing: Double, zone: Int, hemisphere: Hemisphere): LatLon {
        require(zone in 1..60) { "UTM zone must be in 1..60" }
        require(easting in 100_000.0..900_000.0) { "Easting is outside the valid UTM range" }
        require(northing in 0.0..10_000_000.0) { "Northing is outside the valid UTM range" }

        val eccPrimeSquared = WGS84_ECC_SQUARED / (1 - WGS84_ECC_SQUARED)
        val x = easting - 500_000.0
        var y = northing
        if (hemisphere == Hemisphere.SOUTH) y -= 10_000_000.0

        val longOrigin = (zone - 1) * 6 - 180 + 3.0
        val m = y / K0
        val mu = m / (WGS84_A * (1 - WGS84_ECC_SQUARED / 4 - 3 * WGS84_ECC_SQUARED.pow(2) / 64 - 5 * WGS84_ECC_SQUARED.pow(3) / 256))

        val e1 = (1 - sqrt(1 - WGS84_ECC_SQUARED)) / (1 + sqrt(1 - WGS84_ECC_SQUARED))
        val phi1Rad = mu +
            (3 * e1 / 2 - 27 * e1.pow(3) / 32) * sin(2 * mu) +
            (21 * e1.pow(2) / 16 - 55 * e1.pow(4) / 32) * sin(4 * mu) +
            (151 * e1.pow(3) / 96) * sin(6 * mu) +
            (1097 * e1.pow(4) / 512) * sin(8 * mu)

        val n1 = WGS84_A / sqrt(1 - WGS84_ECC_SQUARED * sin(phi1Rad).pow(2))
        val t1 = tan(phi1Rad).pow(2)
        val c1 = eccPrimeSquared * cos(phi1Rad).pow(2)
        val r1 = WGS84_A * (1 - WGS84_ECC_SQUARED) / (1 - WGS84_ECC_SQUARED * sin(phi1Rad).pow(2)).pow(1.5)
        val d = x / (n1 * K0)

        val latRad = phi1Rad - (n1 * tan(phi1Rad) / r1) * (
            d.pow(2) / 2 -
                (5 + 3 * t1 + 10 * c1 - 4 * c1.pow(2) - 9 * eccPrimeSquared) * d.pow(4) / 24 +
                (61 + 90 * t1 + 298 * c1 + 45 * t1.pow(2) - 252 * eccPrimeSquared - 3 * c1.pow(2)) * d.pow(6) / 720
            )

        val lonRad = (
            d -
                (1 + 2 * t1 + c1) * d.pow(3) / 6 +
                (5 - 2 * c1 + 28 * t1 - 3 * c1.pow(2) + 8 * eccPrimeSquared + 24 * t1.pow(2)) * d.pow(5) / 120
            ) / cos(phi1Rad)

        return LatLon(
            latitude = Math.toDegrees(latRad),
            longitude = longOrigin + Math.toDegrees(lonRad)
        )
    }
}

fun buildGoogleMapsUrl(latLon: LatLon): String =
    "https://www.google.com/maps?q=${latLon.latitude},${latLon.longitude}"
