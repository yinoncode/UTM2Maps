package com.utm2maps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.utm2maps.R
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(Modifier.fillMaxWidth()) {
            HeroCard(strings)
            IconButton(onClick = onOpenSettings, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(Icons.Default.Settings, contentDescription = strings.settings)
            }
        }
        ActionButtons(strings, isScanning, onChooseImage, onTakeImage)
        ManualTextCard(strings, manualText, onManualTextChange, onExtractManualText)
        if (isScanning) ScanningCard(strings)
        errorMessage?.let { ErrorCard(it) }
        LastResultCard(strings, lastResult, onOpenLastResult)
        HistoryCard(strings, history, onOpenHistoryItem, onDeleteHistoryItem, onUpdateHistoryTitle, onCopyHistoryLink, onShareHistoryLink, onOpenHistoryInMaps)
        TipCard(strings)
    }
}

@Composable
private fun HeroCard(strings: UiStrings) = ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
) {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(90.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
            Image(painter = painterResource(id = R.drawable.utm2maps_logo), contentDescription = strings.appName, modifier = Modifier.size(78.dp))
        }
        Text(strings.appName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(strings.appTagline, textAlign = TextAlign.Center)
        Text(strings.heroHelper, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActionButtons(strings: UiStrings, isScanning: Boolean, onChooseImage: () -> Unit, onTakeImage: () -> Unit) {
    Button(onClick = onChooseImage, modifier = Modifier.fillMaxWidth().height(54.dp), enabled = !isScanning) {
        Icon(Icons.Default.Image, null); Text("  ${strings.chooseImage}")
    }
    OutlinedButton(onClick = onTakeImage, modifier = Modifier.fillMaxWidth().height(54.dp), enabled = !isScanning) {
        Icon(Icons.Default.CameraAlt, null); Text("  ${strings.takeImage}")
    }
    OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(54.dp), enabled = false) {
        Icon(Icons.Default.TextSnippet, null); Text("  ${strings.pasteTextManually}")
    }
}

@Composable
private fun ManualTextCard(strings: UiStrings, manualText: String, onManualTextChange: (String) -> Unit, onExtractManualText: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(strings.manualTextTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(strings.manualTextHelper, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = manualText,
                onValueChange = onManualTextChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                placeholder = { Text(strings.manualTextPlaceholderSimple) }
            )
            Button(onClick = onExtractManualText, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.TextSnippet, null); Text("  ${strings.extractCoordinateFromText}")
            }
        }
    }
}

@Composable
private fun LastResultCard(strings: UiStrings, lastResult: ScanResult?, onOpenLastResult: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(strings.lastResult, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            val candidate = lastResult?.selectedCandidate
            val latLon = lastResult?.selectedLatLon
            if (candidate == null || latLon == null) {
                Text(strings.lastResultEmptyTitle)
                Text(strings.lastResultEmptySubtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(candidate.rawText)
                Text("${"%.6f".format(latLon.latitude)}, ${"%.6f".format(latLon.longitude)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Button(onClick = onOpenLastResult, modifier = Modifier.fillMaxWidth()) { Text(strings.openResult) }
            }
        }
    }
}

@Composable
private fun HistoryCard(strings: UiStrings, history: List<CoordinateHistoryItem>, onOpen: (CoordinateHistoryItem)->Unit, onDelete:(CoordinateHistoryItem)->Unit, onUpdateTitle:(CoordinateHistoryItem,String)->Unit, onCopy:(CoordinateHistoryItem)->Unit, onShare:(CoordinateHistoryItem)->Unit, onOpenMaps:(CoordinateHistoryItem)->Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(strings.lastFiveCoordinates, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (history.isEmpty()) Text(strings.noHistoryYet, color = MaterialTheme.colorScheme.onSurfaceVariant)
            history.forEach { item ->
                val edit = remember(item.id) { mutableStateOf(item.title) }
                Card(Modifier.fillMaxWidth().clickable { onOpen(item) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.title, fontWeight = FontWeight.Bold)
                        Text(item.rawText, maxLines = 1)
                        Text("${"%.6f".format(item.latitude)}, ${"%.6f".format(item.longitude)}", style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(value = edit.value, onValueChange = { edit.value = it }, label = { Text(strings.editTitle) }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AssistChip(onClick = { onOpenMaps(item) }, label = { Text(strings.open) }, leadingIcon = { Icon(Icons.Default.OpenInNew, null) })
                            AssistChip(onClick = { onCopy(item) }, label = { Text(strings.copy) }, leadingIcon = { Icon(Icons.Default.ContentCopy, null) })
                            AssistChip(onClick = { onShare(item) }, label = { Text(strings.share) }, leadingIcon = { Icon(Icons.Default.Share, null) })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AssistChip(onClick = { onUpdateTitle(item, edit.value) }, label = { Text(strings.editTitle) }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                            AssistChip(onClick = { onDelete(item) }, label = { Text(strings.delete) }, leadingIcon = { Icon(Icons.Default.Delete, null) })
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun ScanningCard(strings: UiStrings) { ElevatedCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(22.dp)); Text("  ${strings.scanningImage}") } } }
@Composable private fun ErrorCard(message: String) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f))) { Text(message, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error) } }
@Composable private fun TipCard(strings: UiStrings) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) { Text(strings.tipText, Modifier.padding(12.dp)) } }
