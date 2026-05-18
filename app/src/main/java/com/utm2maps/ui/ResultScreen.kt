package com.utm2maps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.utm2maps.ScanResult
import com.utm2maps.geo.buildGoogleMapsUrl

@Composable
fun ResultScreen(
    strings: UiStrings,
    result: ScanResult?,
    errorMessage: String?,
    onCandidateSelected: (Int) -> Unit,
    onCopyRecognizedText: (String) -> Unit,
    onReparseRecognizedText: (String) -> Unit,
    onOpenMaps: (String) -> Unit,
    onCopyLink: (String) -> Unit,
    onShareLink: (String) -> Unit,
    onScanAnother: () -> Unit,
    onBack: () -> Unit
) {
    val candidate = result?.selectedCandidate
    val latLon = result?.selectedLatLon
    val url = latLon?.let(::buildGoogleMapsUrl)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = strings.result,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        val recognizedText = result?.recognizedText.orEmpty()
        OcrTextCard(
            strings = strings,
            text = recognizedText,
            onCopyRecognizedText = onCopyRecognizedText,
            onReparseRecognizedText = onReparseRecognizedText
        )

        errorMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f))
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        if (result != null && result.candidates.size > 1) {
            CandidatePickerCard(
                strings = strings,
                result = result,
                onCandidateSelected = onCandidateSelected
            )
        }

        if (candidate != null && latLon != null && url != null) {
            LatLonCard(strings = strings, latitude = latLon.latitude, longitude = latLon.longitude)
            UtmDetailsCard(strings = strings, rawText = candidate.rawText, easting = candidate.easting.toLong(), fullNorthing = candidate.fullNorthing.toLong(), zoneText = "${candidate.zone}${candidate.hemisphere.name.first()}")
            MapsLinkCard(strings = strings, url = url)

            Button(onClick = { onOpenMaps(url) }, modifier = Modifier.fillMaxWidth()) {
                Text(strings.openInGoogleMaps)
            }
            OutlinedButton(onClick = { onCopyLink(url) }, modifier = Modifier.fillMaxWidth()) {
                Text(strings.copyLink)
            }
            OutlinedButton(onClick = { onShareLink(url) }, modifier = Modifier.fillMaxWidth()) {
                Text(strings.shareLink)
            }
            HorizontalDivider()
        } else {
            Text(
                text = strings.noValidCoordinateFound,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        OutlinedButton(onClick = onScanAnother, modifier = Modifier.fillMaxWidth()) {
            Text(strings.scanAnotherImage)
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(strings.back)
        }
    }
}

@Composable
private fun OcrTextCard(
    strings: UiStrings,
    text: String,
    onCopyRecognizedText: (String) -> Unit,
    onReparseRecognizedText: (String) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = strings.ocrText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                Text(text.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge)
            }
            OutlinedButton(
                onClick = { onCopyRecognizedText(text) },
                modifier = Modifier.fillMaxWidth(),
                enabled = text.isNotBlank()
            ) {
                Text(strings.copyRecognizedText)
            }
            Button(
                onClick = { onReparseRecognizedText(text) },
                modifier = Modifier.fillMaxWidth(),
                enabled = text.isNotBlank()
            ) {
                Text(strings.extractCoordinateFromThisText)
            }
        }
    }
}

@Composable
private fun CandidatePickerCard(
    strings: UiStrings,
    result: ScanResult,
    onCandidateSelected: (Int) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = strings.chooseCoordinate,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            result.candidates.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCandidateSelected(index) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = index == result.selectedIndex,
                        onClick = { onCandidateSelected(index) }
                    )
                    Text(item.rawText, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun LatLonCard(strings: UiStrings, latitude: Double, longitude: Double) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = strings.selectedUtm,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${strings.latitude}: ${"%.6f".format(latitude)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${strings.longitude}: ${"%.6f".format(longitude)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun UtmDetailsCard(
    strings: UiStrings,
    rawText: String,
    easting: Long,
    fullNorthing: Long,
    zoneText: String
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = strings.selectedUtm,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text("${strings.rawInput}: $rawText")
            Text("${strings.easting}: $easting")
            Text("${strings.fullNorthing}: $fullNorthing")
            Text("${strings.zone}: $zoneText")
        }
    }
}

@Composable
private fun MapsLinkCard(strings: UiStrings, url: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = strings.googleMapsLink,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = url,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
