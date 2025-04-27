package com.appdev.softec.presentation.feature.Home


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appdev.softec.domain.model.MoodEntry
import com.appdev.softec.domain.model.TaskData
import com.appdev.softec.domain.repository.MoodRepository
import com.appdev.softec.domain.repository.TaskRepository
import com.appdev.softec.presentation.feature.Mood.MoodType
import com.appdev.softec.utils.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// UI Components
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToAddTask: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToMoodJournal: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTask,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            HomeContent(
                uiState = uiState,
                onTaskStatusChanged = { taskId, isCompleted ->
                    viewModel.updateTaskStatus(taskId, isCompleted)
                },
                onDeleteTask = { taskId ->
                    viewModel.deleteTask(taskId)
                },
                onUpdateMood = { mood, note ->
                    viewModel.updateMood(mood, note)
                },
                onNavigateToCalendar = onNavigateToCalendar,
                onNavigateToMoodJournal = onNavigateToMoodJournal,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onTaskStatusChanged: (String, Boolean) -> Unit,
    onDeleteTask: (String) -> Unit,
    onUpdateMood: (MoodType, String) -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToMoodJournal: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date and welcome section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Hello, ${if (uiState.userEntry!=null) uiState.userEntry.name!! else "there"}!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.currentDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Mood tracker card
        item {
            MoodTrackerCard(
                currentMood = uiState.currentMood,
                onUpdateMood = onUpdateMood,
                onNavigateToMoodJournal = onNavigateToMoodJournal
            )
        }

        // Progress overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Progress",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val totalTasks = uiState.todaysTasks.size
                        ProgressStat(
                            title = "Completed",
                            count = uiState.completedTasksCount,
                            color = MaterialTheme.colorScheme.primary,
                            total = totalTasks
                        )
                        ProgressStat(
                            title = "Pending",
                            count = uiState.pendingTasksCount,
                            color = MaterialTheme.colorScheme.tertiary,
                            total = totalTasks
                        )
                        ProgressStat(
                            title = "Total",
                            count = totalTasks,
                            color = MaterialTheme.colorScheme.secondary,
                            total = totalTasks
                        )
                    }

                    if (uiState.todaysTasks.isNotEmpty()) {
                        val completionRate = if (uiState.todaysTasks.isEmpty()) 0f else
                            uiState.completedTasksCount.toFloat() / uiState.todaysTasks.size

                        Spacer(modifier = Modifier.height(12.dp))

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Overall Completion",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${(completionRate * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = completionRate,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Today's Tasks",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Tasks by category
        uiState.tasksByCategory.forEach { (category, tasks) ->
            item {
                TaskCategorySection(
                    category = category,
                    tasks = tasks,
                    onTaskStatusChanged = onTaskStatusChanged,
                    onDeleteTask = onDeleteTask
                )
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun MoodTrackerCard(
    currentMood: MoodEntry?,
    onUpdateMood: (MoodType, String) -> Unit,
    onNavigateToMoodJournal: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "How are you feeling?",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onNavigateToMoodJournal) {
                    Text("View Journal")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (currentMood != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val moodColor = getMoodColor(currentMood.mood)
                    val moodIcon = getMoodIcon(currentMood.mood)
                    val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val timeString = dateFormat.format(Date(currentMood.timestamp))

                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(moodColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = moodIcon,
                            contentDescription = currentMood.mood.name,
                            tint = moodColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentMood.mood.name.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentMood.note.isNotEmpty()) {
                            Text(
                                text = currentMood.note,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { onUpdateMood(MoodType.NEUTRAL, "") },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Update Mood")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MoodButton(mood = MoodType.HAPPY, onClick = {
                        onUpdateMood(MoodType.HAPPY, "Feeling good today!")
                    })
                    MoodButton(mood = MoodType.CALM, onClick = {
                        onUpdateMood(MoodType.CALM, "Feeling peaceful.")
                    })
                    MoodButton(mood = MoodType.SAD, onClick = {
                        onUpdateMood(MoodType.SAD, "Feeling down.")
                    })
                    MoodButton(mood = MoodType.STRESSED, onClick = {
                        onUpdateMood(MoodType.STRESSED, "Feeling overwhelmed.")
                    })
                    MoodButton(mood = MoodType.ANGRY, onClick = {
                        onUpdateMood(MoodType.ANGRY, "Feeling frustrated.")
                    })
                }
            }
        }
    }
}

@Composable
fun MoodButton(mood: MoodType, onClick: () -> Unit) {
    val moodColor = getMoodColor(mood)
    val moodIcon = getMoodIcon(mood)
    val displayName = mood.name.replace("_", " ").lowercase()
        .replaceFirstChar { it.uppercase() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(moodColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = moodIcon,
                contentDescription = mood.name,
                tint = moodColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = displayName,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp
        )
    }
}

// Helper functions to get color and icon for a mood
fun getMoodColor(mood: MoodType): Color {
    return when (mood) {
        MoodType.VERY_HAPPY, MoodType.HAPPY -> Color(0xFF4CAF50)
        MoodType.CALM -> Color(0xFF2196F3)
        MoodType.NEUTRAL -> Color(0xFF9E9E9E)
        MoodType.SAD, MoodType.VERY_SAD -> Color(0xFF9E9E9E)
        MoodType.STRESSED -> Color(0xFFFF9800)
        MoodType.ANGRY -> Color(0xFFF44336)
        MoodType.EXCITED -> Color(0xFFE91E63)
        MoodType.TIRED -> Color(0xFF673AB7)
    }
}

fun getMoodIcon(mood: MoodType): ImageVector {
    return when (mood) {
        MoodType.VERY_HAPPY, MoodType.HAPPY -> Icons.Default.ThumbUp
        MoodType.CALM -> Icons.Default.Spa
        MoodType.NEUTRAL -> Icons.Default.Face
        MoodType.SAD, MoodType.VERY_SAD -> Icons.Default.SentimentDissatisfied
        MoodType.STRESSED -> Icons.Default.Warning
        MoodType.ANGRY -> Icons.Default.Close
        MoodType.EXCITED -> Icons.Default.Celebration
        MoodType.TIRED -> Icons.Default.NightsStay
    }
}

@Composable
fun TaskCategorySection(
    category: String,
    tasks: List<TaskData>,
    onTaskStatusChanged: (String, Boolean) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val categoryIcon = when (category.lowercase()) {
                "academics" -> Icons.Default.School
                "social" -> Icons.Default.People
                "personal" -> Icons.Default.Person
                "health" -> Icons.Default.Spa
                "work" -> Icons.Default.Work
                else -> Icons.Default.List
            }

            Icon(
                imageVector = categoryIcon,
                contentDescription = category,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )

            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${tasks.size} tasks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        tasks.forEach { task ->
            TaskItem(
                task = task,
                onTaskStatusChanged = onTaskStatusChanged,
                onDeleteTask = onDeleteTask
            )
        }
    }
}

@Composable
fun TaskItem(
    task: TaskData,
    onTaskStatusChanged: (String, Boolean) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { isChecked ->
                        onTaskStatusChanged(task.id, isChecked)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = task.text,
                        style = MaterialTheme.typography.titleSmall,
                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )

                    if (task.dueDate != null) {
                        val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                        Text(
                            text = dateFormat.format(Date(task.dueDate)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Show less" else "Show more"
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onDeleteTask(task.id) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}


@Composable
fun ProgressStat(title: String, count: Int, color: Color, total: Int = 0) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(60.dp)
        ) {
            CircularProgressIndicator(
                progress = if (total > 0) count.toFloat() / total else 0f,
                modifier = Modifier.size(60.dp),
                color = color,
                strokeWidth = 4.dp
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}