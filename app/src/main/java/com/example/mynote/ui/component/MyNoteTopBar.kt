package com.example.mynote.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyNoteTopBar(
    title: String,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            if (canNavigateBack) {
                 IconButton(onClick = navigateUp) {
                     Icon(
                         imageVector = Icons.Filled.ArrowBack,
                         contentDescription = "Back"
                     )
                 }
            }
        }
    )
}