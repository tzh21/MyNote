package com.example.mynote.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynote.data.LocalNoteFileApi
import com.example.mynote.data.noteBase
import com.example.mynote.ui.theme.Typography
import com.example.mynote.ui.viewmodel.AppViewModelProvider
import com.example.mynote.ui.viewmodel.CategoryViewModel

data object CategoryRoute {
    const val base = "category"
    const val username = "user"
    const val currentCategory = "currentCategory"
    const val complete = "$base/{$username}/{$currentCategory}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    navigateToHome: (String) -> Unit,
    username: String,
    currentCategory: String,
    viewModel: CategoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current

    viewModel.setUsername(username)
    viewModel.setDirs(context)

    val dirs = viewModel.dirs

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(text = "分类", modifier = Modifier.padding(start = 16.dp)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navigateToHome(currentCategory) },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Back to home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = "Multiselect",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { scaffoldPaddingValue ->
        Column(
            modifier = Modifier
                .padding(scaffoldPaddingValue)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Button(
                onClick = { viewModel.setShowDialog(true) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = "add new category",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "新建分类",
                        fontSize = Typography.titleMedium.fontSize
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val orientation = LocalConfiguration.current.orientation
            LazyVerticalGrid(
                columns = if (orientation == Configuration.ORIENTATION_PORTRAIT) GridCells.Fixed(2)
                    else GridCells.Adaptive(160.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(dirs.size) { index ->
                    val cardColors =
                        if (currentCategory == dirs[index]) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surface
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardColors),
                        modifier = Modifier
                            .clickable {
                                navigateToHome(dirs[index])
                            }
                            .height(56.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dirs[index],
                                fontSize = Typography.titleMedium.fontSize
                            )
                        }
                    }
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
                                    LocalNoteFileApi.createDir("$noteBase/${viewModel.username.value}/${viewModel.newCategory.value}", context)
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
