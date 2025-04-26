package com.appdev.softec.presentation.feature.taskManagement.TaskCreation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appdev.softec.BuildConfig
import com.appdev.softec.domain.model.TaskData
import com.appdev.softec.domain.repository.TaskRepository
import com.appdev.softec.utils.ResultState
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.Date
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    // Initialize Gemini API client with your API key
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_KEY,
        generationConfig = generationConfig {
            temperature = 0.2f
            topK = 1
            topP = 0.8f
            maxOutputTokens = 100
        }
    )

    fun updateTaskText(text: String) {
        _uiState.update { it.copy(taskText = text) }
        if (text.isNotBlank() && text.length > 3) {
            detectCategoryFromText(text)
        }
    }

    fun updateCategory(category: TaskCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateDueDate(timestamp: Long?) {
        _uiState.update { it.copy(dueDate = timestamp) }
    }

    fun toggleVoiceInput() {
        _uiState.update { it.copy(isVoiceInputActive = !it.isVoiceInputActive) }
    }

    fun toggleCameraActive() {
        _uiState.update { it.copy(isCameraActive = !it.isCameraActive) }
    }

    fun processVoiceInput(recognizedText: String) {
        _uiState.update {
            it.copy(
                taskText = recognizedText,
                isVoiceInputActive = false,
                isProcessingInput = true
            )
        }

        detectCategoryFromText(recognizedText)
    }

    fun processCameraInput(imageUri: Uri) {
        _uiState.update {
            it.copy(
                isCameraActive = false,
                isProcessingInput = true
            )
        }

        viewModelScope.launch {
            try {
                val extractedText = processImageOCR(imageUri)

                if (extractedText.isNotBlank()) {
                    _uiState.update {
                        it.copy(
                            taskText = extractedText,
                            isProcessingInput = false
                        )
                    }
                    detectCategoryFromText(extractedText)
                } else {
                    _uiState.update {
                        it.copy(
                            isProcessingInput = false,
                            errorMessage = "No text detected in the image. Try again with clearer text."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessingInput = false,
                        errorMessage = "Error processing image: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private suspend fun processImageOCR(imageUri: Uri): String {
        return try {
            // Create an InputImage from the URI
            val bitmap = getBitmapFromUri(imageUri)
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            // Get text recognition result
            val recognizedText = recognizeText(inputImage)

            // Return the extracted text
            recognizedText
        } catch (e: IOException) {
            throw IOException("Failed to process image: ${e.localizedMessage}")
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
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

    private fun detectCategoryFromText(text: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isProcessingInput = true) }

                // Generate the prompt for Gemini
                val prompt = """
                    Analyze the following task description and categorize it into exactly one of these categories:
                    GENERAL, WORK, PERSONAL, SHOPPING, HEALTH, EDUCATION, FINANCE, OTHER
                    
                    Task description: "$text"
                    
                    Return only the category name in uppercase without any explanation.
                """.trimIndent()

                // Make API call to Gemini
                val response = generativeModel.generateContent(prompt).text?.trim() ?: "GENERAL"

                // Parse the response and update state
                try {
                    val detectedCategory = TaskCategory.valueOf(response)
                    _uiState.update {
                        it.copy(
                            category = detectedCategory,
                            isProcessingInput = false
                        )
                    }
                } catch (e: IllegalArgumentException) {
                    // If the response doesn't match a category, fallback to GENERAL
                    _uiState.update {
                        it.copy(
                            category = TaskCategory.GENERAL,
                            isProcessingInput = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessingInput = false,
                        errorMessage = "Error detecting category: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun saveTask() {
        if (!validateTask()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val taskData = mapUiStateToTaskData()

            taskRepository.saveTask(taskData).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ResultState.Success -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            success = true,
                            errorMessage = ""
                        )}
                    }
                    is ResultState.Failure -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            success = false,
                            errorMessage = result.message.localizedMessage ?: "Failed to save task"
                        )}
                    }
                }
            }
        }
    }

    private fun mapUiStateToTaskData(): TaskData {
        return TaskData(
            id = uiState.value.taskId,
            text = uiState.value.taskText,
            category = uiState.value.category.name,
            createdAt = Date().time,
            dueDate = uiState.value.dueDate,
            isCompleted = false
        )
    }

    private fun validateTask(): Boolean {
        if (uiState.value.taskText.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Task description cannot be empty") }
            return false
        }
        return true
    }

    fun resetState() {
        _uiState.update { TaskUiState() }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = "") }
    }
}