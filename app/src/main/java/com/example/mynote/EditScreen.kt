package com.example.mynote

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun EditScreen(
    navController: NavController,
    noteID: String
) {
    Column {
        Text("$noteID")
    }
}