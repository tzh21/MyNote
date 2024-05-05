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
import com.example.mynote.ui.screen.EditorArg
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
                navigateToHome = { navController.navigate(HomeRoute) },
                navigateToSignup = { navController.navigate(SignupRoute) }
            )
        }
        composable(route = SignupRoute) {
            SignupScreen(
                navigateToHome = { navController.navigate(HomeRoute) }
            )
        }
        composable(route = HomeRoute) {
            HomeScreen(
                navigateToCategory = { navController.navigate(CategoryRoute) },
                navigateToEditorScreen = { noteId ->
                    navController.navigate("$EditorRoute/$noteId")
                }
            )
        }
        composable(route = CategoryRoute) {
            CategoryScreen()
        }
        composable(
            route = EditorRoute,
            arguments = listOf(navArgument(EditorArg) {
                type = NavType.StringType
            })
        ) {
            EditorScreen()
        }
    }
}