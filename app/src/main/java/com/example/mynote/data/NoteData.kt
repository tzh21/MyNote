package com.example.mynote.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.example.mynote.network.ErrorResponse
import com.example.mynote.network.MyNoteApiService
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    fun createFile(
        path: String, context: Context
    ): File {
        val file = File(context.filesDir, path)

        if (!file.exists()) {
            val dir = File(file.parent ?: "")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            file.createNewFile()
        }

        return file
    }

    fun createDir(
        path: String, context: Context
    ): File {
        val dir = File(context.filesDir, path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun writeFile(
        path: String, byteStream: InputStream,
        context: Context
    ) {
        val file = createFile(path, context)
        FileOutputStream(file).use { stream ->
            byteStream.copyTo(stream)
        }
    }

    fun saveNote(
        path: String, note: Note, context: Context
    ) {
        val file = createFile(path, context)
        val gson = Gson()
        val jsonString = gson.toJson(note)

        FileOutputStream(file).use { stream ->
            stream.write(jsonString.toByteArray())
        }
    }
    fun saveResource(
        uri: Uri, path: String, context: Context
    ) {
        val file = createFile(path, context)
        val resolver: ContentResolver = context.contentResolver
        val inputStream = resolver.openInputStream(uri)

        inputStream?.use {
            FileOutputStream(file).use { outputStream ->
                it.copyTo(outputStream)
            }
        }
    }

    //返回 path 下的所有目录名（不包括文件）
    fun listDirs(
        path: String,
        context: Context,
    ): List<String> {
        val dirs = File(context.filesDir, path).listFiles()
        val dirNames = dirs?.filter { it.isDirectory }?.map { it.name } ?: listOf()

        return dirNames
    }

    //返回 path 下的所有文件名（不包括目录）
    fun listFiles(
        path: String,
        context: Context
    ): List<String> {
        val files = File(context.filesDir, path).listFiles()
        if (files.isEmpty()) {
            return listOf("本目录为空")
        }
        return files?.map { it.name } ?: listOf("本目录为空")
    }

    fun deleteFile(
        path: String,
        context: Context
    ) {
        val file = File(context.filesDir, path)
        if (file.exists()) {
            if (file.isFile) {
                file.delete()
            }
            else if (file.isDirectory) {
                file.deleteRecursively()
            }
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
}

object NoteLoaderApi {
    //    返回 Note 格式的笔记
    fun loadNote(
        path: String,
        context: Context
    ): Note {
        val file = File(context.filesDir, path)
        Log.d("loadNote", path)
        var note = Note(
            title = "未命名",
            body = mutableStateListOf<Block>()
        )

        if (file.exists()) {
            val content = file.readText()
            Log.d("loadNote", content)
            val gson = Gson()
            note = gson.fromJson(content, Note::class.java)
        }

        return note
    }

    //    返回笔记文件的 json 源代码
    fun loadNoteSource(
        path: String,
        context: Context
    ): String {
        val file = File(context.filesDir, path)
        var source = ""

        if (file.exists()) {
            source = file.readText()
        }

        return source
    }
}

object RemoteFileApi {
//    上传单个笔记以及其饮用的资源文件（如图片、音频）
    suspend fun uploadNote(
        path: String,
        context: Context,
        coroutineScope: CoroutineScope,
        apiService: MyNoteApiService
    ) {
        coroutineScope.launch {
//            上传笔记文件
            val file = File(context.filesDir, path)
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val formData = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val response = apiService.upload(path, formData)
            if (response.isSuccessful) {
                Log.d("HomeViewModel", "Upload success")
            } else {
                val errorBody = response.errorBody()?.string()
                val errorDetail = if (errorBody != null) {
                    Json.decodeFromString<ErrorResponse>(errorBody).error
                } else {
                    "Unknown error"
                }

                Log.d("HomeViewModel", errorDetail)
            }

//            上传资源文件
            val note = NoteLoaderApi.loadNote(path, context)
            for (block in note.body) {
                if (block.type == BlockType.IMAGE || block.type == BlockType.AUDIO) {
                    val resourcePath = block.data
                    val resourceFile = File(context.filesDir, resourcePath)
                    val resourceRequestFile = resourceFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    val resourceFormData = MultipartBody.Part.createFormData("file", resourceFile.name, resourceRequestFile)
                    val resourceResponse = apiService.upload(resourcePath, resourceFormData)
                    if (resourceResponse.isSuccessful) {
                        Log.d("HomeViewModel", "Upload resource success")
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
        }
    }
}

object LocalFileDebugApi {
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
}

fun getCurrentTime(): String {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MMdd-HHmmss")
    return currentDateTime.format(formatter)
}
