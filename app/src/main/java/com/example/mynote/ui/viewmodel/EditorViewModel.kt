package com.example.mynote.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.example.mynote.data.NoteEntity
import com.example.mynote.data.NoteLoaderApi
import com.example.mynote.data.RemoteFileApi
import com.example.mynote.data.getCurrentTime
import com.example.mynote.network.MyNoteApiService
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
        val note = NoteLoaderApi.loadNote(username, fileName, context)
        noteTitle = note.title
        noteBody.clear()
        noteBody.addAll(note.body)
    }

    var lastModifiedTime = MutableStateFlow<String>("")
    fun loadLastModifiedTime() {
        viewModelScope.launch {
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
        viewModelScope.launch {
    //        本地保存文件
            val note = Note(title = noteTitle, body = noteBody)
            LocalNoteFileApi.saveNote(username, fileName, note, context)
    //        数据库中保存文件
            val bodyString = getBodyString()
            val oldEntity = noteDao.getNoteByName(username, fileName)
                .filterNotNull()
                .first()
            noteDao.updateNote(oldEntity.copy(
                title = noteTitle,
                keyword = bodyString,
                lastModifiedTime = getCurrentTime()
            ))
        }
    }

    var categoryList = MutableStateFlow<List<String>>(emptyList())
    fun loadCategoryList(username: String) {
        viewModelScope.launch {
            noteDao.getAllCategories(username)
                .collect { newCategoryList ->
                    categoryList.value = newCategoryList
                }
        }
    }

    var showNewCategoryDialog by mutableStateOf(false)
    fun createCategory(newCategory: String) {
        viewModelScope.launch {
            noteDao.insertCategory(CategoryEntity(0, username, newCategory))
        }
    }

//    改变笔记所在的分类
    fun moveNote(newCategory: String) {
//        文件系统不做任何变化
//        改变 viewModel 中的分类
        category = newCategory
//        改变数据库中的分类条目
        viewModelScope.launch {
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
}

//class EditorViewModel(
//    val noteDao: NoteDao,
//    val apiService: MyNoteApiService
//): ViewModel() {
//    lateinit var player: ExoPlayer
//
//    fun initExoPlayer(context: Context) {
//        player = ExoPlayer.Builder(context).build()
//    }
//
//    var username = mutableStateOf("null")
//    var fileName = mutableStateOf("null")
//    var category = mutableStateOf("null")
//
//    private var note = Note(
//        title = "null",
//        body = listOf()
//    )
//    var noteEntity by mutableStateOf<NoteEntity?>(null)
//    var noteTitle = mutableStateOf("null")
//    var noteBody = mutableStateListOf<Block>()
//
////    从文件系统中加载笔记
//    fun loadNote(context: Context) {
//        viewModelScope.launch {
//            note = NoteLoaderApi.loadNote(username.value,"${username.value}/${category.value}/${fileName.value}", context)
//
//            noteEntity = noteDao.getNoteByName(username.value, category.value, fileName.value)
//                .filterNotNull()
//                .first()
//
//            noteTitle.value = note.title
//            noteBody.clear()
//            noteBody.addAll(note.body)
//        }
//    }
//
////    本地保存笔记
////    BUG：保存笔记时，如果笔记分类发生更改，则资源文件路径需要对应发生更改
////    这个 BUG 的根源是文件系统设计的不合理
//    suspend fun saveNote(context: Context) {
//        note = Note(
//            title = noteTitle.value,
//            body = noteBody
//        )
////        更新到文件系统
//        LocalNoteFileApi.saveNote(username.value, "${username.value}/${category.value}/${fileName.value}", note, context)
//
////        更新封面图片
//        var coverImage = ""
//        for (block in noteBody) {
//            if (block.type == BlockType.IMAGE) {
//                coverImage = block.data
//                break
//            }
//        }
//
////        更新到数据库
//        noteDao.update(noteEntity!!.copy(
//            category = category.value,
//            title = noteTitle.value,
//            coverImage = coverImage,
//            lastModifiedTime = getCurrentTime()
//        ))
//    }
//
//    suspend fun upload(
//        path: String,
//        context: Context
//    ) {
////        本地更新笔记
//        saveNote(context)
////        上传到云端
//        RemoteFileApi.uploadNote(username.value, path, context, viewModelScope, apiService)
//    }
//
//    var categoryList = mutableStateListOf<String>()
//
//    fun initCategoryList(context: Context) {
//        categoryList.clear()
//        categoryList.addAll(LocalNoteFileApi.listDirs(username.value, context))
//    }
//
////    TODO 待更改
////    suspend fun moveNote(newCategory: String, context: Context) {
//////        保存更改
////        saveNote(context)
////
////        LocalNoteFileApi.moveFile(
////            "${username.value}/${category.value}/${fileName.value}",
////            "${username.value}/${newCategory}/${fileName.value}",
////            context
////        )
////
////        LocalNoteFileApi.moveDir(
////            "${username.value}/${category.value}/assets/${fileName.value}",
////            "${username.value}/${newCategory}/assets/${fileName.value}",
////            context
////        )
////
//////        更新数据库
////        val currentTime = getCurrentTime()
////        noteDao.update(noteEntity!!.copy(
////            category = newCategory,
////            lastModifiedTime = currentTime
////        ))
////    }
//
//    var currentBlockIndex = mutableStateOf(0)
//
//    fun insertResource(uri: Uri, type: BlockType, context: Context) {
//        val fileName = getCurrentTime()
////        val path =
////            if (type == BlockType.IMAGE)
////                "${username.value}/${category.value}/assets/${fileName.value}/image/$currentTime"
////            else
////                "${username.value}/${category.value}/assets/${fileName.value}/audio/$currentTime"
////        LocalNoteFileApi.saveResource(uri, path, context)
//        when(type) {
//            BlockType.IMAGE -> {
//                LocalNoteFileApi.saveImage(username.value, fileName, uri, context)
//            }
//            BlockType.AUDIO -> {
//                LocalNoteFileApi.saveAudio(username.value, fileName, uri, context)
//            }
//            else -> {
//                throw Exception("Invalid BlockType")
//            }
//        }
//
//        var insertIndex = currentBlockIndex.value + 1
////        当前文本块为空时，插入图片到当前文本块
//        if (noteBody[currentBlockIndex.value].type == BlockType.BODY && noteBody[currentBlockIndex.value].data.isEmpty()) {
//            insertIndex = currentBlockIndex.value
//            noteBody[insertIndex] = Block(type, fileName)
//        }
//        else {
//            noteBody.add(insertIndex, Block(type, fileName))
//        }
//
////        在末尾插入空文本块
//        if (noteBody.size <= insertIndex + 1) {
//            noteBody.add(insertIndex + 1, Block(BlockType.BODY, ""))
//        }
//    }
//
//    fun insertImage(uri: Uri, context: Context) {
//        insertResource(uri, BlockType.IMAGE, context)
//    }
//
//    fun insertAudio(uri: Uri, context: Context) {
//        insertResource(uri, BlockType.AUDIO, context)
//    }
//
//    var currentAudioUri = mutableStateOf(Uri.EMPTY)
//    var isPlaying = mutableStateOf(false)
//
//    fun playOrPauseAudio(audioUri: Uri) {
//        if (player.isPlaying && currentAudioUri.value == audioUri) {
//            player.pause()
//            isPlaying.value = false
//        }
//        else {
//            currentAudioUri.value = audioUri
//            val mediaItem = MediaItem.fromUri(audioUri)
//            player.setMediaItem(mediaItem)
//            player.prepare()
//            player.play()
//            isPlaying.value = true
//        }
//    }
//}
