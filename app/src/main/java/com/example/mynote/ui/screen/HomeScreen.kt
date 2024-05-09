package com.example.mynote.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynote.data.LocalFileApi
import com.example.mynote.data.getCurrentTime
import com.example.mynote.ui.component.FileList
import com.example.mynote.ui.component.MyNoteTopBar
import com.example.mynote.ui.viewmodel.AppViewModelProvider
import com.example.mynote.ui.viewmodel.HomeViewModel

data object HomeRoute {
    const val base = "home"
    const val username = "user"
    const val category = "category"
    const val defaultCategory = "default"
    const val complete = "$base/{$username}/{$category}"
}

@Composable
fun HomeScreen(
    navigateToCategory: () -> Unit,
    navigateToEditorScreen: (String) -> Unit,
    navigateToLogin: () -> Unit,
    username: String,
    category: String,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current

    viewModel.setUserName(username)
    viewModel.setCategory(category)
    viewModel.setFiles(context)

    val files = viewModel.files

    Scaffold(
        topBar = { MyNoteTopBar(
            title = "Home Screen",
            canNavigateBack = false
        )},
        floatingActionButton = {
            FloatingActionButton(onClick = {
//                这里采用创建时间作为文件名（这种设计要求两次创建间隔超过 1s）
                val currentTime = getCurrentTime()
                LocalFileApi.createNote("$username/$category", currentTime, context)
                navigateToEditorScreen(currentTime)
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Note"
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            Text("用户名：$username")
            Text("分类：$category")

            Button(onClick = {
                navigateToCategory()
            }) {
                Text("前往分类页面")
            }

            Button(onClick = {
                viewModel.deleteAllFiles(context)
            }) {
                Text("全部删除")
            }

            Button(onClick = {
                navigateToLogin()
            }) {
                Text("退出登录")
            }

            LazyColumn {
                items(files.size) { index ->
                    Card(
                        modifier = Modifier.clickable {
                            navigateToEditorScreen(files[index])
                        }
                    ) {
                        Text(files[index])
                    }
                }
            }
        }
    }
}