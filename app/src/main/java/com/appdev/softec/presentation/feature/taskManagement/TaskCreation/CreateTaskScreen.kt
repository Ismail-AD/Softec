package com.appdev.softec.presentation.feature.taskManagement.TaskCreation

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appdev.softec.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.util.concurrent.Executor
import android.speech.RecognizerIntent
import android.content.Intent
import android.speech.SpeechRecognizer
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onNavigateBack: () -> Unit,
    onTaskCreated: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val datePickerState = rememberDatePickerState()
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // For date picker
    var showDatePicker by remember { mutableStateOf(false) }

    // Permissions
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val microphonePermissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)

    // For camera
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = ContextCompat.getMainExecutor(context)
    val outputDirectory = context.getExternalFilesDir(null)

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // Extract the result directly from the intent
            val spokenText: ArrayList<String>? = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            // Handle the result: get the first recognized text or fallback to an empty string
            val recognizedText = spokenText?.firstOrNull() ?: ""
            viewModel.processVoiceInput(recognizedText)
        }
    }




    // Effects
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(uiState.errorMessage)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            snackbarHostState.showSnackbar("Task saved successfully")
            onTaskCreated()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create New Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveTask() },
                        enabled = !uiState.isLoading && uiState.taskText.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Task input field
                OutlinedTextField(
                    value = uiState.taskText,
                    onValueChange = { viewModel.updateTaskText(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Enter task (e.g., Buy milk tomorrow)") },
                    trailingIcon = {
                        Row {
                            // Voice input button
                            IconButton(
                                onClick = {
                                    if (microphonePermissionState.status.isGranted) {
                                        startVoiceRecognition(context, speechRecognizerLauncher)
                                    } else {
                                        microphonePermissionState.launchPermissionRequest()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input"
                                )
                            }

                            // Camera button
                            IconButton(
                                onClick = {
                                    if (cameraPermissionState.status.isGranted) {
                                        viewModel.toggleCameraActive()
                                    } else {
                                        cameraPermissionState.launchPermissionRequest()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Camera"
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.availableCategories.size) { index ->
                        val category = uiState.availableCategories[index]
                        val isSelected = category == uiState.category

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateCategory(category) },
                            label = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Due date selection
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )

                    if (uiState.dueDate != null) {
                        Text(
                            text = uiState.dueDate?.let { dateFormatter.format(Date(it)) } ?: "No due date",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        IconButton(onClick = { viewModel.updateDueDate(null) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Date"
                            )
                        }
                    } else {
                        OutlinedButton(onClick = { showDatePicker = true }) {
                            Text("Set Due Date")
                        }
                    }
                }
            }

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Camera view
            AnimatedVisibility(
                visible = uiState.isCameraActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    CameraPreview(
                        imageCapture = imageCapture,
                        executor = executor,
                        onImageCaptured = { uri ->
                            viewModel.processCameraInput(uri)
                        },
                        onError = { error ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Camera error: $error")
                            }
                            viewModel.toggleCameraActive()
                        }
                    )

                    // Camera controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleCameraActive() },
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.onSurface
                            )

                        }

                        IconButton(
                            onClick = {
                                val photoFile = File(
                                    outputDirectory,
                                    "task_scan_${System.currentTimeMillis()}.jpg"
                                )

                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                // This function can remain as is, but make sure the image quality is good enough for OCR
                                imageCapture.takePicture(
                                    outputOptions,
                                    executor,
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            val savedUri = Uri.fromFile(photoFile)
                                            val contentUri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                photoFile
                                            )
                                            viewModel.processCameraInput(contentUri)
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Failed to capture image")
                                            }
                                            viewModel.toggleCameraActive()
                                        }
                                    }
                                )
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(40.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Take Photo",
                                tint = MaterialTheme.colorScheme.inverseOnSurface,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }

            // Processing indicator
            if (uiState.isProcessingInput) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Processing input...")
                    }
                }
            }
        }
    }

    // Date picker dialog

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.updateDueDate(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun CameraPreview(
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Camera initialization failed")
                }
            }, executor)

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun startVoiceRecognition(
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your task")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }

    try {
        launcher.launch(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Voice recognition not available on this device", Toast.LENGTH_SHORT).show()
    }
}