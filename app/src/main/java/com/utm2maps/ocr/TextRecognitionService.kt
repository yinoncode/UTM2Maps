package com.utm2maps.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TextRecognitionService(private val context: Context) {
    // ML Kit's bundled on-device recognizer is kept offline. It may not fully
    // recognize Hebrew words, but it reliably extracts the digits and common
    // coordinate separators that UtmParser needs from mixed Hebrew/number text.
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognize(uri: Uri): String = suspendCoroutine { continuation ->
        val image = runCatching { InputImage.fromFilePath(context, uri) }
            .getOrElse { error ->
                continuation.resumeWithException(error)
                return@suspendCoroutine
            }

        recognizer.process(image)
            .addOnSuccessListener { result -> continuation.resume(result.text) }
            .addOnFailureListener { error -> continuation.resumeWithException(error) }
    }
}
