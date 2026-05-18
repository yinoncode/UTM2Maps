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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.utm2maps.data.AppSettings
import com.utm2maps.data.InterfaceLanguage
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
import com.utm2maps.ui.SplashScreen
import com.utm2maps.ui.Utm2MapsTheme
import com.utm2maps.ui.stringsFor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Utm2MapsApp() }
    }
}

private enum class Screen {
    MAIN,
    RESULT,
    SETTINGS
}

data class ScanResult(
    val recognizedText: String,
    val candidates: List<UtmCandidate>,
    val selectedIndex: Int = 0
) {
    val selectedCandidate: UtmCandidate? = candidates.getOrNull(selectedIndex)
    val selectedLatLon: LatLon? = selectedCandidate?.let { candidate ->
        UtmConverter.toLatLon(
            easting = candidate.easting,
            northing = candidate.fullNorthing,
            zone = candidate.zone,
            hemisphere = candidate.hemisphere
        )
    }
    val selectedMapsUrl: String? = selectedLatLon?.let(::buildGoogleMapsUrl)
}

@Composable
private fun Utm2MapsApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { SettingsRepository(context) }
    val settings by repository.settings.collectAsState(initial = AppSettings())
    val strings = stringsFor(settings.interfaceLanguage)
    val layoutDirection = when (settings.interfaceLanguage) {
        InterfaceLanguage.HEBREW -> LayoutDirection.Rtl
        InterfaceLanguage.ENGLISH -> LayoutDirection.Ltr
    }
    val ocrService = remember { TextRecognitionService(context) }

    var showSplash by rememberSaveable { mutableStateOf(true) }
    var screen by rememberSaveable { mutableStateOf(Screen.MAIN) }
    var isScanning by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var resultErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var manualText by rememberSaveable { mutableStateOf("") }
    var scanResult by remember { mutableStateOf<ScanResult?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun openMaps(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun copyLink(url: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(strings.googleMapsLink, url))
        Toast.makeText(context, strings.linkCopied, Toast.LENGTH_SHORT).show()
    }

    fun copyTextToClipboard(label: String, text: String, toastMessage: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }

    fun copyRecognizedText(text: String) {
        copyTextToClipboard(strings.ocrText, text, strings.recognizedTextCopied)
    }

    fun copySelectedText(text: String) {
        copyTextToClipboard(strings.selectedTextForExtraction, text, strings.selectedTextCopied)
    }

    fun copyOcrLine(text: String) {
        copyTextToClipboard(strings.recognizedTextFromImage, text, strings.lineCopied)
    }

    fun parseSelectedText(text: String) {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) {
            resultErrorMessage = strings.noCoordinateInSelectedLine
            return
        }

        val candidates = UtmParser.parseCandidates(
            text = trimmedText,
            zone = settings.zoneNumber,
            hemisphere = settings.hemisphere,
            northingPrefix = settings.northingPrefix
        )
        if (candidates.isEmpty()) {
            resultErrorMessage = strings.noCoordinateInSelectedLine
        } else {
            val originalRecognizedText = scanResult?.recognizedText ?: trimmedText
            scanResult = ScanResult(recognizedText = originalRecognizedText, candidates = candidates)
            resultErrorMessage = null
        }
    }

    fun parseTextInput(text: String, showResultError: Boolean = false) {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) {
            if (showResultError) {
                resultErrorMessage = strings.noValidCoordinateInText
            } else {
                errorMessage = strings.noValidCoordinateInText
            }
            return
        }

        val candidates = UtmParser.parseCandidates(
            text = trimmedText,
            zone = settings.zoneNumber,
            hemisphere = settings.hemisphere,
            northingPrefix = settings.northingPrefix
        )
        if (candidates.isEmpty()) {
            if (showResultError) {
                resultErrorMessage = strings.noValidCoordinateInText
            } else {
                errorMessage = strings.noValidCoordinateInText
            }
        } else {
            errorMessage = null
            resultErrorMessage = null
            scanResult = ScanResult(recognizedText = trimmedText, candidates = candidates)
            screen = Screen.RESULT
        }
    }

    fun scanImage(uri: Uri) {
        scope.launch {
            isScanning = true
            errorMessage = null
            resultErrorMessage = null
            runCatching { ocrService.recognize(uri) }
                .onSuccess { text ->
                    val candidates = UtmParser.parseCandidates(
                        text = text,
                        zone = settings.zoneNumber,
                        hemisphere = settings.hemisphere,
                        northingPrefix = settings.northingPrefix
                    )
                    if (candidates.isEmpty()) {
                        errorMessage = strings.noValidCoordinateFound
                    } else {
                        scanResult = ScanResult(recognizedText = text, candidates = candidates)
                        resultErrorMessage = null
                        screen = Screen.RESULT
                    }
                }
                .onFailure { error -> errorMessage = error.localizedMessage ?: strings.ocrFailed }
            isScanning = false
        }
    }

    LaunchedEffect(Unit) {
        delay(2_000)
        showSplash = false
    }

    LaunchedEffect(scanResult?.selectedMapsUrl, settings.copyLinkAutomatically, settings.autoOpenGoogleMaps) {
        val url = scanResult?.selectedMapsUrl ?: return@LaunchedEffect
        if (settings.copyLinkAutomatically) copyLink(url)
        if (settings.autoOpenGoogleMaps) openMaps(url)
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
            errorMessage = strings.cameraPermissionDenied
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Utm2MapsTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                if (showSplash) {
                    SplashScreen(strings = strings)
                } else {
                    when (screen) {
                        Screen.MAIN -> MainScreen(
                            strings = strings,
                            lastResult = scanResult,
                            isScanning = isScanning,
                            errorMessage = errorMessage,
                            manualText = manualText,
                            onManualTextChange = { manualText = it },
                            onExtractManualText = { parseTextInput(manualText) },
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
                            strings = strings,
                            result = scanResult,
                            errorMessage = resultErrorMessage,
                            onCandidateSelected = { index -> scanResult = scanResult?.copy(selectedIndex = index) },
                            onCopyAllText = ::copyRecognizedText,
                            onCopySelectedText = ::copySelectedText,
                            onCopyOcrLine = ::copyOcrLine,
                            onParseSelectedText = ::parseSelectedText,
                            onOpenMaps = ::openMaps,
                            onCopyLink = ::copyLink,
                            onShareLink = { url -> shareLink(context, url, strings.shareChooserTitle) },
                            onScanAnother = { screen = Screen.MAIN },
                            onBack = { screen = Screen.MAIN }
                        )

                        Screen.SETTINGS -> SettingsScreen(
                            strings = strings,
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
    }
}

private fun createCameraUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val imageFile = File.createTempFile("utm2maps_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
}

private fun shareLink(context: Context, url: String, chooserTitle: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}
