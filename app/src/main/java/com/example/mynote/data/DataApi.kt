package com.example.mynote.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
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

//笔记文件的根路径
const val noteBase = "note"
fun blockBase(username: String) = "$noteBase/$username/blocks"
fun imageBase(username: String) = "$noteBase/$username/image"
fun audioBase(username: String) = "$noteBase/$username/audio"
//const val blockBase = "$noteBase/blocks"
//const val imageBase = "$noteBase/image"
//const val audioBase = "$noteBase/audio"
const val profileBase = "profile"
fun avatarBase(username: String) = "$profileBase/$username/avatar"

//本地笔记文件相关操作
//参数路径为笔记文件系统中的相对路径，以 username 为根目录
//会在路径前加上 context.filesDir 和 noteBase
object LocalNoteFileApi {
    fun createFile(path: String, context: Context): File {
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

    fun createNote(
        username: String,
        fileName: String, context: Context
    ): File {
        return createFile("${blockBase(username)}/$fileName", context)
    }

    fun createImage(
        username: String,
        fileName: String, context: Context
    ): File {
        return createFile("${imageBase(username)}/$fileName", context)
    }

    fun createAudio(
        username: String,
        fileName: String, context: Context
    ): File {
        return createFile("${audioBase(username)}/$fileName", context)
    }

    fun createDir(
        path: String, context: Context
    ): File {
        val dir = File(context.filesDir, "$noteBase/$path")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

//    fun writeFile(
//        filaName: String, byteStream: InputStream,
//        context: Context
//    ) {
//        val file = createNote(filaName, context)
//        FileOutputStream(file).use { stream ->
//            byteStream.copyTo(stream)
//        }
//    }

//    fun writeAvatar(
//        path: String, byteStream: InputStream,
//        context: Context
//    ) {
//        val file = createAvatar(path, context)
//        FileOutputStream(file).use { stream ->
//            byteStream.copyTo(stream)
//        }
//    }

//    fun moveFile(
//        from: String, to: String,
//        context: Context
//    ) {
//        val oldFile = File(context.filesDir, "$noteBase/$from")
//        if (oldFile.exists()) {
//            val newFile = createNote(to, context)
//            oldFile.renameTo(newFile)
//        }
//    }

//    fun moveDir(
//        from: String, to: String,
//        context: Context
//    ) {
//        val oldDir = File(context.filesDir, "$noteBase/$from")
//        if (oldDir.exists()) {
//            val newDir = createDir(to, context)
//            oldDir.renameTo(newDir)
//        }
//    }

    fun saveNote(username: String, fileName: String, note: Note, context: Context) {
        val file = createNote(username, fileName, context)
        val gson = Gson()
        val jsonString = gson.toJson(note)

        FileOutputStream(file).use { stream ->
            stream.write(jsonString.toByteArray())
        }
    }

    fun saveResource(filePath: String, uri: Uri, context: Context) {
        val file = createFile(filePath, context)
        val resolver: ContentResolver = context.contentResolver
        val inputStream = resolver.openInputStream(uri)

        inputStream?.use {
            FileOutputStream(file).use { outputStream ->
                it.copyTo(outputStream)
            }
        }
    }

    fun saveImage(username: String, fileName: String, uri: Uri, context: Context) {
        saveResource("${imageBase(username)}/$fileName", uri, context)
    }

    fun saveAudio(username: String, fileName: String, uri: Uri, context: Context) {
        saveResource("${audioBase(username)}/$fileName", uri, context)
    }

//    fun saveResource(
//        uri: Uri, fileName: String, context: Context
//    ) {
//        val file = createFile(path, context)
//        val resolver: ContentResolver = context.contentResolver
//        val inputStream = resolver.openInputStream(uri)
//
//        inputStream?.use {
//            FileOutputStream(file).use { outputStream ->
//                it.copyTo(outputStream)
//            }
//        }
//    }

    fun createAvatar(username: String, fileName: String, context: Context): File {
        return createFile("${avatarBase(username)}/$fileName", context)
    }

    fun saveAvatar(uri: Uri, username: String, fileName: String, context: Context) {
        val file = createAvatar(username, fileName, context)
        val resolver: ContentResolver = context.contentResolver
        val inputStream = resolver.openInputStream(uri)

        inputStream?.use {
            FileOutputStream(file).use { outputStream ->
                it.copyTo(outputStream)
            }
        }
    }

    fun loadAvatar(username: String, fileName: String, context: Context): File {
        return loadFile("${avatarBase(username)}/$fileName", context)
    }

    //返回 path 下的所有目录名（不包括文件）
    fun listDirs(
        path: String,
        context: Context,
    ): List<String> {
        val dirs = File(context.filesDir, "$noteBase/$path").listFiles()
        val dirNames = dirs?.filter { it.isDirectory }?.map { it.name } ?: listOf()

        return dirNames
    }

    //返回 path 下的所有文件名（不包括目录）
    fun listFiles(
        path: String,
        context: Context
    ): List<String> {
        val files = File(context.filesDir, "$noteBase/$path").listFiles()
        if (files != null) {
            if (files.isEmpty()) {
                return listOf("本目录为空")
            }
        }
        return files?.map { it.name } ?: listOf("本目录为空")
    }

//    删除文件或目录
    fun deleteFile(path: String, context: Context) {
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

//    删除笔记以及其引用的资源文件
    fun deleteNote(
        username: String,
        fileName: String, context: Context
    ) {
        val note = NoteLoaderApi.loadNote(username, fileName, context)
        for (block in note.body) {
            when (block.type) {
                BlockType.IMAGE -> {
                    val resourcePath = block.data
                    deleteFile("${imageBase(username)}/$resourcePath", context)
                }
                BlockType.AUDIO -> {
                    val resourcePath = block.data
                    deleteFile("${audioBase(username)}/$resourcePath", context)
                }
                else -> {}
            }
        }
        deleteFile("${blockBase(username)}/$fileName", context)
    }

//    删除笔记所依赖的资源文件
//    fun deleteResource(username: String, notePath: String, context: Context) {
//        val note = NoteLoaderApi.loadNote(username, fileName, context)
//        for (block in note.body) {
//            if (block.type == BlockType.IMAGE || block.type == BlockType.AUDIO) {
//                val resourcePath = block.data
//                val resourceFile = File(context.filesDir, "$noteBase/$resourcePath")
//                if (resourceFile.exists()) {
//                    resourceFile.delete()
//                }
//            }
//        }
//    }

//    清空目录下的文件（不删除目录本身）
    fun clearDir(
        path: String,
        context: Context
    ) {
        val dir = File(context.filesDir, path)
        if (dir.exists() && dir.isDirectory) {
            val files = dir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile) {
                        file.delete()
                    } else if (file.isDirectory) {
                        file.deleteRecursively()
                    }
                }
            }
        }
    }

    fun loadFile(path: String, context: Context): File {
        return File(context.filesDir, path)
    }

    fun loadImage(username: String, fileName: String, context: Context): File {
        return loadFile("${imageBase(username)}/$fileName", context)
    }

    fun loadAudio(username: String, fileName: String, context: Context): File {
        return loadFile("${audioBase(username)}/$fileName", context)
    }

    fun deleteImage(username: String, fileName: String, context: Context) {
        deleteFile("${imageBase(username)}/$fileName", context)
    }

    fun deleteAudio(username: String, fileName: String, context: Context) {
        deleteFile("${audioBase(username)}/$fileName", context)
    }
}

object NoteLoaderApi {
    //    返回 Note 格式的笔记
    fun loadNote(username: String, fileName: String, context: Context): Note {
        val filePath = "${blockBase(username)}/$fileName"
        val file = LocalNoteFileApi.loadFile(filePath, context)
        var note: Note? = null

        if (file.exists()) {
            val content = file.readText()
            note = Gson().fromJson(content, Note::class.java)
        }

        return note!!
    }
}

//远程笔记文件相关操作
//和服务器通信中使用的 path 是相对路径，不包含 noteBase 和 filesDir
object RemoteFileApi {
//    上传单个笔记以及其使用的资源文件（如图片、音频）
    suspend fun uploadNote(
        username: String, fileName: String, context: Context,
        coroutineScope: CoroutineScope, apiService: MyNoteApiService
    ) {
        coroutineScope.launch {
//            上传笔记文件
            val filePath = "${blockBase(username)}/$fileName"
            val file = LocalNoteFileApi.loadFile(filePath, context)
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val formData = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val response = apiService.upload(filePath, formData)
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
            val note = NoteLoaderApi.loadNote(username, fileName, context)
            for (block in note.body) {
                if (block.type == BlockType.IMAGE || block.type == BlockType.AUDIO) {
                    val resourcePath = block.data
                    val resourceFile = LocalNoteFileApi.loadFile(resourcePath, context)
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

//    suspend fun uploadAvatar(
//        username: String,
//        context: Context,
//        coroutineScope: CoroutineScope,
//        apiService: MyNoteApiService
//    ) {
//        coroutineScope.launch {
//            val path = "$username/avatar"
//            val file = LocalNoteFileApi.loadAvatar(path, context)
//            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//            val formData = MultipartBody.Part.createFormData("avatar", file.name, requestFile)
//            val response = apiService.postAvatar(username, formData)
//            if (response.isSuccessful) {
//                Log.d("HomeViewModel", "Upload avatar success")
//            } else {
//                val errorBody = response.errorBody()?.string()
//                val errorDetail = if (errorBody != null) {
//                    Json.decodeFromString<ErrorResponse>(errorBody).error
//                } else {
//                    "Unknown error"
//                }
//
//                Log.d("HomeViewModel", errorDetail)
//            }
//        }
//    }
}

//object LocalFileDebugApi {
//    fun printFileContent(
//        path: String,
//        context: Context,
//    ) {
//        val root = context.filesDir
//        val file = File(root, path)
//        if (file.exists()) {
//            val content = file.readText()
//            Log.d("printFileContent", content)
//        } else {
//            Log.d("printFileContent", "文件不存在")
//        }
//    }
//
//    fun printFilenames(
//        path: String,
//        context: Context,
//    ) {
//        val root = context.filesDir
//        val filesDir = File(root, path)
//        val files = filesDir.listFiles()
//
//        if (files != null) {
//            for (file in files) {
//                Log.d("printFilenames", file.name)
//            }
//        } else {
//            Log.d("printFilenames", "文件目录为空")
//        }
//    }
//}

fun getCurrentTime(): String {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MMdd-HHmmss")
    return currentDateTime.format(formatter)
}
