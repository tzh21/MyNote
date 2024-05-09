package com.example.mynote

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mynote.ui.screen.CategoryRoute
import com.example.mynote.ui.screen.CategoryScreen
import com.example.mynote.ui.screen.EditorRoute
import com.example.mynote.ui.screen.EditorScreen
import com.example.mynote.ui.screen.HomeRoute
import com.example.mynote.ui.screen.HomeScreen
import com.example.mynote.ui.screen.LoginRoute
import com.example.mynote.ui.screen.LoginScreen
import com.example.mynote.ui.screen.SignupRoute
import com.example.mynote.ui.screen.SignupScreen

@Composable
fun MyNoteNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = LoginRoute,
        modifier = modifier
    ) {
        composable(route = LoginRoute) {
            LoginScreen(
                navigateToHome = { navController.navigate("${HomeRoute.base}/$it/${HomeRoute.defaultCategory}") },
                navigateToSignup = { navController.navigate(SignupRoute) }
            )
        }

        composable(route = SignupRoute) {
            SignupScreen(
                navigateToHome = { navController.navigate("${HomeRoute.base}/$it/${HomeRoute.defaultCategory}") }
            )
        }

        composable(
            route = HomeRoute.complete,
            arguments = listOf(
                navArgument(HomeRoute.username) { type = NavType.StringType },
                navArgument(HomeRoute.category) { type = NavType.StringType }
            )
        ) {
            val username = it.arguments?.getString(HomeRoute.username) ?: "null"
            val category = it.arguments?.getString(HomeRoute.category) ?: HomeRoute.defaultCategory
            HomeScreen(
                navigateToCategory = { navController.navigate("${CategoryRoute.base}/$username") },
                navigateToEditorScreen = { noteTitle ->
                    navController.navigate("${EditorRoute.base}/$username/$category/$noteTitle")},
                navigateToLogin = { navController.navigate(LoginRoute) },
                username = username,
                category = category,
            )
        }

        composable(
            route = CategoryRoute.complete,
            arguments = listOf(
                navArgument(CategoryRoute.username) {type = NavType.StringType}
            )
        ) {
            val username = it.arguments?.getString(CategoryRoute.username) ?: "null"
            CategoryScreen(
                navigateToHome = { category ->
                    navController.navigate("${HomeRoute.base}/$username/$category") },
                username = username,
            )
        }

        composable(
            route = EditorRoute.complete,
            arguments = listOf(
                navArgument(EditorRoute.username) {type = NavType.StringType},
                navArgument(EditorRoute.category) {type = NavType.StringType},
                navArgument(EditorRoute.noteTitle) {type = NavType.StringType},
            )
        ) {
            val username = it.arguments?.getString(EditorRoute.username) ?: "null"
            val category = it.arguments?.getString(EditorRoute.category) ?: HomeRoute.defaultCategory
            val noteTitle = it.arguments?.getString(EditorRoute.noteTitle) ?: "null"
            EditorScreen(
                navigateUp = { navController.navigateUp() },
                username = username,
                category = category,
                fileName = noteTitle,
            )
        }
    }
}