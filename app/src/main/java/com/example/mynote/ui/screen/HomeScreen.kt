package com.example.mynote.ui.screen

import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynote.data.NoteEntity
import com.example.mynote.data.getCurrentTime
import com.example.mynote.data.simplifyTime
import com.example.mynote.ui.theme.Typography
import com.example.mynote.ui.viewmodel.AppViewModelProvider
import com.example.mynote.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data object HomeRoute {
    const val base = "home"
    const val username = "user"
    const val category = "category"
    const val defaultCategory = "default"
    const val complete = "$base/{$username}/{$category}"
}

//nfs:
//HomeScreen 直接从上级接收的 List<NoteEntity>，不再从 ViewModel 中获取，这样可以避免重复加载
//相应地，HomeScreen 需要从上级接收修改笔记列表的函数，以便在增加或删除笔记时更新列表
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navigateToCategory: () -> Unit,
    navigateToEditor: (String) -> Unit,
    navigateToProfile: () -> Unit,
    navigateToHome: (String) -> Unit,
    username: String,
    category: String,
    categoryList: List<String>,
    noteList: List<NoteEntity>,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current

    viewModel.username = username
    viewModel.category = category

    var showDeleteDialog by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(text = "笔记", modifier = Modifier.padding(start = 16.dp)) },
                navigationIcon = {
//                    前往用户信息界面
                    Row {
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(
                            onClick = { navigateToProfile() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
//                        前往分类界面
                    IconButton(onClick = { navigateToCategory() }) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Category",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

//                        多选
//                    IconButton(onClick = { }) {
//                        Icon(
//                            imageVector = Icons.Default.CheckCircleOutline,
//                            contentDescription = "Multiselect",
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }

//                    云同步
                    Box {
                        val expandedSyncMenu = remember {mutableStateOf(false)}

                        IconButton(onClick = {
                            expandedSyncMenu.value = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = "Cloud sync",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = expandedSyncMenu.value,
                            onDismissRequest = { expandedSyncMenu.value = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "上传全部笔记") },
                                onClick = {
                                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                                        viewModel.uploadAll(context)
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(text = "下载全部笔记") },
                                onClick = {
                                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                                        viewModel.downloadAll(context)
                                    }
                                }
                            )
                        }
                    }

                    Box {
                        var deleteExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = {
                            deleteExpanded = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        DropdownMenu(
                            expanded = deleteExpanded,
                            onDismissRequest = { deleteExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "在本地删除全部笔记") },
                                onClick = {
                                    deleteExpanded = false
                                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                                        viewModel.deleteAllNotes(context)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = "在本地和云端删除全部笔记") },
                                onClick = {
                                    deleteExpanded = false
                                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                                        viewModel.deleteAllNotes(context)
                                        viewModel.deleteRemoteAllNotes()
                                    }
                                })
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        },
//        新建笔记
        floatingActionButton = {
            FloatingActionButton(onClick = {
//                    这里采用创建时间作为文件名（这种设计要求两次创建间隔超过 1s）
                    val noteFileName = getCurrentTime()
                    navigateToEditor(noteFileName)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp, end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Note"
                )
            }
        }
    ) { scaffoldPadding ->
        val focusManager = LocalFocusManager.current
        Box(
            modifier = Modifier
                .padding(scaffoldPadding)
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
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
                        value = viewModel.queryText,
                        onValueChange = { newValue ->
                            viewModel.queryText = newValue
                            viewModel.updateQueryResults()
                                        },
                        decorationBox = { innerTextField ->
                            if (viewModel.queryText.isEmpty()) {
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
                                viewModel.isQueryFocused = focusState.isFocused
                            }
                    )
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (viewModel.queryText.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.queryText = "" },
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
                if (viewModel.isQueryFocused && viewModel.queryText.isNotEmpty()) {
                    val queryNoteList by viewModel.queryResultsStateFlow.collectAsState()
                    viewModel.queryMode = queryNoteList.isNotEmpty()
                    if (viewModel.queryMode) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val orientation = LocalConfiguration.current.orientation
//                        检索结果列表
                        LazyVerticalStaggeredGrid(
                            columns = if (orientation == Configuration.ORIENTATION_PORTRAIT) StaggeredGridCells.Fixed(2)
                                else StaggeredGridCells.Adaptive(160.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalItemSpacing = 8.dp
                        ) {
                            val highlightColor = Color.Red

                            items(queryNoteList.size) { index ->
                                var showNoteOption by remember {
                                    mutableStateOf(false)
                                }
                                Box {
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onTap = { navigateToEditor(queryNoteList[index].fileName) },
                                                    onLongPress = { showNoteOption = true }
                                                )
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(12.dp)
                                        ) {
                                            val noteTitle = queryNoteList[index].title
                                            val annotatedTitle = buildAnnotatedString {
                                                val startIndex = noteTitle.indexOf(viewModel.queryText)
                                                if (startIndex != -1) {
                                                    append(noteTitle.substring(0, startIndex))
                                                    withStyle(style = SpanStyle(color = highlightColor)) {
                                                        append(viewModel.queryText)
                                                    }
                                                    append(noteTitle.substring(startIndex + viewModel.queryText.length))
                                                } else {
                                                    append(noteTitle)
                                                }
                                            }
                                            val annotatedKeyword = buildAnnotatedString {
                                                val contextSize = 50
                                                val matchIndex = queryNoteList[index].keyword.indexOf(viewModel.queryText)
                                                val start = maxOf(0, matchIndex - contextSize)
                                                val end = minOf(queryNoteList[index].keyword.length, matchIndex + viewModel.queryText.length + contextSize)
                                                if (matchIndex != -1) {
                                                    append(queryNoteList[index].keyword.substring(start, matchIndex))
                                                    withStyle(style = SpanStyle(color = highlightColor)) {
                                                        append(viewModel.queryText)
                                                    }
                                                    append(queryNoteList[index].keyword.substring(matchIndex + viewModel.queryText.length, end))
                                                } else {
                                                    append(queryNoteList[index].keyword)
                                                }
                                            }
                                            Text(
                                                text = if (noteTitle == "") buildAnnotatedString{append("未命名")} else annotatedTitle,
                                                fontSize = Typography.titleLarge.fontSize,
                                            )
                                            Text(
                                                text = "上次修改：${simplifyTime(queryNoteList[index].lastModifiedTime)}",
                                                fontSize = Typography.bodyMedium.fontSize,
                                                color = Color.Gray
                                            )
                                            Text(text = annotatedKeyword)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    viewModel.queryMode = false
                }
                if (!viewModel.queryMode) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categoryList.size) { index ->
                            FilterChip(
                                onClick = { navigateToHome(categoryList[index]) },
                                label = {
                                    Text(text = categoryList[index])
                                },
                                selected = (categoryList[index] == category),
                                border = null,
                                modifier = Modifier
                                    .height(48.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val orientation = LocalConfiguration.current.orientation
    //                笔记列表
                    LazyVerticalStaggeredGrid(
                        columns = if (orientation == Configuration.ORIENTATION_PORTRAIT) StaggeredGridCells.Fixed(2)
                            else StaggeredGridCells.Adaptive(160.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp
                    ) {
                        items(noteList.size) { index ->
                            var showNoteOption by remember {
                                mutableStateOf(false)
                            }
    //                        笔记条目
                            Box {
                                Card(
                                    onClick = {
                                        Log.d("focus", "${noteList[index].title} ${noteList[index].fileName}")
                                        navigateToEditor(noteList[index].fileName)
                                              },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Column {
    //                                    封面图片
                                        if (noteList[index].coverImage != "") {
                                            val imageFileName = noteList[index].coverImage
                                            val file = viewModel.loadImageFile(imageFileName, context)
                                            if (file.exists()) {
                                                val bitmap = BitmapFactory.decodeFile(file.path)
                                                if (bitmap != null) {
                                                    Image(
                                                        bitmap = bitmap.asImageBitmap(),
                                                        contentDescription = "Cover image",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .heightIn(max = 200.dp)
                                                    )
                                                }
                                            }
                                        }
    //                                    笔记信息
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(12.dp)
                                        ) {
                                            val noteTitle = noteList[index].title
                                            Text(
                                                text = if (noteTitle == "") "未命名" else noteTitle,
                                                fontSize = Typography.titleLarge.fontSize,
                                            )
                                            Text(
                                                text = "上次修改：${simplifyTime(noteList[index].lastModifiedTime)}",
                                                fontSize = Typography.bodyMedium.fontSize,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                                DropdownMenu(
                                    expanded = showNoteOption,
                                    onDismissRequest = { showNoteOption = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("删除") },
                                        onClick = {
                                            viewModel.viewModelScope.launch(Dispatchers.IO) {
                                                viewModel.deleteNote(noteList[index].fileName, context)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                icon = { Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error
                )},
                shape = RectangleShape,
                title = { Text(text = "删除全部笔记") },
                text = { Text(text = "确定要删除全部笔记吗？") },
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.viewModelScope.launch(Dispatchers.IO) {
                                viewModel.deleteAllNotes(context)
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(text = "确定")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text(text = "取消")
                    }
                }
            )
        }
    }
}
