package com.example.mynote

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mynote.ui.screen.CategoryRoute
import com.example.mynote.ui.screen.CategoryScreen
import com.example.mynote.ui.screen.EditorRoute
import com.example.mynote.ui.screen.EditorScreen
import com.example.mynote.ui.screen.HomeRoute
import com.example.mynote.ui.screen.HomeScreen
import com.example.mynote.ui.screen.LoginRoute
import com.example.mynote.ui.screen.LoginScreen
import com.example.mynote.ui.screen.ProfileRoute
import com.example.mynote.ui.screen.ProfileScreen
import com.example.mynote.ui.screen.SignupRoute
import com.example.mynote.ui.screen.SignupScreen
import com.example.mynote.ui.viewmodel.AppViewModelProvider

@Composable
fun MyNoteApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: MyNoteViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    LaunchedEffect(viewModel.username) {
        if (viewModel.username != null) {
            viewModel.loadCategoryList(username = viewModel.username!!)
            viewModel.loadCategoryNotesMap(username = viewModel.username!!)
        }
    }

    val categoryList by viewModel.categoryList.collectAsState()
    val categoryNotesMap by viewModel.categoryNotesMap.collectAsState()

    NavHost(
        navController = navController,
        startDestination = LoginRoute,
        modifier = modifier
    ) {
        composable(route = LoginRoute) {
            LoginScreen(
                navigateToHome = { loginUsername ->
                    viewModel.username = loginUsername
                    navController.navigate("${HomeRoute.base}/$loginUsername/${HomeRoute.defaultCategory}") },
                navigateToSignup = { navController.navigate(SignupRoute) },
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
            val username = it.arguments!!.getString(HomeRoute.username)
            val category = it.arguments?.getString(HomeRoute.category) ?: HomeRoute.defaultCategory
            HomeScreen(
                navigateToCategory = { navController.navigate("${CategoryRoute.base}/$username/$category") },
                navigateToEditor = { noteTitle ->
                    navController.navigate("${EditorRoute.base}/$username/$category/$noteTitle")},
                navigateToProfile = { navController.navigate("${ProfileRoute.base}/$username") },
                navigateToHome = { newCategory -> navController.navigate("${HomeRoute.base}/$username/$newCategory") },
                username = username!!,
                category = category,
                categoryList = categoryList,
                noteList = categoryNotesMap[category] ?: emptyList()
            )
        }

        composable(
            route = CategoryRoute.complete,
            arguments = listOf(
                navArgument(CategoryRoute.username) {type = NavType.StringType},
                navArgument(CategoryRoute.currentCategory) {type = NavType.StringType}
            )
        ) {
            val username = it.arguments?.getString(CategoryRoute.username) ?: "null"
            val currentCategory = it.arguments?.getString(CategoryRoute.currentCategory) ?: HomeRoute.defaultCategory
            CategoryScreen(
                navigateToHome = { category ->
                    navController.navigate("${HomeRoute.base}/$username/$category") },
                username = username,
                currentCategory = currentCategory
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
                navigateToHome = { navController.navigate("${HomeRoute.base}/$username/$category") },
                navigateToEditor = { newCategory, newFileName  ->
                    navController.navigate("${EditorRoute.base}/$username/$newCategory/$newFileName") },
                username = username,
                category = category,
                fileName = noteTitle,
            )
        }

        composable(
            route = ProfileRoute.complete,
            arguments = listOf(
                navArgument(ProfileRoute.username) {type = NavType.StringType}
            )
        ) {
            val username = it.arguments?.getString(ProfileRoute.username) ?: "null"
            ProfileScreen(
                navigateToHome = { navController.navigate("${HomeRoute.base}/$username/${HomeRoute.defaultCategory}") },
                navigateToLogin = { navController.navigate(LoginRoute) },
                username = username,
            )
        }

    }
}