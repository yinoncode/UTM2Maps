package com.utm2maps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.utm2maps.ScanResult
import com.utm2maps.data.CoordinateHistoryItem

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
    onOpenLastResult: () -> Unit,
    history: List<CoordinateHistoryItem>,
    onOpenHistoryItem: (CoordinateHistoryItem) -> Unit,
    onDeleteHistoryItem: (CoordinateHistoryItem) -> Unit,
    onUpdateHistoryTitle: (CoordinateHistoryItem, String) -> Unit,
    onCopyHistoryLink: (CoordinateHistoryItem) -> Unit,
    onShareHistoryLink: (CoordinateHistoryItem) -> Unit,
    onOpenHistoryInMaps: (CoordinateHistoryItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        HeaderCard(strings)
        Button(onClick = onChooseImage, modifier = Modifier.fillMaxWidth().height(54.dp), enabled = !isScanning) { Text(strings.chooseImage) }
        OutlinedButton(onClick = onTakeImage, modifier = Modifier.fillMaxWidth().height(54.dp), enabled = !isScanning) { Text(strings.takeImage) }
        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text(strings.settings) }
        ManualTextCard(strings, manualText, onManualTextChange, onExtractManualText)
        if (isScanning) ScanningCard(strings)
        errorMessage?.let { ErrorCard(it) }
        LastResultCard(strings, lastResult, onOpenLastResult)
        HistoryCard(strings, history, onOpenHistoryItem, onDeleteHistoryItem, onUpdateHistoryTitle, onCopyHistoryLink, onShareHistoryLink, onOpenHistoryInMaps)
    }
}

@Composable private fun HistoryCard(strings: UiStrings, history: List<CoordinateHistoryItem>, onOpen: (CoordinateHistoryItem)->Unit, onDelete:(CoordinateHistoryItem)->Unit, onUpdateTitle:(CoordinateHistoryItem,String)->Unit, onCopy:(CoordinateHistoryItem)->Unit, onShare:(CoordinateHistoryItem)->Unit, onOpenMaps:(CoordinateHistoryItem)->Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(strings.lastFiveCoordinates, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            if (history.isEmpty()) Text(strings.noHistoryYet, color = MaterialTheme.colorScheme.onSurfaceVariant) else history.forEach { item ->
                val edit = remember(item.id) { mutableStateOf(item.title) }
                Card(Modifier.fillMaxWidth().clickable { onOpen(item) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(item.title, fontWeight = FontWeight.Bold)
                        Text(item.rawText)
                        Text("${"%.6f".format(item.latitude)}, ${"%.6f".format(item.longitude)}")
                        OutlinedTextField(value = edit.value, onValueChange = { edit.value = it }, label = { Text(strings.editTitle) }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onUpdateTitle(item, edit.value) }) { Text(strings.save) }
                            OutlinedButton(onClick = { onOpenMaps(item) }) { Text(strings.openInMaps) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onCopy(item) }) { Text(strings.copy) }
                            OutlinedButton(onClick = { onShare(item) }) { Text(strings.share) }
                            OutlinedButton(onClick = { onDelete(item) }) { Text(strings.delete) }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun HeaderCard(strings: UiStrings) { ElevatedCard(Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) { Box(Modifier.size(58.dp).background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) { Text(strings.logoText, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }; Text(strings.appName, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center); Text(strings.appTagline, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center) } } }
@Composable private fun ManualTextCard(strings: UiStrings, manualText: String, onManualTextChange: (String) -> Unit, onExtractManualText: () -> Unit) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(strings.manualTextTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); OutlinedTextField(value = manualText, onValueChange = onManualTextChange, modifier = Modifier.fillMaxWidth(), minLines = 4, label = { Text(strings.pasteTextManually) }, placeholder = { Text(strings.manualTextPlaceholder) }); Button(onClick = onExtractManualText, modifier = Modifier.fillMaxWidth()) { Text(strings.extractCoordinateFromText) } } } }
@Composable private fun ScanningCard(strings: UiStrings) { ElevatedCard(Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) { CircularProgressIndicator(Modifier.size(30.dp)); Text(strings.scanningImage) } } }
@Composable private fun ErrorCard(message: String) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f))) { Text(message, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error) } }
@Composable private fun LastResultCard(strings: UiStrings, lastResult: ScanResult?, onOpenLastResult: () -> Unit) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(strings.lastResult, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); val candidate = lastResult?.selectedCandidate; val latLon = lastResult?.selectedLatLon; if (candidate == null || latLon == null) Text(strings.noScanYet, color = MaterialTheme.colorScheme.onSurfaceVariant) else { Text(candidate.rawText); Text("${"%.6f".format(latLon.latitude)}, ${"%.6f".format(latLon.longitude)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold); Button(onClick = onOpenLastResult, modifier = Modifier.fillMaxWidth()) { Text(strings.openResult) } } } } }
