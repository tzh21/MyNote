package com.example.mynote

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynote.data.NoteDao
import com.example.mynote.data.NoteEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MyNoteViewModel(
    val noteDao: NoteDao,
): ViewModel() {
    var username by mutableStateOf<String?>(null)

//    key: 分类名称
//    value: 该分类下的所有笔记元数据
//    设计成 StateFlow，可以根据数据库的变化自动更新
    val categoryNotesMap = MutableStateFlow<Map<String, List<NoteEntity>>>(emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadCategoryNotesMap(username: String) {
        viewModelScope.launch {
            noteDao.getAllCategories(username)
                .flatMapLatest { categories ->
                    val flows = categories.map { category ->
                        noteDao.getAllNotesInCategory(username, category)
                            .map { notes -> category to notes }
                    }
                    combine(flows) { categoryNotesPairs ->
                        categoryNotesPairs.toMap()
                    }
                }
                .collect { newMap ->
                    categoryNotesMap.value = newMap
                }
        }
    }
}