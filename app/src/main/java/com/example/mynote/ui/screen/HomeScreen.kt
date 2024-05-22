package com.example.mynote.ui.screen

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.mynote.data.LocalNoteFileApi
import com.example.mynote.data.NoteEntity
import com.example.mynote.data.getCurrentTime
import com.example.mynote.ui.theme.Typography
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

//nfs:
//HomeScreen 直接从上级接收的 List<NoteEntity>，不再从 ViewModel 中获取，这样可以避免重复加载
//相应地，HomeScreen 需要从上级接收修改笔记列表的函数，以便在增加或删除笔记时更新列表
@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun HomeScreen(
    navigateToCategory: () -> Unit,
    navigateToEditorScreen: (String) -> Unit,
    navigateToProfile: () -> Unit,
    navigateToHome: (String) -> Unit,
    username: String,
    category: String,
    noteList: List<NoteEntity>,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(username) {
        viewModel.username = username
    }

    LaunchedEffect(category) {
        viewModel.category = category
    }

    Scaffold { scaffoldPadding ->
        Column(
            modifier = Modifier.padding(scaffoldPadding)
        ) {
            Text(text = username)
            Text(text = category)
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.newCreateNote(context)
                    }
                }
            ) {
                Text(text = "添加文件")
            }
            Button(onClick = {
                coroutineScope.launch {
                    viewModel.newDeleteAllFiles(context)
                }
            }) {
                Text(text = "删除所有文件")
            }
            LazyColumn {
                items(noteList.size) {
                    Text(text = noteList[it].fileName)
                }
            }
        }
    }

}

//@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
//@Composable
//fun HomeScreen(
//    navigateToCategory: () -> Unit,
//    navigateToEditorScreen: (String) -> Unit,
//    navigateToProfile: () -> Unit,
//    navigateToHome: (String) -> Unit,
//    username: String,
//    category: String,
//    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
//) {
//    Log.d("HomeScreen", "Recomposition")
//
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//
////    设置用户名和分类
//    viewModel.username.value = username
//    viewModel.category.value = category
//    viewModel.initialNoteList()
//    viewModel.initialQueryNoteList()
//
////    从数据库中加载笔记列表
//    val noteListState by viewModel.noteListStateFlow.collectAsState()
//
//    Scaffold(
//        topBar = {
//            MediumTopAppBar(
//                title = { Text(text = "笔记", modifier = Modifier.padding(start = 16.dp)) },
//                navigationIcon = {
//                    Row(
//                        modifier = Modifier.padding(start = 8.dp)
//                    ) {
//                        IconButton(
//                            onClick = {
//                                navigateToProfile()
//                            }
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Person,
//                                contentDescription = "User information",
//                                tint = MaterialTheme.colorScheme.primary
//                            )
//                        }
//                    }
//                },
//                actions = {
//                    Row(
//                        modifier = Modifier.padding(end = 8.dp)
//                    ) {
//                        Box {
//                            val showSyncMenu = remember {
//                                mutableStateOf(false)
//                            }
//
//                            IconButton(onClick = {
//                                showSyncMenu.value = true
//                            }) {
//                                Icon(
//                                    imageVector = Icons.Default.Cloud,
//                                    contentDescription = "Cloud sync",
//                                    tint = MaterialTheme.colorScheme.primary
//                                )
//                            }
//
//                            DropdownMenu(
//                                expanded = showSyncMenu.value,
//                                onDismissRequest = { showSyncMenu.value = false }
//                            ) {
//                                DropdownMenuItem(
//                                    text = { Text(text = "上传全部笔记") },
//                                    onClick = {
//                                        coroutineScope.launch {
//                                            viewModel.uploadAll(context)
//                                        }
//                                    }
//                                )
//
//                                DropdownMenuItem(
//                                    text = { Text(text = "下载全部笔记") },
//                                    onClick = {
//                                        coroutineScope.launch {
//                                            viewModel.download(context)
//                                        }
//                                    }
//                                )
//                            }
//                        }
//
//                        IconButton(onClick = { navigateToCategory() }) {
//                            Icon(
//                                imageVector = Icons.Default.Folder,
//                                contentDescription = "Category",
//                                tint = MaterialTheme.colorScheme.primary
//                            )
//                        }
//
//                        IconButton(onClick = { }) {
//                            Icon(
//                                imageVector = Icons.Default.CheckCircleOutline,
//                                contentDescription = "Multiselect",
//                                tint = MaterialTheme.colorScheme.primary
//                            )
//                        }
//
//                        IconButton(onClick = {
//                            coroutineScope.launch {
//                                viewModel.deleteAllFiles(context)
//                            }
//                        }) {
//                            Icon(
//                                imageVector = Icons.Default.Delete,
//                                contentDescription = "Delete",
//                                tint = MaterialTheme.colorScheme.error
//                            )
//                        }
//                    }
//                }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(onClick = {
////                    这里采用创建时间作为文件名（这种设计要求两次创建间隔超过 1s）
//                    coroutineScope.launch {
//                        val currentTime = getCurrentTime()
//                        viewModel.createNote(
//                            currentTime,
//                            context
//                        )
//                        navigateToEditorScreen(currentTime)
//                    }
//                },
//                containerColor = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.padding(bottom = 32.dp, end = 16.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = "Add Note"
//                )
//            }
//        }
//    ) {
//        val focusManager = LocalFocusManager.current
//        Box(
//            modifier = Modifier
//                .pointerInput(Unit) {
//                    detectTapGestures {
//                        focusManager.clearFocus()
//                    }
//                }
//                .padding(it)
//        ) {
//            Column(
//                modifier = Modifier
//                    .padding(horizontal = 16.dp)
//                    .fillMaxSize()
//            ) {
////                搜索框
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .background(
//                            color = Color(237, 237, 237),
//                            shape = RoundedCornerShape(percent = 50)
//                        )
//                        .height(40.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Search,
//                        contentDescription = "Search",
//                        tint = Color.Gray,
//                        modifier = Modifier.padding(start = 12.dp)
//                    )
//                    BasicTextField(
//                        value = viewModel.queryText.value,
//                        onValueChange = { newValue ->
//                            viewModel.queryText.value = newValue
//                            viewModel.setQueryNoteList(newValue)
//                                        },
//                        decorationBox = { innerTextField ->
//                            if (viewModel.queryText.value.isEmpty()) {
//                                Text(
//                                    text = "搜索",
//                                    style = TextStyle(
//                                        color = Color.Gray,
//                                        fontSize = 16.sp
//                                    ),
//                                )
//                            }
//                            innerTextField()
//                        },
//                        textStyle = TextStyle(
//                            color = Color.Black,
//                            fontSize = 16.sp
//                        ),
//                        singleLine = true,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(start = 12.dp)
//                            .weight(5f)
//                            .onFocusChanged { focusState ->
//                                viewModel.isQueryFocused.value = focusState.isFocused
//                            }
//                    )
//                    Box(
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        if (viewModel.queryText.value.isNotEmpty()) {
//                            IconButton(
//                                onClick = { viewModel.queryText.value = "" },
//                                modifier = Modifier.padding(end = 12.dp)
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Close,
//                                    contentDescription = "Delete",
//                                    tint = Color.Gray
//                                )
//                            }
//                        }
//                    }
//                }
//
////                搜索结果
//                if (viewModel.isQueryFocused.value) {
//                    val queryNoteList by viewModel.queryNoteListStateFlow.collectAsState()
//                    LazyColumn {
//                        items(queryNoteList.noteList.size) { index ->
//                            Card {
//                                Text(queryNoteList.noteList[index].title)
//                            }
//                        }
//                    }
//                }
//
//                viewModel.initCategoryList(context)
//                viewModel.initSelectedCategoryIndex(category)
//
//                LazyRow(
//                    modifier = Modifier.padding(top = 16.dp)
//                ) {
//                    items(viewModel.categoryList.size) { index ->
//                        FilterChip(
//                            onClick = {
//                                navigateToHome(viewModel.categoryList[index])
//                                      },
//                            label = {
//                                Text(text = viewModel.categoryList[index])
//                            },
//                            selected = (index == viewModel.selectedCategoryIndex.intValue),
//                            border = null,
//                            modifier = Modifier
//                                .padding(end = 8.dp)
//                                .height(48.dp)
//                        )
//                    }
//                }
//
//                val orientation = LocalConfiguration.current.orientation
//                LazyVerticalStaggeredGrid(
//                    columns = if (orientation == Configuration.ORIENTATION_PORTRAIT) StaggeredGridCells.Fixed(2)
//                        else StaggeredGridCells.Adaptive(160.dp),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalItemSpacing = 8.dp,
//                    modifier = Modifier.padding(top = 16.dp)
//                ) {
//                    items(noteListState.noteList.size) { index ->
//                        val showOption = remember { mutableStateOf(false) }
//
//                        Box {
//                            Card(
//                                shape = RoundedCornerShape(8.dp),
//                                colors = CardDefaults.cardColors(
//                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
//                                ),
//                                modifier = Modifier
////                                    .clickable {
////                                        navigateToEditorScreen(noteListState.noteList[index].fileName)
////                                    }
//                                    .pointerInput(Unit) {
//                                        detectTapGestures(
//                                            onTap = {
//                                                navigateToEditorScreen(noteListState.noteList[index].fileName)
//                                            },
////                                            onPress = {
////                                                navigateToEditorScreen(noteListState.noteList[index].fileName)
////                                            },
//                                            onLongPress = {
//                                                showOption.value = true
//                                            }
//                                        )
//                                    }
//                            ) {
//                                Column {
////                                图片
//                                    if (noteListState.noteList[index].coverImage != "") {
//                                        val imagePath = noteListState.noteList[index].coverImage
//                                        val file = LocalNoteFileApi.loadFile(imagePath, context)
//                                        if (file.exists()) {
//                                            val image = file.toUri().toString()
//                                            GlideImage(
//                                                model = image,
//                                                contentDescription = "image",
//                                                contentScale = ContentScale.Crop,
//                                                modifier = Modifier
//                                                    .height(140.dp)
//                                            )
//                                        }
//                                    }
////                                文本
//                                    Column(
//                                        modifier = Modifier
//                                            .padding(12.dp)
//                                    ) {
//                                        val noteTitle = noteListState.noteList[index].title
//                                        Text(
//                                            text = if (noteTitle == "") "未命名" else noteTitle,
//                                            fontSize = Typography.titleLarge.fontSize,
//                                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                                        )
//                                        Text(
//                                            text = noteListState.noteList[index].lastModifiedTime,
//                                            fontSize = Typography.bodyMedium.fontSize,
//                                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                                        )
//                                    }
//                                }
//                            }
//
//                            DropdownMenu(
//                                expanded = showOption.value,
//                                onDismissRequest = { showOption.value = false }
//                            ) {
//                                DropdownMenuItem(
//                                    text = { Text("删除") },
//                                    onClick = {
//                                        coroutineScope.launch {
//                                            viewModel.deleteNote(
//                                                username,
//                                                category,
//                                                noteListState.noteList[index].fileName,
//                                                context
//                                            )
//                                        }
//                                    }
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    if (viewModel.showSyncDialog.value) {
//        Dialog(onDismissRequest = {  }) {
//            Card {
//                Text("正在下载数据...")
//            }
//        }
//    }
//}

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
