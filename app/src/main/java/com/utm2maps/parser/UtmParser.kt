package com.utm2maps.parser

import com.utm2maps.geo.Hemisphere
import com.utm2maps.geo.UtmCandidate

object UtmParser {
    private val fullUtmRegex = Regex(
        pattern = """(?<!\d)([1-5]?\d|60)\s*([C-HJ-NP-Xc-hj-np-x])\s*(\d{6,7})\s*[/\\|:,\-\s]+\s*(\d{7})(?!\d)"""
    )
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
            fullUtmRegex.findAll(text).forEach { match ->
                add(
                    RawMatch.FullUtm(
                        rawText = match.value.trim(),
                        zone = match.groupValues[1].toInt(),
                        latitudeBand = match.groupValues[2].uppercase(),
                        eastingText = match.groupValues[3],
                        northingText = match.groupValues[4],
                        position = match.range.first
                    )
                )
            }
            separatedPairRegex.findAll(text).forEach { match ->
                add(
                    RawMatch.ShortUtm(
                        rawText = match.value.trim(),
                        eastingText = match.groupValues[1],
                        shortNorthing = match.groupValues[2],
                        position = match.range.first
                    )
                )
            }
            compactPairRegex.findAll(text).forEach { match ->
                val digits = match.groupValues[1]
                add(
                    RawMatch.ShortUtm(
                        rawText = match.value.trim(),
                        eastingText = digits.take(6),
                        shortNorthing = digits.takeLast(6),
                        position = match.range.first
                    )
                )
            }
        }

        return matches
            .sortedBy { it.position }
            .distinctBy {
                when (it) {
                    is RawMatch.FullUtm -> "F:${it.zone}:${it.latitudeBand}:${it.eastingText}:${it.northingText}"
                    is RawMatch.ShortUtm -> "S:${it.eastingText}:${it.shortNorthing}"
                }
            }
            .mapNotNull { raw ->
                when (raw) {
                    is RawMatch.FullUtm -> raw.toFullCandidate()
                    is RawMatch.ShortUtm -> raw.toShortCandidate(zone, hemisphere, cleanPrefix)
                }
            }
    }

    fun buildFullNorthing(shortNorthing: String, northingPrefix: String): String =
        northingPrefix.filter(Char::isDigit) + shortNorthing

    private fun RawMatch.ShortUtm.toShortCandidate(
        zone: Int,
        hemisphere: Hemisphere,
        northingPrefix: String
    ): UtmCandidate? {
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

    private fun RawMatch.FullUtm.toFullCandidate(): UtmCandidate? {
        val easting = eastingText.toDoubleOrNull() ?: return null
        val fullNorthing = northingText.toDoubleOrNull() ?: return null
        if (zone !in 1..60) return null
        if (easting !in 100_000.0..900_000.0) return null
        if (fullNorthing !in 0.0..10_000_000.0) return null

        val hemisphere = latitudeBandToHemisphere(latitudeBand) ?: return null

        return UtmCandidate(
            rawText = rawText,
            easting = easting,
            shortNorthing = northingText.takeLast(6),
            fullNorthing = fullNorthing,
            zone = zone,
            hemisphere = hemisphere,
            latitudeBand = latitudeBand
        )
    }

    private fun latitudeBandToHemisphere(band: String): Hemisphere? {
        val c = band.uppercase().firstOrNull() ?: return null
        return when (c) {
            in 'N'..'X' -> Hemisphere.NORTH
            in 'C'..'M' -> Hemisphere.SOUTH
            else -> null
        }
    }

    private sealed interface RawMatch { val position: Int
        data class ShortUtm(
            val rawText: String,
            val eastingText: String,
            val shortNorthing: String,
            override val position: Int
        ) : RawMatch

        data class FullUtm(
            val rawText: String,
            val zone: Int,
            val latitudeBand: String,
            val eastingText: String,
            val northingText: String,
            override val position: Int
        ) : RawMatch
    }
}
