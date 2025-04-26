package com.appdev.softec.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appdev.softec.presentation.feature.auth.login.LoginScreen
import com.appdev.softec.presentation.feature.auth.signup.SignUpScreen

@Composable
fun NavGraph(userId: String) {
    val controller = rememberNavController()
    val initialRoute = if (userId.trim().isNotEmpty()) {
        Routes.Dashboard.route
    } else {
        Routes.Login.route
    }

    NavHost(
        navController = controller,
        startDestination = initialRoute
    ) {
        composable(route = Routes.Login.route) {
            LoginScreen(navigateToHome = {
                controller.navigate(Routes.Dashboard.route)

            }) {
                controller.navigate(Routes.Register.route)
            }
        }
        composable(route = Routes.Register.route) {
            SignUpScreen(navigateToLogin = {
                controller.navigate(Routes.Login.route)
            }) {
                controller.navigate(Routes.Dashboard.route)
            }
        }
        composable(route = Routes.Dashboard.route) {
            MainScreen()
        }
    }
}