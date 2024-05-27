package com.example.mynote.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
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
import com.example.mynote.network.ChatRequest
import com.example.mynote.network.LLMApiService
import com.example.mynote.network.Message
import com.example.mynote.network.MyNoteApiService
import com.example.mynote.network.prompt
import com.example.mynote.network.systemPrompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditorViewModel(
    val noteDao: NoteDao,
    val serverApi: MyNoteApiService,
    val llmApi: LLMApiService,
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
            RemoteFileApi.uploadNote(username, fileName, context, serverApi)
        }
    }

    var summary by mutableStateOf("")

    suspend fun generateSummary(): String {
        try {
            var bodyString = ""
            for (block in noteBody) {
                bodyString += "${block.data} \n"
            }
            val request = ChatRequest(
                model = "moonshot-v1-8k",
                messages = listOf(
                    systemPrompt,
                    Message("user", prompt + bodyString)
                ),
                temperature = 0.3f
            )
            val response = llmApi.chat(request)
            if (response.isSuccessful) {
                val completion = response.body()?.choices?.get(0)?.message?.content
                if (completion != null) {
                    return completion
                } else {
                    throw Exception("cannot find message content in response")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                if (errorBody != null) {
                    Log.e("EditorViewModel", errorBody)
                }
                throw Exception("response is not successful")
            }
        } catch (e: Exception) {
            Log.e("EditorViewModel", e.toString())
        }
        return "错误"
    }
}
