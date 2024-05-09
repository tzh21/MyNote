package com.example.mynote.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

enum class BlockType {
    BODY,
    IMAGE,
    AUDIO,
}

data class Block(
    val type: BlockType,
    val data: String
)

data class Note(
    val title: String,
    val body: List<Block>
)

//本地文件相关操作
object LocalFileApi {
    fun createNote(
        prefix: String, fileName: String,
        context: Context,
    ) {
        val directory = File(context.filesDir, prefix)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val note = Note(
            title = "未命名",
            body = mutableStateListOf<Block>(
                Block(BlockType.BODY, "")
            )
        )

        saveNote(
            prefix, fileName,
            note, context
        )
    }

    fun createDir(
        path: String, context: Context
    ) {
        val root = context.filesDir
        val dir = File(root, path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    fun saveNote(
        prefix: String, fileName: String,
        note: Note, context: Context
    ) {
        val gson = Gson()
        val jsonString = gson.toJson(note)

        val directory = File(context.filesDir, prefix)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)

        // 创建文件并写入内容
        FileOutputStream(file).use { stream ->
            stream.write(jsonString.toByteArray())
        }
    }

    fun saveResource(
        uri: Uri, prefix: String,
        fileName: String, context: Context
    ) {
        val resolver: ContentResolver = context.contentResolver

        val root = context.filesDir
        val dir = File(root, prefix)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val audioFile = File(dir, fileName)

        val inputStream = resolver.openInputStream(uri)
        inputStream?.use {
            FileOutputStream(audioFile).use { outputStream ->
                it.copyTo(outputStream)
            }
        }
    }

    fun printFileContent(
        path: String,
        context: Context,
    ) {
        val root = context.filesDir
        val file = File(root, path)
        if (file.exists()) {
            val content = file.readText()
            Log.d("printFileContent", content)
        } else {
            Log.d("printFileContent", "文件不存在")
        }
    }

    fun printFilenames(
        path: String,
        context: Context,
    ) {
        val root = context.filesDir
        val filesDir = File(root, path)
        val files = filesDir.listFiles()

        if (files != null) {
            for (file in files) {
                Log.d("printFilenames", file.name)
            }
        } else {
            Log.d("printFilenames", "文件目录为空")
        }
    }

    //返回 path 下的所有目录名（不包括文件）
    fun listDirs(
        path: String,
        context: Context,
    ): List<String> {
        val root = context.filesDir
        val dirs = File(root, path).listFiles()
        val dirNames = dirs?.filter { it.isDirectory }?.map { it.name } ?: listOf()

        return dirNames
    }

    //返回 path 下的所有文件名（不包括目录）
    fun listFiles(
        path: String,
        context: Context
    ): List<String> {
        val root = context.filesDir
        val files = File(root, path).listFiles()
        if (files.isEmpty()) {
            return listOf("本目录为空")
        }
        return files?.map { it.name } ?: listOf("本目录为空")
    }

    fun deleteFile(
        path: String,
        context: Context
    ) {
        val root = context.filesDir
        val file = File(root, path)
        if (file.exists()) {
            if (file.isFile) {
                file.delete()
            }
            else if (file.isDirectory) {
                file.deleteRecursively()
            }
        }
        else {
            throw Exception("$path: 文件不存在")
        }
    }

//    删除目录下的所有文件和文件夹
    fun deleteAllFiles(
        path: String,
        context: Context
    ) {
        val root = context.filesDir
        val files = File(root, path).listFiles()

        if (files != null) {
            for (file in files) {
                if (file.isFile) {
                    file.delete()
                }
                else if (file.isDirectory) {
                    file.deleteRecursively()
                }
            }
        }
    }

    fun loadNote(
        path: String,
        context: Context
    ): Note {
        val root = context.filesDir
        val file = File(root, path)
        var note = Note(
            title = "未命名",
            body = mutableStateListOf<Block>()
        )

        if (file.exists()) {
            val content = file.readText()
            val gson = Gson()
            note = gson.fromJson(content, Note::class.java)
        }

        return note
    }

    fun loadBlockList(
        path: String,
        context: Context
    ): List<Block> {
        val root = context.filesDir
        val file = File(root, path)
        var blockList = listOf<Block>()

        if (file.exists()) {
            val content = file.readText()
            val gson = Gson()
            blockList = gson.fromJson(content, Array<Block>::class.java).toList()
        }

        return blockList
    }
}

fun getCurrentTime(): String {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MMdd-HHmmss")
    return currentDateTime.format(formatter)
}
