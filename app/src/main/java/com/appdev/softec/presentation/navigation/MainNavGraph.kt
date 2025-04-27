package com.appdev.softec.presentation.navigation

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
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appdev.softec.presentation.feature.Mood.MoodJournalScreen
import com.appdev.softec.presentation.feature.Summarizer.SummarizerScreen
import com.appdev.softec.presentation.feature.setting.CustomizationScreen
import com.appdev.softec.presentation.feature.taskManagement.TaskCreation.CreateTaskScreen
import com.appdev.softec.presentation.feature.taskManagement.TaskCreation.TaskCategory
import com.appdev.softec.presentation.feature.taskManagement.TasksList.TaskListScreen

@Composable
fun MainScreen(onLogout: () -> Unit = {}) {
    val navController = rememberNavController()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val hideBottomBarRoutes = listOf(
        Routes.TaskCreation.route
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
                TaskListScreen(onTaskClick = {

                }, onAddTaskClick = {
                    navController.navigate(Routes.TaskCreation.route)
                })
            }

            composable(Routes.TaskCreation.route) {
                CreateTaskScreen(onNavigateBack = {
                    navController.navigateUp()
                }, {
                    navController.navigateUp()
                })
            }
            composable(Routes.HomePage.route) {
                HomePage()
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
fun HomePage() {

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