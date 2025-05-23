package com.appdev.softec.presentation.feature.Mood


import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appdev.softec.presentation.components.CustomLoader
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appdev.softec.domain.model.MoodEntry


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
    var showAnalysisSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showAnalysisSheet = uiState.isAnalysisVisible
    }


    val context = LocalContext.current
    val selectedAnalysisPeriod = remember { mutableStateOf(uiState.selectedPeriod) }

    LaunchedEffect(uiState.selectedPeriod) {
        selectedAnalysisPeriod.value = uiState.selectedPeriod
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage.isNotEmpty()) {
            Toast.makeText(context, uiState.successMessage, Toast.LENGTH_SHORT).show()
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

    if (showAnalysisSheet) {
        MoodAnalysisSheet(
            moodEntries = uiState.moodEntries,
            selectedPeriod = selectedAnalysisPeriod.value,
            onPeriodSelected = {
                selectedAnalysisPeriod.value = it
                viewModel.updateSelectedPeriod(it)
            },
            onDismiss = {
                showAnalysisSheet = false
                viewModel.toggleAnalysisVisibility()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mood Journal") },
                actions = {
                    IconButton(onClick = {
                        showAnalysisSheet = true
                        viewModel.toggleAnalysisVisibility()
                    }) {
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
        if (uiState.isSaving) {
            CustomLoader()
        }

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
                    ),
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp)
                    ) {
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

                items(uiState.moodEntries.sortedByDescending { it.timestamp }) { entry ->
                    MoodEntryItem(entry = entry, timeFormatter = timeFormatter)
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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

    // Filter entries based on selected period
    val filteredEntries = remember(moodEntries, selectedPeriod) {
        filterEntriesByPeriod(moodEntries, selectedPeriod)
    }

    // Generate pie chart data from filtered entries
    val chartData = remember(filteredEntries) {
        generatePieChartData(filteredEntries)
    }

    // Create mood count statistics
    val moodStats = remember(filteredEntries) {
        calculateMoodStats(filteredEntries)
    }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
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
                AnalysisPeriod.entries.forEach { period ->
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

            Spacer(modifier = Modifier.height(16.dp))

            // Summary text
            Text(
                text = "Summary for ${selectedPeriod.name.lowercase()}: ${filteredEntries.size} entries",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pie Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (chartData.isNotEmpty()) {
                        var selectedChartData by remember {
                            mutableStateOf(chartData)
                        }

                        PieChart(
                            modifier = Modifier.size(150.dp),
                            data = selectedChartData,
                            onPieClick = { clickedPie ->
                                val pieIndex = selectedChartData.indexOf(clickedPie)
                                selectedChartData = selectedChartData.mapIndexed { mapIndex, pie ->
                                    pie.copy(selected = pieIndex == mapIndex)
                                }
                            },
                            selectedScale = 1.1f,
                            scaleAnimEnterSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            colorAnimEnterSpec = tween(300),
                            colorAnimExitSpec = tween(300),
                            scaleAnimExitSpec = tween(300),
                            spaceDegreeAnimExitSpec = tween(300),
                            style = Pie.Style.Fill
                        )
                    } else {
                        Text(
                            text = "No mood data available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mood Statistics Legend
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Mood Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (moodStats.isEmpty()) {
                        Text(
                            text = "No mood data available for this period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        moodStats.forEach { (mood, count, percentage) ->
                            MoodStatItem(mood = mood, count = count, percentage = percentage)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onDismiss() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Close")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
fun MoodStatItem(mood: MoodType, count: Int, percentage: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mood emoji and color indicator
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(mood.getColor()),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mood.getEmoji(),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Mood name
        Text(
            text = mood.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        // Count and percentage
        Text(
            text = "$count (${"%.1f".format(percentage)}%)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
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


@Composable
fun MoodPieChart(
    isVisible: Boolean,
    moodEntries: List<MoodEntry>,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mood Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Calculate mood counts and percentages
            val moodCounts = moodEntries
                .groupBy { it.mood }
                .mapValues { it.value.size }

            val totalEntries = moodEntries.size.toFloat()

            // Convert to PieData format
            var data by remember {
                mutableStateOf(
                    MoodType.entries.mapNotNull { mood ->
                        val count = moodCounts[mood] ?: 0
                        if (count > 0) {
                            Pie(
                                data = count.toDouble(),
                                color = mood.getColor(),
                                selected = false,
                                label = "${
                                    mood.name.lowercase().replaceFirstChar { it.uppercase() }
                                }\n${(count / totalEntries * 100).toInt()}%"
                            )
                        } else null
                    }
                )
            }

            if (data.isNotEmpty()) {
                PieChart(
                    modifier = Modifier.size(280.dp),
                    data = data,
                    onPieClick = { clickedPie ->
                        val pieIndex = data.indexOf(clickedPie)
                        data = data.mapIndexed { mapIndex, pie ->
                            pie.copy(selected = pieIndex == mapIndex)
                        }
                    },
                    selectedScale = 1.1f,
                    scaleAnimEnterSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    colorAnimEnterSpec = tween(300),
                    colorAnimExitSpec = tween(300),
                    scaleAnimExitSpec = tween(300),
                    spaceDegreeAnimExitSpec = tween(300),
                    style = Pie.Style.Fill
                )
            } else {
                Text(
                    text = "No mood data available yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    }
}

// Filter entries by the selected time period
private fun filterEntriesByPeriod(
    entries: List<MoodEntry>,
    period: AnalysisPeriod
): List<MoodEntry> {
    val currentTime = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = currentTime

    return when (period) {
        AnalysisPeriod.WEEK -> {
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val weekAgo = calendar.timeInMillis
            entries.filter { it.timestamp >= weekAgo }
        }

        AnalysisPeriod.MONTH -> {
            calendar.add(Calendar.MONTH, -1)
            val monthAgo = calendar.timeInMillis
            entries.filter { it.timestamp >= monthAgo }
        }

        AnalysisPeriod.YEAR -> {
            calendar.add(Calendar.YEAR, -1)
            val yearAgo = calendar.timeInMillis
            entries.filter { it.timestamp >= yearAgo }
        }
    }
}

// Generate PieChart data from entries
private fun generatePieChartData(moodEntries: List<MoodEntry>): List<Pie> {
    val moodCounts = moodEntries
        .groupBy { it.mood }
        .mapValues { it.value.size }

    val totalEntries = moodEntries.size.toFloat().takeIf { it > 0 } ?: 1f

    return MoodType.entries.mapNotNull { mood ->
        val count = moodCounts[mood] ?: 0
        if (count > 0) {
            Pie(
                data = count.toDouble(),
                color = mood.getColor(),
                selected = false,
                label = "${mood.name.lowercase().replaceFirstChar { it.uppercase() }}"
            )
        } else null
    }
}

// Calculate mood statistics for display
private fun calculateMoodStats(moodEntries: List<MoodEntry>): List<Triple<MoodType, Int, Float>> {
    val moodCounts = moodEntries
        .groupBy { it.mood }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }

    val totalEntries = moodEntries.size.toFloat().takeIf { it > 0 } ?: 1f

    return moodCounts.map { (mood, count) ->
        Triple(mood, count, (count / totalEntries) * 100f)
    }
}