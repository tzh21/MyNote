package com.example.mynote.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynote.ui.component.MaxWidthButton
import com.example.mynote.ui.component.TextFieldDialog
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
    viewModel.username = username

    LaunchedEffect(Unit) {
        viewModel.loadCategoryList(username)
    }

    val categoryList by viewModel.categoryList.collectAsState()
    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(text = "分类", modifier = Modifier.padding(start = 16.dp)) },
                navigationIcon = {
                    Row {
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(
                            onClick = { navigateToHome(currentCategory) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Back to home",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    Row {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = "Multiselect",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            )
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(horizontal = 16.dp)
        ) {
            MaxWidthButton(
                onClick = { viewModel.showNewCategoryDialog = true },
                text = "新建分类"
            )
            Spacer(modifier = Modifier.height(8.dp))
            val orientation = LocalConfiguration.current.orientation
            LazyVerticalGrid(
                columns = if (orientation == Configuration.ORIENTATION_PORTRAIT) GridCells.Fixed(2)
                    else GridCells.Adaptive(160.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(categoryList.size) { index ->
                    val cardColors =
                        if (currentCategory == categoryList[index]) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surface
                    Card(
                        onClick = {navigateToHome(categoryList[index])},
                        colors = CardDefaults.cardColors(containerColor = cardColors),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = categoryList[index],
                                fontSize = Typography.titleMedium.fontSize
                            )
                        }
                    }
                }
            }
        }

        if (viewModel.showNewCategoryDialog) {
            var newCategory by rememberSaveable { mutableStateOf("") }
            TextFieldDialog(
                title = "新建分类",
                text = { TextField(value = newCategory, onValueChange = { newCategory = it }) },
                onConfirmClick = {
                    if (newCategory.isNotEmpty()) {
                        viewModel.createCategory(newCategory)
                    }
                    viewModel.showNewCategoryDialog = false
                },
                onDismissRequest = {
                    viewModel.showNewCategoryDialog = false
                }
            )
        }
    }
}
