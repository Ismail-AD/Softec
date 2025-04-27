package com.appdev.softec.presentation.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material.icons.filled.TagFaces
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.appdev.softec.domain.model.TaskData
import com.appdev.softec.presentation.feature.Home.HomeScreen
import com.appdev.softec.presentation.feature.Mood.MoodJournalScreen
import com.appdev.softec.presentation.feature.Summarizer.SummarizerScreen
import com.appdev.softec.presentation.feature.setting.CustomizationScreen
import com.appdev.softec.presentation.feature.taskManagement.TaskCreation.CreateTaskScreen
import com.appdev.softec.presentation.feature.taskManagement.TaskCreation.TaskCategory
import com.appdev.softec.presentation.feature.taskManagement.TaskCreation.TaskViewModel
import com.appdev.softec.presentation.feature.taskManagement.TasksList.TaskListScreen
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun MainScreen(onLogout: () -> Unit = {}) {
    val navController = rememberNavController()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val hideBottomBarRoutes = listOf(
        Routes.TaskCreation.route + "/{taskJson}",
        Routes.Settings.route
    )

    // Define navigation items with icons
    val bottomNavItems = listOf(
        BottomNavItem(
            route = Routes.HomePage.route,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = "Home"
        ),
        BottomNavItem(
            route = Routes.TaskList.route,
            icon = { Icon(Icons.Default.Task, contentDescription = "Tasks") },
            label = "Tasks"
        ),
        BottomNavItem(
            route = Routes.MoodJournal.route,
            icon = { Icon(Icons.Default.TagFaces, contentDescription = "Mood") },
            label = "Mood"
        ),
        BottomNavItem(
            route = Routes.Summarize.route,
            icon = {
                Icon(
                    Icons.Default.Summarize,
                    contentDescription = "Summarize"
                )
            },
            label = "Summarizer"
        )
//        BottomNavItem(
//            route = Routes.Settings.route,
//            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
//            label = "Settings"
//        )

    )

    Scaffold(
        bottomBar = {
            if (currentRoute !in hideBottomBarRoutes) {
                BottomNavigation(navController, bottomNavItems)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Routes.HomePage.route,
        ) {


            composable(Routes.TaskList.route) {
                TaskListScreen(onTaskClick = { task ->
                    val taskJson = Uri.encode(Json.encodeToString(task))
                    navController.navigate(Routes.TaskCreation.route + "/$taskJson")

                }, onAddTaskClick = {
                    navController.navigate(Routes.TaskCreation.route + "/{}")
                })
            }

            composable(Routes.TaskCreation.route + "/{taskJson}", arguments = listOf(
                navArgument("taskJson") {
                    type = NavType.StringType
                }
            )) { backStackEntry ->
                val taskViewModel: TaskViewModel = hiltViewModel()
                val taskJson = backStackEntry.arguments?.getString("taskJson") ?: ""
                val decodedProduct = try {
                    Json.decodeFromString<TaskData>(Uri.decode(taskJson))
                } catch (e: Exception) {
                    null
                }
                Log.d("CAZX","${decodedProduct}")
                if (decodedProduct != null) {
                    taskViewModel.initializeWithTask(decodedProduct)
                }
                CreateTaskScreen(onNavigateBack = {
                    navController.navigateUp()
                }, {
                    navController.navigateUp()
                })
            }
            composable(Routes.HomePage.route) {
                HomeScreen(onNavigateToAddTask = {

                }, onNavigateToCalendar = {

                }, onNavigateToSettings = {
                    navController.navigate(Routes.Settings.route)
                }, onNavigateToMoodJournal = {

                })
            }
            composable(Routes.Summarize.route) {
                SummarizerScreen()
            }
            composable(Routes.Settings.route) {
                CustomizationScreen()
            }
            composable(Routes.MoodJournal.route) {
                MoodJournalScreen { }
            }


        }
    }

}


@Composable
fun BottomNavigation(
    navController: NavController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { item.icon() },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }, colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary, // Change icon color when selected
                    selectedTextColor = MaterialTheme.colorScheme.primary, // Change text color when selected
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.inversePrimary,
                    unselectedTextColor = MaterialTheme.colorScheme.inversePrimary,
                )
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: @Composable () -> Unit,
    val label: String
)