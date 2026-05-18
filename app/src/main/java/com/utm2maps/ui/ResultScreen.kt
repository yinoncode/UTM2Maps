package com.utm2maps.ui

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.utm2maps.ScanResult
import com.utm2maps.geo.buildGoogleMapsUrl

@Composable
fun ResultScreen(
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
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Result", style = MaterialTheme.typography.headlineMedium)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("OCR text", style = MaterialTheme.typography.titleMedium)
                Text(result?.recognizedText?.ifBlank { "(empty)" } ?: "No result")
            }
        }

        if (result != null && result.candidates.size > 1) {
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
