package com.example.mynote.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynote.data.CategoryEntity
import com.example.mynote.data.NoteDao
import com.example.mynote.data.getCurrentTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    val noteDao: NoteDao
): ViewModel() {
    var username by mutableStateOf("")

    var categoryList = MutableStateFlow<List<String>>(emptyList())
    fun loadCategoryList(username: String) {
        viewModelScope.launch {
            noteDao.getAllCategories(username)
                .collect { newCategoryList ->
                    categoryList.value = newCategoryList
                }
        }
    }

    fun createCategory(newCategory: String) {
        viewModelScope.launch {
            noteDao.insertCategory(CategoryEntity(0, username, newCategory, getCurrentTime()))
        }
    }

    var showNewCategoryDialog by mutableStateOf(false)
}

//class CategoryViewModel: ViewModel() {
//    var username = mutableStateOf("null")
//        private set
//
//    fun setUsername(name: String) {
//        username.value = name
//    }
//
//    var dirs = mutableStateListOf<String>()
//        private set
//
//    fun setDirs(context: Context) {
//        dirs.clear()
//        dirs.addAll(LocalNoteFileApi.listDirs(username.value, context))
//    }
//
//    var showDialog = mutableStateOf(false)
//        private set
//
//    fun setShowDialog(value: Boolean) {
//        showDialog.value = value
//    }
//
//    var newCategory = mutableStateOf("")
//        private set
//
//    fun setNewCategory(value: String) {
//        newCategory.value = value
//    }
//}