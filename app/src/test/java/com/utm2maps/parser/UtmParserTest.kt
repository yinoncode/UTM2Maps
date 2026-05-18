package com.utm2maps.parser

import com.utm2maps.geo.Hemisphere
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtmParserTest {
    @Test
    fun parsesSeparatedFormats() {
        val text = "629073:431750\n630299 429076\n629377,431566\n629073-431750"
        val candidates = UtmParser.parseCandidates(text, 36, Hemisphere.NORTH, "3")

        assertEquals(3, candidates.size)
        assertEquals(629073.0, candidates[0].easting)
        assertEquals("431750", candidates[0].shortNorthing)
        assertEquals(3_431_750.0, candidates[0].fullNorthing)
        assertEquals(630299.0, candidates[1].easting)
        assertEquals(3_429_076.0, candidates[1].fullNorthing)
    }

    @Test
    fun parsesSplitLinesAndCompactTwelveDigits() {
        val text = "629073\n431750\n629377431566"
        val candidates = UtmParser.parseCandidates(text, 36, Hemisphere.NORTH, "3")

        assertEquals(2, candidates.size)
        assertEquals(629073.0, candidates[0].easting)
        assertEquals(3_431_750.0, candidates[0].fullNorthing)
        assertEquals(629377.0, candidates[1].easting)
        assertEquals(3_431_566.0, candidates[1].fullNorthing)
    }

    @Test
    fun buildsFullNorthingByConcatenatingPrefix() {
        assertEquals("3431750", UtmParser.buildFullNorthing("431750", "3"))
    }

    @Test
    fun rejectsInvalidEasting() {
        val candidates = UtmParser.parseCandidates("999999:431750", 36, Hemisphere.NORTH, "3")
        assertTrue(candidates.isEmpty())
    }
}
