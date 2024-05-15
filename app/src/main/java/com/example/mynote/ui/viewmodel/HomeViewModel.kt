package com.example.mynote.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynote.data.Block
import com.example.mynote.data.BlockType
import com.example.mynote.data.LocalFileApi
import com.example.mynote.data.Note
import com.example.mynote.data.NoteDao
import com.example.mynote.data.NoteEntity
import com.example.mynote.data.NoteLoaderApi
import com.example.mynote.data.getCurrentTime
import com.example.mynote.network.ErrorResponse
import com.example.mynote.network.MyNoteApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class HomeViewModel(
    val noteDao: NoteDao,
    val apiService: MyNoteApiService
): ViewModel() {
    var username = mutableStateOf("null")
    var category = mutableStateOf("null")

    lateinit var noteListUiState: StateFlow<NoteListUiState>

    fun setNoteList(_username: String, _category: String) {
        if (!::noteListUiState.isInitialized) {
            noteListUiState = noteDao.getAllNotes(_username, _category).map { NoteListUiState(_username, _category, it) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                    initialValue = NoteListUiState()
                )
        }
    }

//    在当前分类下创建一个笔记
    suspend fun createNote(
        fileName: String,
        context: Context
    ) {
//        文件系统中创建文件
        val note = Note(
            title = "",
            body = listOf(Block(type = BlockType.BODY, data = ""))
        )
        LocalFileApi.saveNote(
            "${username.value}/${category.value}/$fileName",
            note,
            context
        )
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

//    删除当前分类下的所有笔记
    suspend fun deleteAllFiles(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
    //        从数据库中删除所有笔记
            noteDao.deleteAllNotes(username.value, category.value)
    //        从文件系统中删除所有笔记
            LocalFileApi.deleteAllFiles("${username.value}/${category.value}", context)
        }
    }

    var showSyncDialog = mutableStateOf(false)

//    从服务器下载当前用户所有分类下的所有笔记以及其依赖文件（如图片、音频等）
    suspend fun download(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            showSyncDialog.value = true

//            从服务器获取文件列表
            val response = apiService.list(username.value)
            if (response.isSuccessful) {
                val noteList = response.body()!!

//                根据文件列表逐个下载文件
                for (filePath in noteList.files) {
                    val fileResponse = apiService.download(filePath)
                    if (fileResponse.isSuccessful) {
//                        保存笔记到文件系统
//                        BUG files 会下载图片文件
                        val responseBody = fileResponse.body()!!
                        LocalFileApi.writeFile(filePath, responseBody.byteStream(), context)

                        val note = NoteLoaderApi.loadNote(filePath, context)

//                        下载依赖文件
                        val body = note.body
                        for (block in body) {
                            if (block.type == BlockType.IMAGE || block.type == BlockType.AUDIO) {
                                val resourcePath = block.data
                                val resourceResponse = apiService.download(resourcePath)
                                if (resourceResponse.isSuccessful) {
                                    val resourceResponseBody = resourceResponse.body()!!
                                    Log.d("download", resourceResponseBody.toString())
                                    LocalFileApi.writeFile(resourcePath, resourceResponseBody.byteStream(), context)
                                } else {
                                    val errorBody = resourceResponse.errorBody()?.string()
                                    val errorDetail = if (errorBody != null) {
                                        Json.decodeFromString<ErrorResponse>(errorBody).error
                                    } else {
                                        "Unknown error"
                                    }

                                    Log.d("HomeViewModel", errorDetail)
                                }
                            }
                        }

//                        更新数据库中笔记信息
                        val currentTime = getCurrentTime()
                        val title = note.title
                        val fileName = filePath.split("/").last()
                        val categoryName = filePath.split("/").dropLast(1).last()
                        noteDao.insert(NoteEntity(
                            id = 0,
                            username = username.value,
                            category = categoryName,
                            fileName = fileName,
                            title = title,
                            keyword = "",
                            coverImage = "",
                            lastModifiedTime = currentTime
                        ))
                    } else {
                        val errorBody = fileResponse.errorBody()?.string()
                        val errorDetail = if (errorBody != null) {
                            Json.decodeFromString<ErrorResponse>(errorBody).error
                        } else {
                            "Unknown error"
                        }

                        Log.d("HomeViewModel", errorDetail)
                    }
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorDetail = if (errorBody != null) {
                    Json.decodeFromString<ErrorResponse>(errorBody).error
                } else {
                    "Unknown error"
                }

                Log.d("HomeViewModel", errorDetail)
            }

            showSyncDialog.value = false
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 50_000L
    }
}

data class NoteListUiState(
    val username: String = "null",
    val category: String = "null",
    val noteList: List<NoteEntity> = listOf(),
)