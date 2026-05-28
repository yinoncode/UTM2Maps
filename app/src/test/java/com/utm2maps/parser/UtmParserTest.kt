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
    fun parsesFullUtmZoneBandAcrossLines() {
        val candidates = UtmParser.parseCandidates("36S 0691523\n3608074", 36, Hemisphere.NORTH, "3")

        assertEquals(1, candidates.size)
        assertEquals(36, candidates[0].zone)
        assertEquals("S", candidates[0].latitudeBand)
        assertEquals(Hemisphere.NORTH, candidates[0].hemisphere)
        assertEquals(691523.0, candidates[0].easting)
        assertEquals(3_608_074.0, candidates[0].fullNorthing)
    }

    @Test
    fun parsesFullUtmZoneBandSameLine() {
        val candidates = UtmParser.parseCandidates("36S 0691523 3608074", 1, Hemisphere.SOUTH, "9")

        assertEquals(1, candidates.size)
        assertEquals(36, candidates[0].zone)
        assertEquals("S", candidates[0].latitudeBand)
        assertEquals(Hemisphere.NORTH, candidates[0].hemisphere)
        assertEquals(691523.0, candidates[0].easting)
        assertEquals(3_608_074.0, candidates[0].fullNorthing)
    }

    @Test
    fun parsesFullUtmWithSpaceBetweenZoneAndBand() {
        val candidates = UtmParser.parseCandidates("36 R 0625854 3439328", 1, Hemisphere.SOUTH, "9")

        assertEquals(1, candidates.size)
        assertEquals(36, candidates[0].zone)
        assertEquals("R", candidates[0].latitudeBand)
        assertEquals(Hemisphere.NORTH, candidates[0].hemisphere)
        assertEquals(625854.0, candidates[0].easting)
        assertEquals(3_439_328.0, candidates[0].fullNorthing)
    }

    @Test
    fun parsesSlashSeparatedCoordinateInsideHebrewText() {
        val candidates = UtmParser.parseCandidates("נצ הנחתה 625854/439328", 36, Hemisphere.NORTH, "3")

        assertEquals(1, candidates.size)
        assertEquals(625854.0, candidates[0].easting)
        assertEquals("439328", candidates[0].shortNorthing)
        assertEquals(3_439_328.0, candidates[0].fullNorthing)
    }

    @Test
    fun parsesAllRequestedSeparatorFormats() {
        val inputs = listOf(
            "625854/439328",
            "625854:439328",
            "625854 439328",
            "625854,439328",
            "625854-439328",
            "625854\\439328",
            "625854 | 439328",
            "625854\n439328",
            "625854439328"
        )

        inputs.forEach { input ->
            val candidates = UtmParser.parseCandidates(input, 36, Hemisphere.NORTH, "3")
            assertEquals(1, candidates.size, "Expected one candidate for input: $input")
            assertEquals(625854.0, candidates[0].easting)
            assertEquals("439328", candidates[0].shortNorthing)
            assertEquals(3_439_328.0, candidates[0].fullNorthing)
        }
    }

    @Test
    fun parsesFullDecimalPair() {
        val candidates = UtmParser.parseCandidates("691296.452 3609272.95", 36, Hemisphere.NORTH, "3")
        assertEquals(1, candidates.size)
        assertEquals(691296.452, candidates[0].easting)
        assertEquals(3_609_272.95, candidates[0].fullNorthing)
    }

    @Test
    fun parsesLabeledDecimalPairWithSpaces() {
        val candidates = UtmParser.parseCandidates("E 691296.452 N 3609272.95", 36, Hemisphere.NORTH, "3")
        assertEquals(1, candidates.size)
        assertEquals(691296.452, candidates[0].easting)
        assertEquals(3_609_272.95, candidates[0].fullNorthing)
    }

    @Test
    fun parsesLabeledDecimalPairWithEquals() {
        val candidates = UtmParser.parseCandidates("E=691296.452 N=3609272.95", 36, Hemisphere.NORTH, "3")
        assertEquals(1, candidates.size)
        assertEquals(691296.452, candidates[0].easting)
        assertEquals(3_609_272.95, candidates[0].fullNorthing)
    }


    @Test
    fun parsesAdjacentSixDigitLinesFromScreenshot() {
        val candidates = UtmParser.parseCandidates("627144\n437671", 36, Hemisphere.NORTH, "3")

        assertEquals(1, candidates.size)
        assertEquals(627144.0, candidates[0].easting)
        assertEquals("437671", candidates[0].shortNorthing)
        assertEquals(3_437_671.0, candidates[0].fullNorthing)
    }

    @Test
    fun parsesAdjacentSixDigitLinesInsideWhatsAppText() {
        val candidates = UtmParser.parseCandidates("some WhatsApp text\n627144\n437671\nmore text", 36, Hemisphere.NORTH, "3")

        assertEquals(1, candidates.size)
        assertEquals(627144.0, candidates[0].easting)
        assertEquals("437671", candidates[0].shortNorthing)
        assertEquals(3_437_671.0, candidates[0].fullNorthing)
    }

    @Test
    fun parsesAdjacentLinesWithOcrSpacesInsideNumbers() {
        val candidates = UtmParser.parseCandidates("627 144\n437 671", 36, Hemisphere.NORTH, "3")

        assertEquals(1, candidates.size)
        assertEquals(627144.0, candidates[0].easting)
        assertEquals("437671", candidates[0].shortNorthing)
        assertEquals(3_437_671.0, candidates[0].fullNorthing)
    }

    @Test
    fun parsesAdjacentLinesWithDifferentOcrSplitsInsideNumbers() {
        val candidates = UtmParser.parseCandidates("6271 44\n4376 71", 36, Hemisphere.NORTH, "3")

        assertEquals(1, candidates.size)
        assertEquals(627144.0, candidates[0].easting)
        assertEquals("437671", candidates[0].shortNorthing)
        assertEquals(3_437_671.0, candidates[0].fullNorthing)
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
