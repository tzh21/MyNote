package com.example.mynote.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.example.mynote.data.Block
import com.example.mynote.data.LocalFileApi
import com.example.mynote.data.Note
import com.example.mynote.data.NoteDao
import com.example.mynote.data.NoteEntity
import com.example.mynote.data.NoteLoaderApi
import com.example.mynote.data.RemoteFileApi
import com.example.mynote.data.getCurrentTime
import com.example.mynote.network.ErrorResponse
import com.example.mynote.network.MyNoteApiService
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody

class EditorViewModel(
    val noteDao: NoteDao,
    val apiService: MyNoteApiService
): ViewModel() {
    lateinit var player: ExoPlayer

    fun initExoPlayer(context: Context) {
        player = ExoPlayer.Builder(context).build()
    }

    var username = mutableStateOf("null")
    var fileName = mutableStateOf("null")
    var category = mutableStateOf("null")

    private var note = Note(
        title = "null",
        body = listOf()
    )
    var noteEntity by mutableStateOf<NoteEntity?>(null)
    var noteTitle = mutableStateOf("null")
    var noteBody = mutableStateListOf<Block>()

    var checkSource = mutableStateOf(false)

//    从文件系统中加载笔记
    fun loadNote(context: Context) {
        viewModelScope.launch {
            note = NoteLoaderApi.loadNote("${username.value}/${category.value}/${fileName.value}", context)

            noteEntity = noteDao.getNoteByName(username.value, category.value, fileName.value)
                .filterNotNull()
                .first()

            noteTitle.value = note.title
            noteBody.clear()
            noteBody.addAll(note.body)
        }
    }

    fun changeText(index: Int, text: String) {
        noteBody[index] = noteBody[index].copy(data = text)
    }

    fun addBlockData(block: Block) {
        noteBody.add(block)
    }

//    本地保存笔记
    suspend fun updateNote(context: Context) {
        note = Note(
            title = noteTitle.value,
            body = noteBody
        )
//        更新到文件系统
        LocalFileApi.saveNote("${username.value}/${category.value}/${fileName.value}", note, context)
//        更新到数据库
        noteDao.update(noteEntity!!.copy(
            category = category.value,
            title = noteTitle.value,
            lastModifiedTime = getCurrentTime()
        ))
    }

    suspend fun upload(
        path: String,
        context: Context
    ) {
//        本地更新笔记
        updateNote(context)
//        上传到云端
        RemoteFileApi.uploadNote(path, context, viewModelScope, apiService)
    }
}
