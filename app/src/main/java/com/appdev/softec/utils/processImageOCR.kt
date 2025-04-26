package com.appdev.softec.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private suspend fun processImageOCR(imageUri: Uri,context: Context): String {
    return try {
        // Create an InputImage from the URI
        val bitmap = getBitmapFromUri(imageUri,context)
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        // Get text recognition result
        val recognizedText = recognizeText(inputImage)

        // Return the extracted text
        recognizedText
    } catch (e: IOException) {
        throw IOException("Failed to process image: ${e.localizedMessage}")
    }
}

private fun getBitmapFromUri(uri: Uri,context: Context): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = true
        }
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
}

private suspend fun recognizeText(image: InputImage): String = suspendCancellableCoroutine { continuation ->
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            val extractedText = visionText.text
            continuation.resume(extractedText)
        }
        .addOnFailureListener { e ->
            continuation.resumeWithException(e)
        }
        .addOnCanceledListener {
            continuation.cancel()
        }
}