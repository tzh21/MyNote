package com.example.mynote.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import com.example.mynote.data.LocalFileApi

class CategoryViewModel: ViewModel() {
    var username = mutableStateOf("null")
        private set

    fun setUsername(name: String) {
        username.value = name
    }

    var dirs = mutableStateListOf<String>()
        private set

    fun setDirs(context: Context) {
        dirs.clear()
        dirs.addAll(LocalFileApi.listDirs(username.value, context))
    }

    var showDialog = mutableStateOf(false)
        private set

    fun setShowDialog(value: Boolean) {
        showDialog.value = value
    }

    var newCategory = mutableStateOf("")
        private set

    fun setNewCategory(value: String) {
        newCategory.value = value
    }
}