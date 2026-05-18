package com.utm2maps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.utm2maps.data.AppSettings
import com.utm2maps.geo.Hemisphere

@Composable
fun SettingsScreen(
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Text("Latitude Band is saved for display/context. Conversion uses Zone Number and Hemisphere.")

        OutlinedTextField(
            value = zone,
            onValueChange = { zone = it.filter(Char::isDigit).take(2) },
            label = { Text("UTM Zone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = latitudeBand,
            onValueChange = { latitudeBand = it.uppercase().take(1) },
            label = { Text("Latitude Band") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Text("Hemisphere")
        Hemisphere.entries.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = hemisphere == option, onClick = { hemisphere = option }),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = hemisphere == option, onClick = { hemisphere = option })
                Text(option.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
        OutlinedTextField(
            value = northingPrefix,
            onValueChange = { northingPrefix = it.filter(Char::isDigit).take(4) },
            label = { Text("Northing Prefix") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        SettingCheckbox("Auto open Google Maps", autoOpen) { autoOpen = it }
        SettingCheckbox("Copy link automatically", copyAutomatically) { copyAutomatically = it }

        Button(
            onClick = {
                onSave(
                    AppSettings(
                        zoneNumber = zone.toIntOrNull()?.coerceIn(1, 60) ?: 36,
                        latitudeBand = latitudeBand.ifBlank { "R" },
                        hemisphere = hemisphere,
                        northingPrefix = northingPrefix.ifBlank { "3" },
                        autoOpenGoogleMaps = autoOpen,
                        copyLinkAutomatically = copyAutomatically
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
    }
}

@Composable
private fun SettingCheckbox(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text)
    }
}
