// 主页。注意：主页不是分组（文件夹）页面

package com.example.mynote.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

const val HomeRoute = "home"

@Composable
fun HomeScreen(
    navigateToCategory: () -> Unit,
    navigateToEditorScreen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column {
        Text("Home Screen")
    }
}