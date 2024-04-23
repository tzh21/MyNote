// 主页。注意：主页不是分组（文件夹）页面

// TODO
// 新建笔记按钮

package com.example.mynote

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun HomeScreen(
    navController: NavController
) {
    Column {
        Text("Home Screen")
    }
}