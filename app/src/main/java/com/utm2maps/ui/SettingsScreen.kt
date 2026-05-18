package com.utm2maps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.utm2maps.data.AppSettings
import com.utm2maps.data.InterfaceLanguage
import com.utm2maps.geo.Hemisphere

@Composable
fun SettingsScreen(
    strings: UiStrings,
    settings: AppSettings,
    onSave: (AppSettings) -> Unit,
    onBack: () -> Unit
) {
    var zone by remember(settings) { mutableStateOf(settings.zoneNumber.toString()) }
    var latitudeBand by remember(settings) { mutableStateOf(settings.latitudeBand) }
    var hemisphere by remember(settings) { mutableStateOf(settings.hemisphere) }
    var northingPrefix by remember(settings) { mutableStateOf(settings.northingPrefix) }
    var autoOpen by remember(settings) { mutableStateOf(settings.autoOpenGoogleMaps) }
    var copyAutomatically by remember(settings) { mutableStateOf(settings.copyLinkAutomatically) }
    var interfaceLanguage by remember(settings) { mutableStateOf(settings.interfaceLanguage) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = strings.settings,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        SettingsGroup(title = strings.utmSettings) {
            Text(
                text = strings.conversionHint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = zone,
                onValueChange = { zone = it.filter(Char::isDigit).take(2) },
                label = { Text(strings.zoneNumber) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = latitudeBand,
                onValueChange = { latitudeBand = it.uppercase().take(1) },
                label = { Text(strings.latitudeBand) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text(
                text = strings.hemisphere,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            SingleChoiceRow(strings.north, hemisphere == Hemisphere.NORTH) {
                hemisphere = Hemisphere.NORTH
            }
            SingleChoiceRow(strings.south, hemisphere == Hemisphere.SOUTH) {
                hemisphere = Hemisphere.SOUTH
            }
            OutlinedTextField(
                value = northingPrefix,
                onValueChange = { northingPrefix = it.filter(Char::isDigit).take(4) },
                label = { Text(strings.northingPrefix) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        SettingsGroup(title = strings.appSettings) {
            Text(
                text = strings.interfaceLanguage,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            SingleChoiceRow(strings.hebrew, interfaceLanguage == InterfaceLanguage.HEBREW) {
                interfaceLanguage = InterfaceLanguage.HEBREW
            }
            SingleChoiceRow(strings.english, interfaceLanguage == InterfaceLanguage.ENGLISH) {
                interfaceLanguage = InterfaceLanguage.ENGLISH
            }
            SettingCheckbox(strings.autoOpenGoogleMaps, autoOpen) { autoOpen = it }
            SettingCheckbox(strings.copyLinkAutomatically, copyAutomatically) { copyAutomatically = it }
        }

        SettingsGroup(title = strings.about) {
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(strings.createdByFull, style = MaterialTheme.typography.bodyLarge)
        }

        Button(
            onClick = {
                onSave(
                    AppSettings(
                        zoneNumber = zone.toIntOrNull()?.coerceIn(1, 60) ?: 36,
                        latitudeBand = latitudeBand.ifBlank { "R" },
                        hemisphere = hemisphere,
                        northingPrefix = northingPrefix.ifBlank { "3" },
                        autoOpenGoogleMaps = autoOpen,
                        copyLinkAutomatically = copyAutomatically,
                        interfaceLanguage = interfaceLanguage
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(strings.save)
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(strings.cancel)
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
private fun SingleChoiceRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SettingCheckbox(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
