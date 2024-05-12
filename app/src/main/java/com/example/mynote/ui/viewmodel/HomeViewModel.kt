package com.example.mynote.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.mynote.data.LocalFileApi

class HomeViewModel: ViewModel() {
    var username = mutableStateOf("null")

    var category = mutableStateOf("null")

    var fileNames = mutableStateListOf<String>()

    fun setFiles(context: Context) {
        fileNames.clear()
        fileNames.addAll(LocalFileApi.listFiles("${username.value}/${category.value}", context))
    }

    fun deleteAllFiles(context: Context) {
        fileNames.clear()
        LocalFileApi.deleteAllFiles("${username.value}/${category.value}", context)
    }
}