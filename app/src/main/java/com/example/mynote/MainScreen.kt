package com.example.mynote

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                navController = navController,
                onLoginTriggered = {
                    navController.navigate("home")
                }
            )
        }
        composable("signup") {
            SignupScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
//        composable("note/{id}") {backStackEntry ->
//            val id = backStackEntry.arguments?.getString("id")
//            EditorScreen(navController = navController, noteID = id ?: "none")
//        }
    }
}