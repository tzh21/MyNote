package com.example.mynote.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynote.data.Block
import com.example.mynote.data.BlockType
import com.example.mynote.data.CategoryEntity
import com.example.mynote.data.LocalNoteFileApi
import com.example.mynote.data.Note
import com.example.mynote.data.NoteDao
import com.example.mynote.data.NoteEntity
import com.example.mynote.data.RemoteFileApi
import com.example.mynote.data.getCurrentTime
import com.example.mynote.data.noteBase
import com.example.mynote.network.MyNoteApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(
    val noteDao: NoteDao,
    val apiService: MyNoteApiService,
): ViewModel()
{
    var username = ""
    var category = ""

//    在当前分类下创建一个笔记
    suspend fun createNote(context: Context): String {
        val fileName = getCurrentTime()
//        文件系统中创建文件
        val note = Note(
            body = listOf(Block(type = BlockType.BODY, data = ""))
        )
        LocalNoteFileApi.saveNote(username, fileName, note, context)
//        数据库中确认创建分类
        noteDao.insertCategory(CategoryEntity(
            id = 0,
            username = username,
            category = category,
            lastUsedTime = fileName
        ))
//        数据库中更新笔记信息
        noteDao.insertNote(NoteEntity(
            id = 0,
            username = username,
            category = category,
            fileName = fileName,
            title = "",
            keyword = "",
            coverImage = "",
            lastModifiedTime = fileName
        ))

        return fileName
    }

//    删除笔记
    suspend fun deleteNote(fileName: String, context: Context) {
//            数据库
        noteDao.deleteNote(username, category, fileName)
//            文件系统
        LocalNoteFileApi.deleteNote(username, fileName, context)
    }

//    删除当前分类下的所有笔记
    suspend fun deleteAllNotes(context: Context) {
//        从数据库中删除所有笔记
        noteDao.deleteAllNotes(username)
//        从文件系统中删除所有笔记和依赖文件
        LocalNoteFileApi.clearDir("$noteBase/${username}/blocks", context)
        LocalNoteFileApi.clearDir("$noteBase/${username}/image", context)
        LocalNoteFileApi.clearDir("$noteBase/${username}/audio", context)
    }

    var queryText by mutableStateOf("")
    val queryResultsStateFlow = MutableStateFlow<List<NoteEntity>>(emptyList())
    var isQueryFocused by mutableStateOf(false)
//    更新搜索笔记的列表
    fun updateQueryResults() {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.filterNotes(username, queryText).collect { queryResultsStateFlow.value = it }
        }
    }

    var queryMode by mutableStateOf(false)

    fun loadImageFile(fileName: String, context: Context): File {
        return LocalNoteFileApi.loadImage(username, fileName, context)
    }

    suspend fun downloadAll(context: Context) {
        val listResponse = apiService.list(username)
        if (listResponse.isSuccessful) {
            val fileNameList = listResponse.body()!!.files
            for (fileName in fileNameList) {
                viewModelScope.launch(Dispatchers.IO) {
                    RemoteFileApi.downloadNote(
                        username, fileName, category, context,
                        apiService, noteDao
                    )
                }
            }
        }
    }

    suspend fun uploadAll(context: Context) {
//        从数据库中获取所有笔记的列表
//        逐个上传
        viewModelScope.launch(Dispatchers.IO) {
            val noteList = noteDao.getAllNotes(username)
            for (note in noteList) {
                viewModelScope.launch(Dispatchers.IO) {
                    RemoteFileApi.uploadNote(
                        username, note.fileName, context,
                        apiService
                    )
                }
            }
        }
    }
}
