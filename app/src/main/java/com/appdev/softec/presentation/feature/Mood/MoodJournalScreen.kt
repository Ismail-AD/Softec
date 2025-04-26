package com.appdev.softec.presentation.feature.Mood


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.util.lerp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appdev.softec.domain.model.MoodEntry
import com.appdev.softec.presentation.components.CustomLoader
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodJournalScreen(
    viewModel: MoodJournalViewModel = hiltViewModel(),
    navigateToAnalysis: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()) }
    val timeFormatter =
        remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) } // 12-hour format with AM/PM
    val calendarDialogVisible =
        remember { mutableStateOf(false) } // State to show the calendar dialog
    val calendar = Calendar.getInstance().apply { time = Date(uiState.selectedDate) }
    val datePickerState = remember {
        DatePickerState(
            initialSelectedDateMillis = calendar.timeInMillis,
            locale = Locale.getDefault()
        )
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.errorMessage)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.successMessage)
            viewModel.resetSuccessState() // Reset the success state after showing the message
        }
    }

    if (calendarDialogVisible.value) {
        DatePickerDialogFunction(
            onDismissRequest = { calendarDialogVisible.value = false },
            selectedDateMillis = uiState.selectedDate,
            onDateSelected = { selectedDateMillis ->
                viewModel.updateSelectedDate(selectedDateMillis)
                calendarDialogVisible.value = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mood Journal") },
                actions = {
                    IconButton(onClick = { viewModel.toggleAnalysisVisibility() }) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "View Mood Analysis"
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp), // Adds space between elements
            contentPadding = PaddingValues(16.dp)
        ) {
            // Date Selection
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Select a Date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateFormatter.format(Date(uiState.selectedDate)),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = { calendarDialogVisible.value = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select Date"
                            )
                        }
                    }
                }
            }

            // Mood Selector
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "How are you feeling today?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MoodType.entries.forEach { mood ->
                            MoodButton(
                                mood = mood,
                                isSelected = uiState.currentMood == mood,
                                onClick = { viewModel.selectMood(mood) }
                            )
                        }
                    }
                }
            }

            // Reflection Input
            item {
                OutlinedTextField(
                    value = uiState.moodNote,
                    onValueChange = { viewModel.updateMoodNote(it) },
                    label = { Text("Reflection (How do you feel and why?)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // Save Button
            item {
                Button(
                    onClick = { viewModel.saveMoodEntry() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !uiState.isSaving
                ) {
                    Text(
                        "SAVE MOOD",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Recent Entries Section
            if (uiState.isLoading) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            } else if (uiState.moodEntries.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Entries",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                items(uiState.moodEntries.sortedByDescending { it.timestamp }.take(5)) { entry ->
                    MoodEntryItem(entry = entry, timeFormatter = timeFormatter)
                    Divider(modifier = Modifier.fillMaxWidth())
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { navigateToAnalysis() },
                    ) {
                        Text("View All Entries")
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "View All",
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun MoodButton(
    mood: MoodType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) mood.getColor() else MaterialTheme.colorScheme.surface
                )
                .border(
                    width = if (isSelected) 0.dp else 2.dp,
                    color = if (isSelected) Color.Transparent else mood.getColor(),
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mood.getEmoji(),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = mood.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) mood.getColor() else MaterialTheme.colorScheme.onBackground
        )
    }
}


@Composable
fun MoodEntryItem(entry: MoodEntry, timeFormatter: SimpleDateFormat) {
    val fullDateFormatter =
        remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) } // For the full date

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(entry.mood.getColor()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.mood.getEmoji(),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Format the date and time separately
                val formattedDate = fullDateFormatter.format(Date(entry.timestamp))
                val formattedTime = timeFormatter.format(Date(entry.timestamp))

                // Display full date and time
                Text(
                    text = "$formattedDate at $formattedTime",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = entry.note,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodAnalysisSheet(
    moodEntries: List<MoodEntry>,
    selectedPeriod: AnalysisPeriod,
    onPeriodSelected: (AnalysisPeriod) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Mood Analysis",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Period Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AnalysisPeriod.values().forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodSelected(period) },
                        label = {
                            Text(
                                text = period.name.lowercase()
                                    .replaceFirstChar { it.uppercase() }
                            )
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mood Distribution
            Text(
                text = "Mood Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mood Chart - Simple Bar Chart representation
            // In a real app, you'd use a chart library
            val moodCounts = moodEntries
                .groupBy { it.mood }
                .mapValues { it.value.size }
                .toSortedMap()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val maxCount = if (moodCounts.values.isNotEmpty()) moodCounts.values.max() else 1

                MoodType.values().forEach { mood ->
                    val count = moodCounts[mood] ?: 0
                    val heightRatio = if (maxCount > 0) count.toFloat() / maxCount else 0f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(160.dp * heightRatio)
                                .background(
                                    mood
                                        .getColor()
                                        .copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = mood.getEmoji(),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mood Trends
            Text(
                text = "Mood Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Simplified trend visualization
            // Would use a line chart library in a real app
            if (moodEntries.isNotEmpty()) {
                val sortedEntries = moodEntries.sortedBy { it.timestamp }
                val dateFormatter = SimpleDateFormat("MM/dd", Locale.getDefault())

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    sortedEntries.forEach { entry ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = entry.mood.getEmoji(),
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = dateFormatter.format(Date(entry.timestamp)),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Not enough data to show trends",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogFunction(
    onDismissRequest: () -> Unit,
    selectedDateMillis: Long,
    onDateSelected: (Long) -> Unit
) {
    // Create a DatePickerState with the provided selectedDateMillis
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )

    DatePickerDialog(
        modifier = Modifier.padding(horizontal = 10.dp),
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    // Use the selected date from the state
                    datePickerState.selectedDateMillis?.let {
                        onDateSelected(it)
                    }
                    onDismissRequest()
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        content = {
            DatePicker(state = datePickerState)
        }
    )
}
