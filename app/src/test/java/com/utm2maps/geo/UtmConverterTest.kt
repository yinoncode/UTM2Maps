package com.utm2maps.geo

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtmConverterTest {
    @Test
    fun convertsRequiredSampleCoordinates() {
        assertNear(31.012301, 34.352145, UtmConverter.toLatLon(629073.0, 3_431_750.0, 36, Hemisphere.NORTH))
        assertNear(30.988045, 34.364641, UtmConverter.toLatLon(630299.0, 3_429_076.0, 36, Hemisphere.NORTH))
        assertNear(31.010608, 34.355305, UtmConverter.toLatLon(629377.0, 3_431_566.0, 36, Hemisphere.NORTH))
    }

    @Test
    fun buildsGoogleMapsUrl() {
        val url = buildGoogleMapsUrl(LatLon(31.012301, 34.352145))
        assertEquals("https://www.google.com/maps?q=31.012301,34.352145", url)
    }

    private fun assertNear(expectedLat: Double, expectedLon: Double, actual: LatLon) {
        assertTrue(abs(actual.latitude - expectedLat) < 0.0002, "latitude ${actual.latitude} is not near $expectedLat")
        assertTrue(abs(actual.longitude - expectedLon) < 0.0002, "longitude ${actual.longitude} is not near $expectedLon")
    }
}
