package com.appdev.softec.presentation.feature.taskManagement.TaskCreation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
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
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_KEY
    )

    // Simply update the text without AI processing
    fun updateTaskText(text: String) {
        _uiState.update { it.copy(taskText = text) }
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
                isVoiceInputActive = false
            )
        }
        // We don't run AI processing here, just set the text
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
                    // No AI processing here
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



    // New function to extract structured data from natural language input
    private suspend fun processNaturalLanguageInput(text: String): NaturalLanguageResult {
        try {
            // Generate the prompt for Gemini to extract structured data
            val prompt = """
            Parse the following natural language task description and extract structured information.
            Task description: "$text"
            
            Return a JSON object with these properties:
            1. "taskText": The core task description without any date/time information
            2. "dueDate": Date in ISO format (YYYY-MM-DD) if specified, or null if not specified
            3. "dueTime": Time in 24h format (HH:MM) if specified, or null if not specified
            4. "priority": Extract priority level if mentioned (HIGH, MEDIUM, LOW), or "MEDIUM" if not specified
            5. "category": Best matching category from these options: GENERAL, WORK, PERSONAL, SHOPPING, HEALTH, EDUCATION, FINANCE, OTHER
        """.trimIndent()

            Log.d("TaskViewModel", "Generated Prompt: $prompt")

            // Make API call to Gemini
            val response = generativeModel.generateContent(prompt).text?.trim() ?: return NaturalLanguageResult()

            Log.d("TaskViewModel", "Gemini API Response: $response")

            // Sanitize the response string, removing any unwanted characters (like backticks)
            val sanitizedResponse = response.replace("```json", "").replace("```", "").trim()
            Log.d("TaskViewModel", "Gemini API Response SANITIZED: $sanitizedResponse")

            try {
                // Parse the sanitized response into a JSON object
                val jsonResponse = JSONObject(sanitizedResponse)

                val dueDateStr = if (jsonResponse.has("dueDate") && !jsonResponse.isNull("dueDate"))
                    jsonResponse.getString("dueDate") else null
                val dueTimeStr = if (jsonResponse.has("dueTime") && !jsonResponse.isNull("dueTime"))
                    jsonResponse.getString("dueTime") else null

                // Calculate timestamp from date and time if present
                val dueTimestamp = calculateTimestamp(dueDateStr, dueTimeStr)

                val categoryStr = if (jsonResponse.has("category")) jsonResponse.getString("category") else "GENERAL"
                val category = try {
                    TaskCategory.valueOf(categoryStr)
                } catch (e: IllegalArgumentException) {
                    TaskCategory.GENERAL
                }

                val taskText = if (jsonResponse.has("taskText")) jsonResponse.getString("taskText") else text
                Log.d("TaskViewModel", "Parsed Task Text: $taskText, Category: $categoryStr")

                return NaturalLanguageResult(
                    taskText = taskText,
                    category = category,
                    dueDate = dueTimestamp
                )
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error parsing Gemini response: ${e.localizedMessage}")
                return NaturalLanguageResult(taskText = text)
            }
        } catch (e: Exception) {
            Log.e("TaskViewModel", "Error processing natural language input: ${e.localizedMessage}")
            return NaturalLanguageResult(taskText = text)
        }
    }



    private fun calculateTimestamp(dateStr: String?, timeStr: String?): Long? {
        if (dateStr == null) return null

        try {
            val calendar = Calendar.getInstance()

            // Parse date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = dateFormat.parse(dateStr)

            if (date != null) {
                calendar.time = date

                // Add time if available
                if (timeStr != null) {
                    val timeParts = timeStr.split(":")
                    if (timeParts.size == 2) {
                        val hour = timeParts[0].toIntOrNull() ?: 0
                        val minute = timeParts[1].toIntOrNull() ?: 0

                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                    }
                } else {
                    // Default to 9:00 AM if no time specified
                    calendar.set(Calendar.HOUR_OF_DAY, 9)
                    calendar.set(Calendar.MINUTE, 0)
                }

                return calendar.timeInMillis
            }
        } catch (e: Exception) {
            // If parsing fails, return null
        }

        return null
    }

    // Natural Language Processing result data class
    data class NaturalLanguageResult(
        val taskText: String = "",
        val category: TaskCategory = TaskCategory.GENERAL,
        val dueDate: Long? = null
    )

    // New function to prepare and save task with natural language processing
    fun prepareAndSaveTask() {
        if (!validateTask()) return

        _uiState.update { it.copy(isLoading = true, isProcessingInput = true) }

        viewModelScope.launch {
            try {
                if (uiState.value.taskText.isNotBlank()) {
                    // First, process natural language to extract structured data
                    val nlpResult = processNaturalLanguageInput(uiState.value.taskText)

                    Log.d("TaskViewModel", "Extracted Task Data: $nlpResult")
                    // Update UI state with the extracted data
                    _uiState.update { currentState ->
                        currentState.copy(
                            taskText = nlpResult.taskText,
                            category = nlpResult.category,
                            dueDate = nlpResult.dueDate ?: currentState.dueDate
                        )
                    }

                    // Save the task with the structured data
                    saveTaskToRepository()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isProcessingInput = false,
                        errorMessage = "Error processing natural language: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun saveTaskToRepository() {
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
                            isProcessingInput = false,
                            success = true,
                            errorMessage = ""
                        )}
                    }
                    is ResultState.Failure -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            isProcessingInput = false,
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
        if (uiState.value.dueDate == null) {
            // Notify the user that they need to set the due date
            _uiState.update {
                it.copy(errorMessage = "Please set a due date before saving the task.")
            }
            return false
        }
        return true
    }

    // Keep original saveTask for backward compatibility
    fun saveTask() {
        prepareAndSaveTask()
    }

    fun resetState() {
        _uiState.update { TaskUiState() }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = "") }
    }
}