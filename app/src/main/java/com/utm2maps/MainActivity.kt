package com.utm2maps

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.utm2maps.data.AppSettings
import com.utm2maps.data.SettingsRepository
import com.utm2maps.geo.LatLon
import com.utm2maps.geo.UtmCandidate
import com.utm2maps.geo.UtmConverter
import com.utm2maps.geo.buildGoogleMapsUrl
import com.utm2maps.ocr.TextRecognitionService
import com.utm2maps.parser.UtmParser
import com.utm2maps.ui.MainScreen
import com.utm2maps.ui.ResultScreen
import com.utm2maps.ui.SettingsScreen
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Utm2MapsApp() }
    }
}

private enum class Screen { MAIN, RESULT, SETTINGS }

data class ScanResult(
    val recognizedText: String,
    val candidates: List<UtmCandidate>,
    val selectedIndex: Int = 0
) {
    val selectedCandidate: UtmCandidate? = candidates.getOrNull(selectedIndex)
    val selectedLatLon: LatLon? = selectedCandidate?.let {
        UtmConverter.toLatLon(it.easting, it.fullNorthing, it.zone, it.hemisphere)
    }
    val selectedMapsUrl: String? = selectedLatLon?.let(::buildGoogleMapsUrl)
}

@Composable
private fun Utm2MapsApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { SettingsRepository(context) }
    val settings by repository.settings.collectAsState(initial = AppSettings())
    val ocrService = remember { TextRecognitionService(context) }

    var screen by rememberSaveable { mutableStateOf(Screen.MAIN) }
    var isScanning by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var scanResult by remember { mutableStateOf<ScanResult?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun openMaps(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun copyLink(url: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("UTM2Maps link", url))
        Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
    }

    fun scanImage(uri: Uri) {
        scope.launch {
            isScanning = true
            errorMessage = null
            runCatching { ocrService.recognize(uri) }
                .onSuccess { text ->
                    val candidates = UtmParser.parseCandidates(
                        text = text,
                        zone = settings.zoneNumber,
                        hemisphere = settings.hemisphere,
                        northingPrefix = settings.northingPrefix
                    )
                    if (candidates.isEmpty()) {
                        errorMessage = "לא נמצאה קואורדינטת UTM תקינה בתמונה"
                    } else {
                        scanResult = ScanResult(text, candidates)
                        screen = Screen.RESULT
                    }
                }
                .onFailure { error -> errorMessage = error.localizedMessage ?: "OCR failed" }
            isScanning = false
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scanImage(uri)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) scanImage(uri)
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createCameraUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            errorMessage = "Camera permission denied"
        }
    }

    LaunchedEffect(scanResult?.selectedMapsUrl, settings.copyLinkAutomatically, settings.autoOpenGoogleMaps) {
        val url = scanResult?.selectedMapsUrl ?: return@LaunchedEffect
        if (settings.copyLinkAutomatically) copyLink(url)
        if (settings.autoOpenGoogleMaps) openMaps(url)
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (screen) {
                Screen.MAIN -> MainScreen(
                    lastResult = scanResult,
                    isScanning = isScanning,
                    errorMessage = errorMessage,
                    onChooseImage = { imagePicker.launch("image/*") },
                    onTakeImage = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            val uri = createCameraUri(context)
                            pendingCameraUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onOpenSettings = { screen = Screen.SETTINGS },
                    onOpenLastResult = { if (scanResult != null) screen = Screen.RESULT }
                )
                Screen.RESULT -> ResultScreen(
                    result = scanResult,
                    onCandidateSelected = { index -> scanResult = scanResult?.copy(selectedIndex = index) },
                    onOpenMaps = { url -> openMaps(url) },
                    onCopyLink = { url -> copyLink(url) },
                    onShareLink = { url -> shareLink(context, url) },
                    onScanAnother = { screen = Screen.MAIN },
                    onBack = { screen = Screen.MAIN }
                )
                Screen.SETTINGS -> SettingsScreen(
                    settings = settings,
                    onSave = { newSettings ->
                        scope.launch { repository.save(newSettings) }
                        screen = Screen.MAIN
                    },
                    onBack = { screen = Screen.MAIN }
                )
            }
        }
    }
}

private fun createCameraUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val imageFile = File.createTempFile("utm2maps_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
}

private fun shareLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }
    context.startActivity(Intent.createChooser(intent, "Share Google Maps link"))
}
