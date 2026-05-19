package com.utm2maps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.utm2maps.ScanResult

@Composable
fun MainScreen(
    strings: UiStrings,
    lastResult: ScanResult?,
    isScanning: Boolean,
    errorMessage: String?,
    manualText: String,
    onManualTextChange: (String) -> Unit,
    onExtractManualText: () -> Unit,
    onChooseImage: () -> Unit,
    onTakeImage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLastResult: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderCard(strings = strings)

        Button(
            onClick = onChooseImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = !isScanning
        ) {
            Text(strings.chooseImage, style = MaterialTheme.typography.titleMedium)
        }

        OutlinedButton(
            onClick = onTakeImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = !isScanning
        ) {
            Text(strings.takeImage, style = MaterialTheme.typography.titleMedium)
        }

        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(strings.settings, style = MaterialTheme.typography.titleMedium)
        }

        ManualTextCard(
            strings = strings,
            manualText = manualText,
            onManualTextChange = onManualTextChange,
            onExtractManualText = onExtractManualText
        )

        if (isScanning) {
            ScanningCard(strings = strings)
        }

        errorMessage?.let { message ->
            ErrorCard(message = message)
        }

        Spacer(Modifier.height(4.dp))
        LastResultCard(
            strings = strings,
            lastResult = lastResult,
            onOpenLastResult = onOpenLastResult
        )
    }
}

@Composable
private fun HeaderCard(strings: UiStrings) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.logoText,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = strings.appTagline,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ManualTextCard(
    strings: UiStrings,
    manualText: String,
    onManualTextChange: (String) -> Unit,
    onExtractManualText: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = strings.manualTextTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = manualText,
                onValueChange = onManualTextChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                label = { Text(strings.pasteTextManually) },
                placeholder = { Text(strings.manualTextPlaceholder) }
            )
            Button(onClick = onExtractManualText, modifier = Modifier.fillMaxWidth()) {
                Text(strings.extractCoordinateFromText)
            }
        }
    }
}

@Composable
private fun ScanningCard(strings: UiStrings) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(30.dp))
            Text(strings.scanningImage, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
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

@Composable
private fun LastResultCard(
    strings: UiStrings,
    lastResult: ScanResult?,
    onOpenLastResult: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = strings.lastResult,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            val candidate = lastResult?.selectedCandidate
            val latLon = lastResult?.selectedLatLon
            if (candidate == null || latLon == null) {
                Text(
                    text = strings.noScanYet,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(candidate.rawText, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${"%.6f".format(latLon.latitude)}, ${"%.6f".format(latLon.longitude)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = onOpenLastResult, modifier = Modifier.fillMaxWidth()) {
                    Text(strings.openResult)
                }
            }
        }
    }
}
