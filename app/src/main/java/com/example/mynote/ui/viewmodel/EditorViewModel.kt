package com.example.mynote.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.mynote.data.Block
import com.example.mynote.data.LocalFileApi
import com.example.mynote.data.Note

class EditorViewModel: ViewModel() {
    lateinit var player: ExoPlayer

    fun initExoPlayer(context: Context) {
        player = ExoPlayer.Builder(context).build()
    }

    var username = mutableStateOf("null")
        private set

    fun setUserName(name: String) {
        username.value = name
    }

    var fileName = mutableStateOf("null")
        private set

    fun setFileName(value: String) {
        fileName.value = value
    }

    var category = mutableStateOf("null")
        private set

    fun setCategory(value: String) {
        category.value = value
    }

    var note = Note(
        title = "null",
        body = listOf()
    )
        private set

    var noteTitle = mutableStateOf("null")
        private set

    var noteBody = mutableStateListOf<Block>()
        private set

    fun loadNote(context: Context) {
        note = LocalFileApi.loadNote("${username.value}/${category.value}/${fileName.value}", context)
        noteTitle.value = note.title
        noteBody.clear()
        noteBody.addAll(note.body)
    }

    fun setNoteTitle(value: String) {
        noteTitle.value = value
    }

    fun changeText(index: Int, text: String) {
        noteBody[index] = noteBody[index].copy(data = text)
    }

    fun addBlockData(block: Block) {
        noteBody.add(block)
    }

    fun insertBlockData(index: Int, block: Block) {
        noteBody.add(index, block)
    }

    fun saveNote(context: Context) {
        note = Note(
            title = noteTitle.value,
            body = noteBody
        )
        LocalFileApi.saveNote("${username.value}/${category.value}", fileName.value, note, context)
    }

    var showImageOption = mutableStateOf(false)
        private set

    fun setShowImageOption(value: Boolean) {
        showImageOption.value = value
    }
}
