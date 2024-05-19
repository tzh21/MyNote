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
import com.example.mynote.data.LocalFileApi
import com.example.mynote.data.Note
import com.example.mynote.data.NoteDao
import com.example.mynote.data.NoteEntity
import com.example.mynote.data.NoteLoaderApi
import com.example.mynote.data.RemoteFileApi
import com.example.mynote.data.getCurrentTime
import com.example.mynote.network.MyNoteApiService
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

//    本地保存笔记
    suspend fun saveNote(context: Context) {
        note = Note(
            title = noteTitle.value,
            body = noteBody
        )
//        更新到文件系统
        LocalFileApi.saveNote("${username.value}/${category.value}/${fileName.value}", note, context)

//        更新封面图片
        var coverImage = ""
        for (block in noteBody) {
            if (block.type == BlockType.IMAGE) {
                coverImage = block.data
                break
            }
        }

//        更新到数据库
        noteDao.update(noteEntity!!.copy(
            category = category.value,
            title = noteTitle.value,
            coverImage = coverImage,
            lastModifiedTime = getCurrentTime()
        ))
    }

    suspend fun upload(
        path: String,
        context: Context
    ) {
//        本地更新笔记
        saveNote(context)
//        上传到云端
        RemoteFileApi.uploadNote(path, context, viewModelScope, apiService)
    }

    var categoryList = mutableStateListOf<String>()

    fun initCategoryList(context: Context) {
        categoryList.clear()
        categoryList.addAll(LocalFileApi.listDirs(username.value, context))
    }

    suspend fun moveNote(newCategory: String, context: Context) {
//        保存更改
        saveNote(context)

        LocalFileApi.moveFile(
            "${username.value}/${category.value}/${fileName.value}",
            "${username.value}/${newCategory}/${fileName.value}",
            context
        )

        LocalFileApi.moveDir(
            "${username.value}/${category.value}/assets/${fileName.value}",
            "${username.value}/${newCategory}/assets/${fileName.value}",
            context
        )

//        更新数据库
        val currentTime = getCurrentTime()
        noteDao.update(noteEntity!!.copy(
            category = newCategory,
            lastModifiedTime = currentTime
        ))
    }

    var currentBlockIndex = mutableStateOf(0)

    fun insertResource(uri: Uri, type: BlockType, context: Context) {
        val currentTime = getCurrentTime()
        val path =
            if (type == BlockType.IMAGE)
                "${username.value}/${category.value}/assets/${fileName.value}/image/$currentTime"
            else
                "${username.value}/${category.value}/assets/${fileName.value}/audio/$currentTime"
        LocalFileApi.saveResource(uri, path, context)

        var insertIndex = currentBlockIndex.value + 1
//        当前文本块为空时，插入图片到当前文本块
        if (noteBody[currentBlockIndex.value].type == BlockType.BODY && noteBody[currentBlockIndex.value].data.isEmpty()) {
            insertIndex = currentBlockIndex.value
            noteBody[insertIndex] = Block(type, path)
        }
        else {
            noteBody.add(insertIndex, Block(type, path))
        }

//        在末尾插入空文本块
        if (noteBody.size <= insertIndex + 1) {
            noteBody.add(insertIndex + 1, Block(BlockType.BODY, ""))
        }
    }

    fun insertImage(uri: Uri, context: Context) {
        insertResource(uri, BlockType.IMAGE, context)
    }

    fun insertAudio(uri: Uri, context: Context) {
        insertResource(uri, BlockType.AUDIO, context)
    }

    var currentAudioUri = mutableStateOf(Uri.EMPTY)
    var isPlaying = mutableStateOf(false)

    fun playOrPauseAudio(audioUri: Uri) {
        if (player.isPlaying && currentAudioUri.value == audioUri) {
            player.pause()
            isPlaying.value = false
        }
        else {
            currentAudioUri.value = audioUri
            val mediaItem = MediaItem.fromUri(audioUri)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            isPlaying.value = true
        }
    }
}
