package com.appdev.softec.presentation.feature.taskManagement.TasksList

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.appdev.softec.domain.model.TaskData
import com.appdev.softec.presentation.feature.taskManagement.TaskCreation.TaskCategoryGeneral
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onTaskClick: (String) -> Unit,
    onAddTaskClick: () -> Unit,
    viewModel: GetTaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Effect to handle initial data loading
    var showTaskDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<TaskData?>(null) }
    LaunchedEffect(key1 = true) {
        viewModel.fetchTasks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks List") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
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
                // Category filter chips
                CategoryFilterChips(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.updateCategory(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TaskListContent(
                    uiState = uiState,
                    onTaskClick = { task ->
                        // Show the dialog when a task is clicked
                        selectedTask = task
                        showTaskDialog = true
                    },
                    onToggleCompletion = { task ->
                        coroutineScope.launch {
                            viewModel.toggleTaskCompletion(task)
                        }
                    }
                )
            }

            // Task detail dialog
            if (showTaskDialog && selectedTask != null) {
                TaskDetailDialog(
                    task = selectedTask!!,
                    onDismiss = { showTaskDialog = false },
                    onDelete = {
                        // Call your delete task function here
                        coroutineScope.launch {
                            viewModel.deleteTask(selectedTask!!)
                        }
                    },
                    onUpdate = {
                        // Navigate to task creation/update screen
                        // Pass the task ID to the edit screen
                        onTaskClick(selectedTask!!.id)
                    }
                )
            }

            // Error message
            if (uiState.errorMessage.isNotEmpty()) {
                ErrorMessage(
                    message = uiState.errorMessage,
                    onDismiss = { viewModel.clearError() }
                )
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
        }
    }
}

@Composable
fun TaskListContent(
    uiState: TaskUiState,
    onTaskClick: (TaskData) -> Unit,
    onToggleCompletion: (TaskData) -> Unit
) {
    if (uiState.selectedCategory == TaskCategoryGeneral.ALL) {
        // Display all categories as sections
        var hasAnyTasks = false

        uiState.categories
            .filter { it != TaskCategoryGeneral.ALL }
            .forEach { category ->
                val tasksInCategory = uiState.tasks.filter { it.category == category.name }
                if (tasksInCategory.isNotEmpty()) {
                    hasAnyTasks = true
                    CategoryTaskList(
                        categoryName = category.name,
                        tasks = tasksInCategory,
                        onTaskClick = onTaskClick,
                        onToggleCompletion = onToggleCompletion
                    )
                }
            }

        // Show empty state if no tasks across all categories
        if (!hasAnyTasks && !uiState.isLoading) {
            EmptyTasksMessage()
        }
    } else {
        // Display tasks for selected category or show empty state
        val tasksForCategory = uiState.tasks.filter { it.category == uiState.selectedCategory.name }

        if (tasksForCategory.isNotEmpty()) {
            CategoryTaskList(
                categoryName = uiState.selectedCategory.name,
                tasks = tasksForCategory,
                onTaskClick = onTaskClick,
                onToggleCompletion = onToggleCompletion
            )
        } else if (!uiState.isLoading) {
            // Show empty state when no tasks for selected category
            EmptyTasksMessageForCategory(categoryName = uiState.selectedCategory.name)
        }
    }
}

@Composable
fun CategoryTaskList(
    categoryName: String,
    tasks: List<TaskData>,
    onTaskClick: (TaskData) -> Unit,
    onToggleCompletion: (TaskData) -> Unit
) {
    Column {
        Text(
            text = categoryName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            items(
                items = tasks,
                key = { task -> task.id } // Use stable key for items
            ) { task ->
                key(task.id) { // Additional key for more stable recomposition
                    TaskItemOptimized(
                        task = task,
                        onClick = { onTaskClick(task) },
                        onToggleCompletion = { onToggleCompletion(task) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterChips(
    categories: List<TaskCategoryGeneral>,
    selectedCategory: TaskCategoryGeneral,
    onCategorySelected: (TaskCategoryGeneral) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.name) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItemOptimized(
    task: TaskData,
    onClick: () -> Unit,
    onToggleCompletion: () -> Unit
) {
    // Hoisting the completion state out of the recomposition path
    val (completionState, updateCompletionState) = remember(task.id) {
        mutableStateOf(task.isCompleted)
    }

    // Only update when the task's completion status actually changes
    // This prevents recomposition cascades
    SideEffect {
        if (completionState != task.isCompleted) {
            updateCompletionState(task.isCompleted)
        }
    }

    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dueDate = remember(task.dueDate) {
        task.dueDate?.let { dateFormatter.format(Date(it)) } ?: "No due date"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completion checkbox - handled separately from card click
            CompletionCheckbox(
                isCompleted = completionState,
                onToggle = {
                    // Update local state first for immediate UI feedback
                    updateCompletionState(!completionState)
                    // Then notify parent
                    onToggleCompletion()
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Task content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.text,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Due date
                Text(
                    text = dueDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Category chip
            CategoryChip(categoryName = task.category)
        }
    }
}

@Composable
fun CompletionCheckbox(
    isCompleted: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
                if (isCompleted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            )
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun CategoryChip(categoryName: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = categoryName,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun EmptyTasksMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tasks found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create a new task to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun EmptyTasksMessageForCategory(categoryName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No $categoryName tasks found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create a new task in this category to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Snackbar(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        action = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        },
        dismissAction = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(message)
        }
    }
}



@Composable
fun TaskDetailDialog(
    task: TaskData,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with completion status
                CompletionStatusTag(isCompleted = task.isCompleted)

                Spacer(modifier = Modifier.height(16.dp))

                // Task title
                Text(
                    text = task.text,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Task details
                TaskDetailsSection(task)

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Delete button
                    Button(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Task"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remove")
                    }

                    // Update button
                    Button(
                        onClick = {
                            onUpdate()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Task"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dismiss button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun CompletionStatusTag(isCompleted: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isCompleted)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.tertiary,
        contentColor = if (isCompleted)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onTertiary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "COMPLETED",
                    style = MaterialTheme.typography.labelMedium
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Incomplete",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "INCOMPLETE",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun TaskDetailsSection(task: TaskData) {
    val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val createdDate = dateFormatter.format(Date(task.createdAt))
    val dueDate = task.dueDate?.let { dateFormatter.format(Date(it)) } ?: "No due date"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Category
        DetailRow(label = "Category", value = task.category)

        Spacer(modifier = Modifier.height(8.dp))

        // Created date
        DetailRow(label = "Created", value = createdDate)

        Spacer(modifier = Modifier.height(8.dp))

        // Due date
        DetailRow(label = "Due Date", value = dueDate)

        Spacer(modifier = Modifier.height(8.dp))

        // Sync status
        DetailRow(
            label = "Sync Status",
            value = if (task.isSynced) "Synced" else "Not synced"
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}