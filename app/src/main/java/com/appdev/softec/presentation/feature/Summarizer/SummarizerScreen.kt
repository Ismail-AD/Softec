package com.appdev.softec.presentation.feature.Summarizer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appdev.softec.presentation.components.CustomLoader
import com.appdev.softec.presentation.feature.taskManagement.TaskCreation.handleMicrophonePermission
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SummarizerScreen(
    viewModel: NoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // For voice input
    val microphonePermissionState = rememberPermissionState(permission = android.Manifest.permission.RECORD_AUDIO)
    var showMicrophoneRationale by rememberSaveable { mutableStateOf(false) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("SpeechRecognizer", "Got result with code: ${result.resultCode}")

        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText: ArrayList<String>? = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            Log.d("SpeechRecognizer", "Results: $spokenText")

            val recognizedText = spokenText?.firstOrNull() ?: ""
            Log.d("SpeechRecognizer", "Using recognized text: $recognizedText")

            if (recognizedText.isNotBlank()) {
                // Append microphone text to existing note text
                val newText = if (viewModel.uiState.value.noteText.isBlank()) {
                    recognizedText
                } else {
                    viewModel.uiState.value.noteText + " " + recognizedText
                }
                viewModel.updateNoteText(newText)
            }
        } else {
            Log.d("SpeechRecognizer", "Speech recognition failed or was cancelled")
        }
    }


    // Function to copy summary to clipboard
    fun copySummaryToClipboard() {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val summaryText = uiState.summary.joinToString("\n") { "- $it" }
        val clipData = ClipData.newPlainText("Summary", summaryText)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(context, "Summary copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Summarizer") },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Text field limited to 50% of screen height
                OutlinedTextField(
                    value = uiState.noteText,
                    onValueChange = { viewModel.updateNoteText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp), // Limit height to approximately half screen
                    label = { Text("Enter Notes to summarize") },
                    trailingIcon = {
                        Row {
                            // Microphone button for voice input
                            IconButton(
                                onClick = {
                                    Log.d("VoiceInput", "Mic button clicked")
                                    handleMicrophonePermission(
                                        permissionState = microphonePermissionState,
                                        onShowRationale = { showMicrophoneRationale = true },
                                        onPermissionGranted = {
                                            Log.d("VoiceInput", "Microphone permission granted, starting voice recognition")
                                            startVoiceRecognition(context, speechRecognizerLauncher)
                                        }
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input"
                                )
                            }

                            // Send Message button
                            IconButton(
                                onClick = {
                                    if (uiState.noteText.isBlank()) {
                                        Toast.makeText(context, "Please input some notes first.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.generateText()
                                        Log.d("SendMessage", "Message Sent")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send Message"
                                )
                            }

                        }
                    }
                )
            }

            item {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CustomLoader()
                    }
                }
            }

            // Summary section with copy button
            if (uiState.summary.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Summary:",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )

                        // Copy button in top-right corner
                        IconButton(
                            onClick = { copySummaryToClipboard() },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy to clipboard"
                            )
                        }
                    }
                }

                items(uiState.summary) { summary ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = "- $summary",
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Add extra space at the bottom for better scrolling experience
                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }
}

private fun startVoiceRecognition(context: Context, launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    // First check if speech recognition is available
    if (!android.speech.SpeechRecognizer.isRecognitionAvailable(context)) {
        Toast.makeText(context, "Speech recognition not available on this device", Toast.LENGTH_SHORT).show()
        return
    }

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your note")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }

    try {
        launcher.launch(intent)
    } catch (e: Exception) {
        Log.e("VoiceRecognition", "Error launching speech recognition", e)
        Toast.makeText(context, "Failed to start voice recognition: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}