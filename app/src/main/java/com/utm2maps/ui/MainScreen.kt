package com.utm2maps.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AssistChip
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TopRow(strings, onOpenSettings)
        HeroCard(strings)
        ActionButtons(strings, isScanning, onChooseImage, onTakeImage)
        if (isScanning) ScanningCard(strings)
        errorMessage?.let { ErrorCard(it) }
        LastResultCard(strings, lastResult, onOpenLastResult)
        ManualTextCard(strings, manualText, onManualTextChange, onExtractManualText)
        HistoryCard(strings, history, onOpenHistoryItem, onDeleteHistoryItem, onUpdateHistoryTitle, onCopyHistoryLink, onShareHistoryLink, onOpenHistoryInMaps)
        TipCard(strings)
    }
}

@Composable
private fun TopRow(strings: UiStrings, onOpenSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(strings.appName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = onOpenSettings) { Text(strings.settings) }
    }
}

@Composable
private fun HeroCard(strings: UiStrings) = ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
) {
    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Image(
            painter = painterResource(id = R.drawable.utm2maps_logo),
            contentDescription = strings.appName,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(strings.appName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(strings.appTagline, style = MaterialTheme.typography.bodyMedium)
            Text(strings.heroHelper, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActionButtons(strings: UiStrings, isScanning: Boolean, onChooseImage: () -> Unit, onTakeImage: () -> Unit) {
    Button(onClick = onChooseImage, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !isScanning) { Text(strings.chooseImage) }
    OutlinedButton(onClick = onTakeImage, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !isScanning) { Text(strings.takeImage) }
}

@Composable
private fun LastResultCard(strings: UiStrings, lastResult: ScanResult?, onOpenLastResult: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(strings.lastResult, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            val candidate = lastResult?.selectedCandidate
            val latLon = lastResult?.selectedLatLon
            if (candidate == null || latLon == null) {
                Text(strings.lastResultEmptyTitle)
                Text(strings.lastResultEmptySubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(candidate.rawText, style = MaterialTheme.typography.bodyMedium)
                Text("${"%.6f".format(latLon.latitude)}, ${"%.6f".format(latLon.longitude)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onOpenLastResult, modifier = Modifier.fillMaxWidth()) { Text(strings.openResult) }
            }
        }
    }
}

@Composable
private fun ManualTextCard(strings: UiStrings, manualText: String, onManualTextChange: (String) -> Unit, onExtractManualText: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(strings.manualTextTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(strings.manualTextHelper, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(value = manualText, onValueChange = onManualTextChange, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 4, placeholder = { Text(strings.manualTextPlaceholderSimple) })
            Button(onClick = onExtractManualText, modifier = Modifier.fillMaxWidth()) { Text(strings.extractCoordinateFromText) }
        }
    }
}

@Composable
private fun HistoryCard(strings: UiStrings, history: List<CoordinateHistoryItem>, onOpen: (CoordinateHistoryItem)->Unit, onDelete:(CoordinateHistoryItem)->Unit, onUpdateTitle:(CoordinateHistoryItem,String)->Unit, onCopy:(CoordinateHistoryItem)->Unit, onShare:(CoordinateHistoryItem)->Unit, onOpenMaps:(CoordinateHistoryItem)->Unit) {
    val editing = remember { mutableStateMapOf<String, Boolean>() }
    val titles = remember { mutableStateMapOf<String, String>() }

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(strings.lastFiveCoordinates, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (history.isEmpty()) Text(strings.noHistoryYet, color = MaterialTheme.colorScheme.onSurfaceVariant)
            history.forEach { item ->
                val isEditing = editing[item.id] == true
                if (item.id !in titles) titles[item.id] = item.title
                Card(Modifier.fillMaxWidth().clickable { onOpen(item) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(item.rawText, style = MaterialTheme.typography.bodySmall)
                        Text("${"%.6f".format(item.latitude)}, ${"%.6f".format(item.longitude)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AssistChip(onClick = { onOpenMaps(item) }, label = { Text(strings.open) })
                            AssistChip(onClick = { onCopy(item) }, label = { Text(strings.copy) })
                            AssistChip(onClick = { onShare(item) }, label = { Text(strings.share) })
                            AssistChip(onClick = { onDelete(item) }, label = { Text(strings.delete) })
                            AssistChip(onClick = { editing[item.id] = !isEditing }, label = { Text(strings.editShort) })
                        }
                        if (isEditing) {
                            OutlinedTextField(value = titles[item.id].orEmpty(), onValueChange = { titles[item.id] = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onUpdateTitle(item, titles[item.id].orEmpty()); editing[item.id] = false }) { Text(strings.save) }
                                OutlinedButton(onClick = { titles[item.id] = item.title; editing[item.id] = false }) { Text(strings.cancel) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun ScanningCard(strings: UiStrings) { ElevatedCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { CircularProgressIndicator(Modifier.size(20.dp)); Text(strings.scanningImage) } } }
@Composable private fun ErrorCard(message: String) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f))) { Text(message, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error) } }
@Composable private fun TipCard(strings: UiStrings) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) { Text(strings.tipText, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Start) } }
