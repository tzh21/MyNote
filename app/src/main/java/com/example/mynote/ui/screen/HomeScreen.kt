package com.example.mynote.ui.screen

import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynote.data.getCurrentTime
import com.example.mynote.ui.component.MyNoteTopBar
import com.example.mynote.ui.viewmodel.AppViewModelProvider
import com.example.mynote.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()

//    设置用户名和分类
    viewModel.username.value = username
    viewModel.category.value = category
    viewModel.setNoteList(username, category)

//    从数据库中加载笔记列表
    val noteListState by viewModel.noteListUiState.collectAsState()
    Log.d("HomeScreen", "size: ${noteListState.noteList.size}")

    Scaffold(
        topBar = { MyNoteTopBar(
            title = "Home Screen",
            canNavigateBack = false
        )},
        floatingActionButton = {
            FloatingActionButton(onClick = {
//                这里采用创建时间作为文件名（这种设计要求两次创建间隔超过 1s）
                coroutineScope.launch {
                    val currentTime = getCurrentTime()
                    viewModel.createNote(
                        currentTime,
                        context
                    )
                    navigateToEditorScreen(currentTime)
                }
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
            Text("用户名：${viewModel.username.value}")
            Text("分类：${viewModel.category.value}")

            Button(onClick = {
                coroutineScope.launch {
                    viewModel.download(context)
                }
            }) {
                Text("下载")
            }

            Button(onClick = {
                navigateToCategory()
            }) {
                Text("前往分类页面")
            }

            Button(onClick = {
                coroutineScope.launch {
                    viewModel.deleteAllFiles(context)
                }
            }) {
                Text("全部删除")
            }

            Button(onClick = {
                navigateToLogin()
            }) {
                Text("退出登录")
            }

            LazyColumn {
                items(noteListState.noteList.size) { index ->
                    Card(
                        modifier = Modifier.clickable {
                            navigateToEditorScreen(noteListState.noteList[index].fileName)
                        }
                    ) {
                        val noteTitle = noteListState.noteList[index].title
                        if (noteTitle == "") {
                            Text("未命名")
                        } else {
                            Text(noteTitle)
                        }
                    }
                }
            }
        }

        if (viewModel.showSyncDialog.value) {
            Dialog(onDismissRequest = {  }) {
                Card {
                    Text("正在同步数据...")
                }
            }
        }
    }
}