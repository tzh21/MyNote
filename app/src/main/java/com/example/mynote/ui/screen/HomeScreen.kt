package com.example.mynote.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mynote.data.getCurrentTime
import com.example.mynote.ui.component.MyNoteTopBar
import com.example.mynote.ui.viewmodel.AppViewModelProvider
import com.example.mynote.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import coil.compose.rememberImagePainter
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

//    从数据库中加载笔记列表
    val noteListState by viewModel.noteListUiState.collectAsState()

    var multiSelectEnabled by remember { mutableStateOf(false) }
    val selectedNotes = remember { mutableSetOf<String>() }

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
            Text("用户名：$username")
            Text("分类：$category")

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

            Switch(
                checked = multiSelectEnabled,
                onCheckedChange = {
                    multiSelectEnabled = it
                    selectedNotes.clear() // Clear selections when toggling mode
                }
            )

            LazyColumn {
                items(noteListState.noteList.size) { index ->
                    var showMenu by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { navigateToEditorScreen(noteListState.noteList[index].fileName) }
                            .pointerInput(noteListState.noteList[index].fileName) {
                                detectTapGestures(
                                    onLongPress = {
                                        // Show the menu on long press
                                        showMenu = true
                                    }
                                )
                            }
                    ) {
                        val noteTitle = noteListState.noteList[index].title
                        val noteImage = noteListState.noteList[index].coverImage
                        val noteTime = noteListState.noteList[index].lastModifiedTime
                        val noteKeyword = noteListState.noteList[index].keyword

                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Column(modifier = Modifier.weight(7f)) {
                                // title
                                Text(
                                    text = if (noteTitle.isEmpty()) "未命名" else noteTitle,
                                    modifier = Modifier
                                        .align(Alignment.Start)
                                        .padding(bottom = 8.dp) // 在标题和时间之间添加一点垂直间距
                                )
                                // time
                                Text(text = noteTime, modifier = Modifier.align(Alignment.Start))

                                // keyword
                                if (noteKeyword.isNotEmpty()) {
                                    Text(text = noteKeyword, modifier = Modifier.align(Alignment.End))
                                }
                            }

                            if (noteImage.isEmpty()) {
                                AsyncImage(
                                    model = noteImage,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .weight(3f) // 占比3
                                        .size(50.dp)
                                        .align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }

                    // DropdownMenu for long-press options
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {  Text("Edit") },
                            onClick = {
                                showMenu = false
                                // Perform edit operation
                                navigateToEditorScreen(noteListState.noteList[index].fileName)
                            }
                        )
                        DropdownMenuItem(
                            text = {  Text("Delete") },
                            onClick = {
                                showMenu = false
                                // Perform edit operation
                                // TODO
                            }
                        )
                    }
                }
            }
        }
    }
}