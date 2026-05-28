package com.utm2maps.parser

import com.utm2maps.geo.Hemisphere
import com.utm2maps.geo.UtmCandidate

object UtmParser {
    private val fullUtmRegex = Regex(
        pattern = """(?<!\d)([1-5]?\d|60)\s*([C-HJ-NP-Xc-hj-np-x])\s*(\d{6,7})\s*[/\\|:,\-\s]+\s*(\d{7})(?!\d)"""
    )
    private val fullDecimalPairRegex = Regex(
        pattern = """(?<!\d)(\d{6}(?:\.\d+)?)\s*[/\\|:,\-\s]+\s*(\d{7}(?:\.\d+)?)(?!\d)"""
    )
    private val labeledDecimalPairRegex = Regex(
        pattern = """(?i)\bE\s*[:=]?\s*(\d{6}(?:\.\d+)?)\s*[,;/\\|:\-\s]*\bN\s*[:=]?\s*(\d{7}(?:\.\d+)?)\b"""
    )
    private val separatedPairRegex = Regex("""(?<!\d)(\d{6})\s*[/\\|:,\-\s]+\s*(\d{6})(?!\d)""")
    private val compactPairRegex = Regex("""(?<!\d)(\d{12})(?!\d)""")

    fun parseCandidates(text: String, zone: Int, hemisphere: Hemisphere, northingPrefix: String): List<UtmCandidate> {
        val cleanPrefix = northingPrefix.filter(Char::isDigit)
        if (cleanPrefix.isEmpty()) return emptyList()

        val matches = buildList {
            fullUtmRegex.findAll(text).forEach { m ->
                add(RawMatch.FullUtm(m.value.trim(), m.groupValues[1].toInt(), m.groupValues[2].uppercase(), m.groupValues[3], m.groupValues[4], m.range.first))
            }
            labeledDecimalPairRegex.findAll(text).forEach { m ->
                add(RawMatch.FullNumeric(m.value.trim(), m.groupValues[1], m.groupValues[2], m.range.first))
            }
            fullDecimalPairRegex.findAll(text).forEach { m ->
                add(RawMatch.FullNumeric(m.value.trim(), m.groupValues[1], m.groupValues[2], m.range.first))
            }
            adjacentSixDigitLinePairs(text).forEach { raw -> add(raw) }
            separatedPairRegex.findAll(text).forEach { m ->
                add(RawMatch.ShortUtm(m.value.trim(), m.groupValues[1], m.groupValues[2], m.range.first))
            }
            compactPairRegex.findAll(text).forEach { m ->
                val digits = m.groupValues[1]
                add(RawMatch.ShortUtm(m.value.trim(), digits.take(6), digits.takeLast(6), m.range.first))
            }
        }

        return matches.sortedBy { it.position }
            .distinctBy {
                when (it) {
                    is RawMatch.FullUtm -> "F:${it.zone}:${it.latitudeBand}:${it.eastingText}:${it.northingText}"
                    is RawMatch.FullNumeric -> "N:${it.eastingText}:${it.fullNorthingText}"
                    is RawMatch.ShortUtm -> "S:${it.eastingText}:${it.shortNorthing}"
                }
            }
            .mapNotNull {
                when (it) {
                    is RawMatch.FullUtm -> it.toFullCandidate()
                    is RawMatch.FullNumeric -> it.toFullNumericCandidate(zone, hemisphere)
                    is RawMatch.ShortUtm -> it.toShortCandidate(zone, hemisphere, cleanPrefix)
                }
            }
    }


    private fun adjacentSixDigitLinePairs(text: String): List<RawMatch.ShortUtm> {
        val lines = buildList {
            var start = 0
            text.splitToSequence('\n').forEach { line ->
                val digitsOnly = line.filter(Char::isDigit)
                add(OcrLine(line.trim(), digitsOnly, start))
                start += line.length + 1
            }
        }

        return lines.zipWithNext()
            .filter { (first, second) -> first.digitsOnly.length == 6 && second.digitsOnly.length == 6 }
            .map { (first, second) ->
                RawMatch.ShortUtm(
                    rawText = listOf(first.rawText, second.rawText).filter(String::isNotBlank).joinToString("\n"),
                    eastingText = first.digitsOnly,
                    shortNorthing = second.digitsOnly,
                    position = first.position
                )
            }
    }

    fun buildFullNorthing(shortNorthing: String, northingPrefix: String): String = northingPrefix.filter(Char::isDigit) + shortNorthing

    private fun RawMatch.ShortUtm.toShortCandidate(zone: Int, hemisphere: Hemisphere, northingPrefix: String): UtmCandidate? {
        val easting = eastingText.toDoubleOrNull() ?: return null
        if (easting !in 100_000.0..900_000.0 || !shortNorthing.matches(Regex("""\d{6}""")) || zone !in 1..60) return null
        val fullNorthing = buildFullNorthing(shortNorthing, northingPrefix).toDoubleOrNull() ?: return null
        if (fullNorthing !in 0.0..10_000_000.0) return null
        return UtmCandidate(rawText, easting, shortNorthing, fullNorthing, zone, hemisphere)
    }

    private fun RawMatch.FullNumeric.toFullNumericCandidate(zone: Int, hemisphere: Hemisphere): UtmCandidate? {
        val easting = eastingText.toDoubleOrNull() ?: return null
        val fullNorthing = fullNorthingText.toDoubleOrNull() ?: return null
        if (zone !in 1..60 || easting !in 100_000.0..900_000.0 || fullNorthing !in 0.0..10_000_000.0) return null
        return UtmCandidate(rawText, easting, fullNorthingText.takeLast(6).filter { it.isDigit() }, fullNorthing, zone, hemisphere)
    }

    private fun RawMatch.FullUtm.toFullCandidate(): UtmCandidate? {
        val easting = eastingText.toDoubleOrNull() ?: return null
        val fullNorthing = northingText.toDoubleOrNull() ?: return null
        if (zone !in 1..60 || easting !in 100_000.0..900_000.0 || fullNorthing !in 0.0..10_000_000.0) return null
        val inferredHemisphere = latitudeBandToHemisphere(latitudeBand) ?: return null
        return UtmCandidate(rawText, easting, northingText.takeLast(6), fullNorthing, zone, inferredHemisphere, latitudeBand)
    }

    private fun latitudeBandToHemisphere(band: String): Hemisphere? = when (band.uppercase().firstOrNull()) {
        in 'N'..'X' -> Hemisphere.NORTH
        in 'C'..'M' -> Hemisphere.SOUTH
        else -> null
    }

    private data class OcrLine(val rawText: String, val digitsOnly: String, val position: Int)

    private sealed interface RawMatch {
        val position: Int
        data class ShortUtm(val rawText: String, val eastingText: String, val shortNorthing: String, override val position: Int) : RawMatch
        data class FullNumeric(val rawText: String, val eastingText: String, val fullNorthingText: String, override val position: Int) : RawMatch
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
