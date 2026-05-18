package com.utm2maps.parser

import com.utm2maps.geo.Hemisphere
import com.utm2maps.geo.UtmCandidate

object UtmParser {
    private val separatedPairRegex = Regex("""(?<!\d)(\d{6})\s*[/\\|:,\-\s]+\s*(\d{6})(?!\d)""")
    private val compactPairRegex = Regex("""(?<!\d)(\d{12})(?!\d)""")

    fun parseCandidates(
        text: String,
        zone: Int,
        hemisphere: Hemisphere,
        northingPrefix: String
    ): List<UtmCandidate> {
        val cleanPrefix = northingPrefix.filter(Char::isDigit)
        if (cleanPrefix.isEmpty()) return emptyList()

        val matches = buildList {
            separatedPairRegex.findAll(text).forEach { match ->
                add(RawPair(match.value.trim(), match.groupValues[1], match.groupValues[2], match.range.first))
            }
            compactPairRegex.findAll(text).forEach { match ->
                val digits = match.groupValues[1]
                add(RawPair(match.value.trim(), digits.take(6), digits.takeLast(6), match.range.first))
            }
        }

        return matches
            .sortedBy { it.position }
            .distinctBy { it.eastingText to it.shortNorthing }
            .mapNotNull { raw -> raw.toCandidate(zone, hemisphere, cleanPrefix) }
    }

    fun buildFullNorthing(shortNorthing: String, northingPrefix: String): String =
        northingPrefix.filter(Char::isDigit) + shortNorthing

    private fun RawPair.toCandidate(zone: Int, hemisphere: Hemisphere, northingPrefix: String): UtmCandidate? {
        val easting = eastingText.toDoubleOrNull() ?: return null
        if (easting !in 100_000.0..900_000.0) return null
        if (!shortNorthing.matches(Regex("""\d{6}"""))) return null

        val fullNorthingText = buildFullNorthing(shortNorthing, northingPrefix)
        val fullNorthing = fullNorthingText.toDoubleOrNull() ?: return null
        if (fullNorthing !in 0.0..10_000_000.0) return null
        if (zone !in 1..60) return null

        return UtmCandidate(
            rawText = rawText,
            easting = easting,
            shortNorthing = shortNorthing,
            fullNorthing = fullNorthing,
            zone = zone,
            hemisphere = hemisphere
        )
    }

    private data class RawPair(
        val rawText: String,
        val eastingText: String,
        val shortNorthing: String,
        val position: Int
    )
}
