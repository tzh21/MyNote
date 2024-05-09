package com.example.mynote.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.example.mynote.data.LocalFileApi

class HomeViewModel: ViewModel() {
    var username = mutableStateOf("null")
        private set

    fun setUserName(name: String) {
        username.value = name
    }

    var category = mutableStateOf("null")
        private set

    fun setCategory(name: String) {
        category.value = name
    }

    var files = mutableStateListOf<String>()
        private set

    fun setFiles(context: Context) {
//        文件确实被存储在指定位置。
        files.clear()
        files.addAll(LocalFileApi.listFiles("${username.value}/${category.value}", context))
    }

    fun deleteAllFiles(context: Context) {
        files.clear()
        LocalFileApi.deleteAllFiles("${username.value}/${category.value}", context)
    }
}