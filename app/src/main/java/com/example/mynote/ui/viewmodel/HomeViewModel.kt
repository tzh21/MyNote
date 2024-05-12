package com.example.mynote.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynote.data.LocalFileApi
import com.example.mynote.data.NoteDao
import com.example.mynote.data.NoteEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    val noteDao: NoteDao
): ViewModel() {
    var username = mutableStateOf("null")
    var category = mutableStateOf("null")
    var fileNames = mutableStateListOf<String>()

    var noteListUiState: StateFlow<NoteListUiState> =
        noteDao.getAllNotes().map { NoteListUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = NoteListUiState()
            )

//    从数据库中加载笔记列表
//    fun initNoteLists(context: Context) {
//
//    }

//    fun setFiles(context: Context) {
//        fileNames.clear()
//        fileNames.addAll(LocalFileApi.listFiles("${username.value}/${category.value}", context))
//    }

    suspend fun createNote(
        fileName: String,
        context: Context
    ) {
//        文件系统中创建文件
        LocalFileApi.createNoteFile("${username.value}/${category.value}", fileName, context)
//        数据库中更新文件
        noteDao.insert(NoteEntity(
            id = 0,
            username = username.value,
            category = category.value,
            fileName = fileName,
            title = "",
            keyword = "",
            coverImage = "",
            lastModifiedTime = fileName
        ))
    }

    fun deleteAllFiles(context: Context) {
        fileNames.clear()
        LocalFileApi.deleteAllFiles("${username.value}/${category.value}", context)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class NoteListUiState(
    val noteList: List<NoteEntity> = listOf(),
)