package com.example.mynote.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// 笔记按标签（或文件夹）分组的界面

// TODO
// 新增标签分组
// 展示标签分组列表
// 查看标签分组中的所有笔记
// 删除标签分组（可选同时删除分组中的笔记）
// 重命名标签分组
// 批量编辑标签分组

const val CategoryRoute = "Category"

@Composable
fun CategoryScreen(
    modifier: Modifier = Modifier,
) {
    Column {
        Text(text = "Category screen")
    }
}