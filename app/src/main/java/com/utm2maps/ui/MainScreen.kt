package com.utm2maps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.utm2maps.ScanResult

@Composable
fun MainScreen(
    lastResult: ScanResult?,
    isScanning: Boolean,
    errorMessage: String?,
    onChooseImage: () -> Unit,
    onTakeImage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLastResult: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("UTM2Maps", style = MaterialTheme.typography.headlineLarge)
        Text("זהה קואורדינטות UTM מתמונה ופתח בגוגל מפות")

        Button(onClick = onChooseImage, modifier = Modifier.fillMaxWidth(), enabled = !isScanning) {
            Text("Choose Image")
        }
        OutlinedButton(onClick = onTakeImage, modifier = Modifier.fillMaxWidth(), enabled = !isScanning) {
            Text("Take Screenshot/Image if possible")
        }
        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
            Text("Settings")
        }

        if (isScanning) {
            CircularProgressIndicator()
            Text("Scanning image with ML Kit OCR...")
        }

        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Last Result", style = MaterialTheme.typography.titleMedium)
                val candidate = lastResult?.selectedCandidate
                val latLon = lastResult?.selectedLatLon
                if (candidate == null || latLon == null) {
                    Text("No scan yet")
                } else {
                    Text("${candidate.rawText} → ${"%.6f".format(latLon.latitude)}, ${"%.6f".format(latLon.longitude)}")
                    Button(onClick = onOpenLastResult) { Text("Open Result") }
                }
            }
        }
    }
}
