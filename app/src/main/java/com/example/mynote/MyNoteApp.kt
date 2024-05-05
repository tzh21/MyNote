package com.example.mynote

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun MyNoteApp(navController: NavHostController = rememberNavController()) {
    MyNoteNavHost(navController = navController)
}