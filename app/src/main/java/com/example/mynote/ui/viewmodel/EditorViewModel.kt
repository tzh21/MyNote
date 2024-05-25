package com.example.mynote.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.mynote.data.Block
import com.example.mynote.data.BlockType
import com.example.mynote.data.CategoryEntity
import com.example.mynote.data.LocalNoteFileApi
import com.example.mynote.data.Note
import com.example.mynote.data.NoteDao
import com.example.mynote.data.RemoteFileApi
import com.example.mynote.data.getCurrentTime
import com.example.mynote.network.MyNoteApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditorViewModel(
    val noteDao: NoteDao,
    val apiService: MyNoteApiService
): ViewModel() {
    lateinit var username: String
    lateinit var category: String
    lateinit var fileName: String

    var currentBlockIndex by mutableStateOf(0)

    var noteTitle by mutableStateOf("")
    var noteBody = mutableStateListOf<Block>()
    fun loadNote(context: Context) {
        val note = LocalNoteFileApi.loadNote(username, fileName, context)
        noteTitle = note.title
        noteBody.clear()
        noteBody.addAll(note.body)
    }

    var lastModifiedTime = MutableStateFlow<String>("")
    fun loadLastModifiedTime() {
        viewModelScope.launch(Dispatchers.IO) {
            lastModifiedTime.value = noteDao.getLastModifiedTime(username, fileName)
                .filterNotNull()
                .first()
        }
    }

//    将标题和正文合并为一个字符串，用于后续数据库检索
    fun getBodyString(): String {
        var bodyString = ""
        for (block in noteBody) {
            bodyString += "${block.data} \n"
        }
        return bodyString
    }

    suspend fun saveNote(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
    //        本地保存文件
            val note = Note(title = noteTitle, body = noteBody)
            LocalNoteFileApi.saveNote(username, fileName, note, context)
    //        数据库中保存文件
            LocalNoteFileApi.digestNoteEntity(username, fileName, category, note, noteDao)
            noteDao.updateCategoryLastUsedTime(username, category, getCurrentTime())
        }
    }

    var categoryList = MutableStateFlow<List<String>>(emptyList())
    fun loadCategoryList(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.getAllCategories(username)
                .collect { newCategoryList ->
                    categoryList.value = newCategoryList
                }
        }
    }

    var showNewCategoryDialog by mutableStateOf(false)
    fun createCategory(newCategory: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.insertCategory(CategoryEntity(0, username, newCategory, getCurrentTime()))
        }
    }

//    改变笔记所在的分类
    suspend fun moveNote(newCategory: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            saveNote(context)
    //        无需移动文件
    //        改变 viewModel 中的分类
            category = newCategory
    //        改变数据库中的分类条目
            val oldEntity = noteDao.getNoteByName(username, fileName)
                .filterNotNull()
                .first()
            noteDao.updateNote(oldEntity.copy(
                category = newCategory,
                lastModifiedTime = getCurrentTime()
            ))
        }
    }

    fun insertResource(uri: Uri, type: BlockType, context: Context) {
        val fileName = getCurrentTime()
        when(type) {
            BlockType.IMAGE -> {
                LocalNoteFileApi.saveImage(username, fileName, uri, context)
            }
            BlockType.AUDIO -> {
                LocalNoteFileApi.saveAudio(username, fileName, uri, context)
            }
            else -> {
                throw Exception("Invalid BlockType")
            }
        }

        var insertIndex = currentBlockIndex + 1
//        当前文本块为空时，插入图片到当前文本块
        if (noteBody[currentBlockIndex].type == BlockType.BODY && noteBody[currentBlockIndex].data.isEmpty()) {
            insertIndex = currentBlockIndex
            noteBody[insertIndex] = Block(type, fileName)
        }
        else {
            noteBody.add(insertIndex, Block(type, fileName))
        }

//        在末尾插入空文本块
        noteBody.add(insertIndex + 1, Block(BlockType.BODY, ""))
    }

    fun insertImage(uri: Uri, context: Context) {
        insertResource(uri, BlockType.IMAGE, context)
    }

    fun insertAudio(uri: Uri, context: Context) {
        insertResource(uri, BlockType.AUDIO, context)
    }

    fun deleteResource(fileName: String, type: BlockType, context: Context) {
        when(type) {
            BlockType.IMAGE -> {
                LocalNoteFileApi.deleteImage(username, fileName, context)
            }
            BlockType.AUDIO -> {
                LocalNoteFileApi.deleteAudio(username, fileName, context)
            }
            else -> {
                throw Exception("Invalid BlockType")
            }
        }
    }

    fun deleteImage(fileName: String, context: Context) {
        deleteResource(fileName, BlockType.IMAGE, context)
    }

    fun deleteAudio(fileName: String, context: Context) {
        deleteResource(fileName, BlockType.AUDIO, context)
    }

    lateinit var player: ExoPlayer

    fun initExoPlayer(context: Context) {if (!::player.isInitialized) {player = ExoPlayer.Builder(context).build()}}
    var isPlaying by mutableStateOf(false)
    var currentAudioUri by mutableStateOf(Uri.EMPTY)
    fun playOrPauseAudio(audioUri: Uri) {
        if (player.isPlaying && currentAudioUri == audioUri) {
            player.pause()
            isPlaying = false
        }
        else {
            currentAudioUri = audioUri
            val mediaItem = MediaItem.fromUri(audioUri)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            isPlaying = true
        }
    }

    suspend fun uploadNote(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            saveNote(context)
            RemoteFileApi.uploadNote(username, fileName, context, apiService)
        }
    }
}
