package com.example.mynote.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynote.data.LocalFileApi
import com.example.mynote.data.NoteDao
import com.example.mynote.data.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    val noteDao: NoteDao
): ViewModel() {
    var username = mutableStateOf("null")
    var category = mutableStateOf("null")

    var noteListUiState: StateFlow<NoteListUiState> =
        noteDao.getAllNotes().map { NoteListUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = NoteListUiState()
            )

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

    suspend fun deleteAllFiles(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
    //        从数据库中删除所有笔记
            noteDao.deleteAllNotes(username.value, category.value)
    //        从文件系统中删除所有笔记
            LocalFileApi.deleteAllFiles("${username.value}/${category.value}", context)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class NoteListUiState(
    val noteList: List<NoteEntity> = listOf(),
)