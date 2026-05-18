package com.utm2maps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
    onCandidateSelected: (Int) -> Unit,
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
        Text(strings.result, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.ocrText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(result?.recognizedText?.ifBlank { "—" } ?: "—", style = MaterialTheme.typography.bodyLarge)
        Text("Result", style = MaterialTheme.typography.headlineMedium)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("OCR text", style = MaterialTheme.typography.titleMedium)
                Text(result?.recognizedText?.ifBlank { "(empty)" } ?: "No result")
            }
        }

        if (result != null && result.candidates.size > 1) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.chooseCoordinate, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    result.candidates.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCandidateSelected(index) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = index == result.selectedIndex, onClick = { onCandidateSelected(index) })
                            Text(item.rawText, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }

        if (candidate != null && latLon != null && url != null) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.selectedUtm, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("${strings.latitude}: ${"%.6f".format(latLon.latitude)}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("${strings.longitude}: ${"%.6f".format(latLon.longitude)}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.selectedUtm, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${strings.rawInput}: ${candidate.rawText}")
                    Text("${strings.easting}: ${candidate.easting.toLong()}")
                    Text("${strings.fullNorthing}: ${candidate.fullNorthing.toLong()}")
                    Text("${strings.zone}: ${candidate.zone}${candidate.hemisphere.name.first()}")
                }
            }

            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.googleMapsLink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(url, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            Button(onClick = { onOpenMaps(url) }, modifier = Modifier.fillMaxWidth()) { Text(strings.openInGoogleMaps) }
            OutlinedButton(onClick = { onCopyLink(url) }, modifier = Modifier.fillMaxWidth()) { Text(strings.copyLink) }
            OutlinedButton(onClick = { onShareLink(url) }, modifier = Modifier.fillMaxWidth()) { Text(strings.shareLink) }
            HorizontalDivider()
        } else {
            Text(strings.noValidCoordinateFound, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
        }

        OutlinedButton(onClick = onScanAnother, modifier = Modifier.fillMaxWidth()) { Text(strings.scanAnotherImage) }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(strings.back) }
            Text("Choose Coordinate", style = MaterialTheme.typography.titleMedium)
            result.candidates.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCandidateSelected(index) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = index == result.selectedIndex, onClick = { onCandidateSelected(index) })
                    Text(item.rawText)
                }
            }
            HorizontalDivider()
        }

        if (candidate != null && latLon != null && url != null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Selected UTM", style = MaterialTheme.typography.titleMedium)
                    Text("Raw input: ${candidate.rawText}")
                    Text("Easting: ${candidate.easting.toLong()}")
                    Text("Full Northing: ${candidate.fullNorthing.toLong()}")
                    Text("Zone: ${candidate.zone}${candidate.hemisphere.name.first()}")
                    Text("Latitude: ${"%.6f".format(latLon.latitude)}")
                    Text("Longitude: ${"%.6f".format(latLon.longitude)}")
                    Text("Google Maps: $url")
                }
            }

            Button(onClick = { onOpenMaps(url) }, modifier = Modifier.fillMaxWidth()) { Text("Open in Google Maps") }
            OutlinedButton(onClick = { onCopyLink(url) }, modifier = Modifier.fillMaxWidth()) { Text("Copy Link") }
            OutlinedButton(onClick = { onShareLink(url) }, modifier = Modifier.fillMaxWidth()) { Text("Share Link") }
        } else {
            Text("לא נמצאה קואורדינטת UTM תקינה בתמונה", color = MaterialTheme.colorScheme.error)
        }

        OutlinedButton(onClick = onScanAnother, modifier = Modifier.fillMaxWidth()) { Text("Scan Another Image") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
