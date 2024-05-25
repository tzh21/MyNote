package com.example.mynote.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynote.data.CategoryEntity
import com.example.mynote.data.NoteDao
import com.example.mynote.data.getCurrentTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    val noteDao: NoteDao
): ViewModel() {
    var username by mutableStateOf("")

    var categoryList = MutableStateFlow<List<String>>(emptyList())
    fun loadCategoryList(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.getAllCategories(username)
                .collect { newCategoryList ->
                    categoryList.value = newCategoryList
                }
        }
    }

    fun createCategory(newCategory: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.insertCategory(CategoryEntity(0, username, newCategory, getCurrentTime()))
        }
    }

    var showNewCategoryDialog by mutableStateOf(false)
}
