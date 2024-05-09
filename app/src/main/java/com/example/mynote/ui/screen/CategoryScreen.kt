package com.example.mynote.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynote.data.LocalFileApi
import com.example.mynote.ui.component.MyNoteTopBar
import com.example.mynote.ui.viewmodel.AppViewModelProvider
import com.example.mynote.ui.viewmodel.CategoryViewModel

data object CategoryRoute {
    const val base = "category"
    const val username = "user"
    const val complete = "$base/{$username}"
}

@Composable
fun CategoryScreen(
    navigateToHome: (String) -> Unit,
    username: String,
    viewModel: CategoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current

    viewModel.setUsername(username)
    viewModel.setDirs(context)

    val dirs = viewModel.dirs

    Scaffold(
        topBar = { MyNoteTopBar(title = "分类", canNavigateBack = false) }
    ) {
        LazyColumn(
            modifier = Modifier.padding(it)
        ) {
            item {
                Button(
                    onClick = {
                        viewModel.setShowDialog(true)
                    }
                ) {
                    Text("新建分类")
                }
            }
            items(dirs.size) { index ->
                Card(
                    modifier = Modifier.clickable {
                        navigateToHome(dirs[index])
                    }
                ) {
                    Text(text = dirs[index])
                }
            }
        }

        if (viewModel.showDialog.value) {
            Dialog(onDismissRequest = { viewModel.setShowDialog(false) }) {
                Card {
                    Column {
                        Text("新建分类")
                        TextField(
                            value = viewModel.newCategory.value,
                            onValueChange = { newCategory -> viewModel.setNewCategory(newCategory) }
                        )
                        Row {
                            Button(
                                onClick = {
                                    viewModel.setShowDialog(false)
                                }
                            ) {
                                Text("取消")
                            }
                            Button(
                                onClick = {
                                    LocalFileApi.createDir("${viewModel.username.value}/${viewModel.newCategory.value}", context)
                                    viewModel.setDirs(context)
                                    viewModel.setShowDialog(false)
                                }
                            ) {
                                Text("确定")
                            }
                        }
                    }
                }
            }
        }
    }
}