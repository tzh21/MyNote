package com.example.mynote.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.placeholder
import com.example.mynote.data.getCurrentTime
import com.example.mynote.ui.component.MyNoteTopBar
import com.example.mynote.ui.theme.LightColorScheme
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

@OptIn(ExperimentalMaterial3Api::class)
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
    viewModel.initialNoteList()
    viewModel.initialQueryNoteList()

//    从数据库中加载笔记列表
    val noteListState by viewModel.noteListStateFlow.collectAsState()
    Log.d("HomeScreen", "size: ${noteListState.noteList.size}")

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(text = "笔记", modifier = Modifier.padding(start = 16.dp)) },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.download(context)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = LightColorScheme.primary
                            )
                        }

                        IconButton(onClick = { navigateToCategory() }) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Category",
                                tint = LightColorScheme.primary
                            )
                        }

                        IconButton(onClick = { navigateToLogin() }) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = LightColorScheme.primary
                            )
                        }

                        IconButton(onClick = {
                            coroutineScope.launch {
                                viewModel.deleteAllFiles(context)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = LightColorScheme.error
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
//                    这里采用创建时间作为文件名（这种设计要求两次创建间隔超过 1s）
                    coroutineScope.launch {
                        val currentTime = getCurrentTime()
                        viewModel.createNote(
                            currentTime,
                            context
                        )
                        navigateToEditorScreen(currentTime)
                    }
                },
                modifier = Modifier.padding(bottom = 32.dp, end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Note"
                )
            }
        }
    ) {
        val focusManager = LocalFocusManager.current
        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                    }
                }
                .padding(it)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
//                搜索框
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = Color(237, 237, 237),
                            shape = RoundedCornerShape(percent = 50)
                        )
                        .height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    BasicTextField(
                        value = viewModel.queryText.value,
                        onValueChange = { newValue ->
                            viewModel.queryText.value = newValue
                            viewModel.setQueryNoteList(newValue)
                                        },
                        decorationBox = { innerTextField ->
                            if (viewModel.queryText.value.isEmpty()) {
                                Text(
                                    text = "搜索",
                                    style = TextStyle(
                                        color = Color.Gray,
                                        fontSize = 16.sp
                                    ),
                                )
                            }
                            innerTextField()
                        },
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 16.sp
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp)
                            .weight(5f)
                            .onFocusChanged { focusState ->
                                viewModel.isQueryFocused.value = focusState.isFocused
                            }
                    )
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (viewModel.queryText.value.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.queryText.value = "" },
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }

//                搜索结果
                if (viewModel.isQueryFocused.value) {
                    val queryNoteList by viewModel.queryNoteListStateFlow.collectAsState()
                    LazyColumn {
                        items(queryNoteList.noteList.size) { index ->
                            Card {
                                Text(queryNoteList.noteList[index].title)
                            }
                        }
                    }
                }

//                viewModel.initCategoryList(context)
//
//                var selectedIndex by rememberSavable {
//                    mutableStateOf(0)
//                }
//
//                LazyRow {
//                    items(viewModel.categoryList.size) { index ->
//                        FilterChip(
//                            onClick = {selectedIndex = index},
//                            label = {
//                                Text(text = viewModel.categoryList[index])
//                            },
//                            selected = (index == selectedIndex)
//                        )
//                    }
//                }

                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(180.dp)
                ) {
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

//                LazyColumn {
//                    items(noteListState.noteList.size) { index ->
//                        Card(
//                            modifier = Modifier.clickable {
//                                navigateToEditorScreen(noteListState.noteList[index].fileName)
//                            }
//                        ) {
//                            val noteTitle = noteListState.noteList[index].title
//                            if (noteTitle == "") {
//                                Text("未命名")
//                            } else {
//                                Text(noteTitle)
//                            }
//                        }
//                    }
//                }
            }
        }
    }

    if (viewModel.showSyncDialog.value) {
        Dialog(onDismissRequest = {  }) {
            Card {
                Text("正在下载数据...")
            }
        }
    }
}

@Preview
@Composable
fun SearchField() {
    var text by remember { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color = Color(237, 237, 237),
                shape = RoundedCornerShape(percent = 50)
            )
            .height(40.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            modifier = Modifier.padding(start = 12.dp)
        )
        BasicTextField(
            value = text,
            onValueChange = {text = it},
            modifier = Modifier
                .fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Text(
                        text = "搜索",
                        style = TextStyle(
                            color = Color.Gray,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        )
        if (text.isNotEmpty()) {
            IconButton(
                onClick = { text = "" },
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Gray
                )
            }
        }
    }
}
