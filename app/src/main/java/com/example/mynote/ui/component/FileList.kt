package com.example.mynote.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.mynote.data.LocalNoteFileApi

//点击按钮显示 path 下的所有文件和文件夹
@Composable
fun FileList(
    path: String,
) {
    val context = LocalContext.current
    var files = remember {
        mutableStateListOf<String>()
    }
    var show = remember {
        mutableStateOf(false)
    }

    Column {
        Button(
            onClick = {
                files.clear()
                files.addAll(LocalNoteFileApi.listFiles(path, context))
                show.value = !show.value
            }
        ) {
            Text("显示/隐藏 $path 下的文件")
        }
        if (show.value) {
            LazyColumn {
                items(files.size) { index ->
                    Text(files[index])
                }
            }
        }
    }
}