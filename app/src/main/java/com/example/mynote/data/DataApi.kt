package com.example.mynote.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.mynote.network.AvatarNameRequest
import com.example.mynote.network.ErrorResponse
import com.example.mynote.network.MyNoteApiService
import com.example.mynote.network.ProfileResponse
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
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

    fun loadNoteFile(username: String, fileName: String, context: Context): File {
        return loadFile("${blockBase(username)}/$fileName", context)
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

//    更新数据库中笔记的信息
//    val title: String = "",
//    val keyword: String = "", // 正文的第一段
//    val coverImage: String = "", // 封面图片
//    val lastModifiedTime: String = ""
suspend fun updateNoteEntity(
    username: String, fileName: String,
    category: String, note: Note, noteDao: NoteDao
) {
    val noteTitle = note.title
    val noteBody = note.body

    var bodyString = ""
    for (block in noteBody) {
        bodyString += "${block.data} \n"
    }

    var coverImage = ""
    for (block in noteBody) {
        if (block.type == BlockType.IMAGE) {
            coverImage = block.data
            break
        }
    }

    noteDao.updateNoteInfo(
        username, fileName,
        category, noteTitle, bodyString,
        coverImage, getCurrentTime()
    )
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
            try {
//                上传笔记文件
                val file = LocalNoteFileApi.loadNoteFile(username, fileName, context)
                val requestBody = file.asRequestBody("application/json".toMediaTypeOrNull())
                val response = apiService.uploadBlocks(username, fileName, requestBody)
                if (response.isSuccessful) {
//                    上传资源文件
                    val noteBody = NoteLoaderApi.loadNote(username, fileName, context).body
                    for (block in noteBody) {
                        val resourceFileName = block.data
                        Log.d("RemoteFileApi", "resourcePath: $resourceFileName")
                        when (block.type) {
                            BlockType.IMAGE -> {
                                val resourceFile =
                                    LocalNoteFileApi.loadImage(username, resourceFileName, context)
                                val requestFile =
                                    resourceFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                                val resourceResponse =
                                    apiService.uploadImage(username, resourceFileName, requestFile)
                                if (!resourceResponse.isSuccessful) {
                                    throw Exception("fail to upload image")
                                }
                            }

                            BlockType.AUDIO -> {
                                val resourceFile =
                                    LocalNoteFileApi.loadAudio(username, resourceFileName, context)
                                val requestFile =
                                    resourceFile.asRequestBody("audio/mpeg".toMediaTypeOrNull())
                                val resourceResponse =
                                    apiService.uploadAudio(username, resourceFileName, requestFile)
                                if (!resourceResponse.isSuccessful) {
                                    throw Exception("fail to upload audio")
                                }
                            }

                            else -> {}
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("RemoteFileApi", e.message.toString())
            }
        }
    }

    //    下载单个笔记以及其使用的资源文件（如图片、音频）
    suspend fun downloadNote(
        username: String, fileName: String, category: String, context: Context,
        coroutineScope: CoroutineScope, apiService: MyNoteApiService,
        noteDao: NoteDao
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val fileResponse = apiService.getBlocks(username, fileName)
            if (fileResponse.isSuccessful) {
                val blocksBody = fileResponse.body()!!
                val noteFile = LocalNoteFileApi.createNote(username, fileName, context)
                FileOutputStream(noteFile).use { stream ->
                    stream.write(blocksBody.bytes())
                }

                //            资源文件
                val note = NoteLoaderApi.loadNote(username, fileName, context)
                val noteTitle = note.title
                val noteBody = note.body
                for (block in noteBody) {
                    val resourceFileName = block.data
                    when (block.type) {
                        BlockType.IMAGE -> {
                            val resourceFile =
                                LocalNoteFileApi.createImage(username, resourceFileName, context)
                            val resourceResponse = apiService.getImage(username, resourceFileName)
                            if (resourceResponse.isSuccessful) {
                                FileOutputStream(resourceFile).use { stream ->
                                    stream.write(resourceResponse.body()!!.bytes())
                                }
                            } else {
                                Log.d("RemoteFileApi", "Fail to download resource file")
                            }
                        }

                        BlockType.AUDIO -> {
                            val resourceFile =
                                LocalNoteFileApi.createAudio(username, resourceFileName, context)
                            val resourceResponse = apiService.getAudio(username, resourceFileName)
                            if (resourceResponse.isSuccessful) {
                                FileOutputStream(resourceFile).use { stream ->
                                    stream.write(resourceResponse.body()!!.bytes())
                                }
                            } else {
                                Log.d("RemoteFileApi", "Fail to download resource file")
                            }
                        }

                        else -> {}
                    }
                }

                noteDao.insertNote(
                    NoteEntity(
                        username = username,
                        category = category,
                        fileName = fileName,
                    )
                )
                LocalNoteFileApi.updateNoteEntity(username, fileName, category, note, noteDao)
                noteDao.updateTitle(username, fileName, noteTitle)
            } else {
                val errorBody = fileResponse.errorBody()?.string()
                val errorDetail = if (errorBody != null) {
                    Json.decodeFromString<ErrorResponse>(errorBody).error
                } else {
                    "Unknown error"
                }

                Log.d("RemoteFileApi", errorDetail)
            }
        }
    }

    suspend fun uploadAvatar(
        username: String, fileName: String, context: Context,
        coroutineScope: CoroutineScope, apiService: MyNoteApiService
    ) {
        coroutineScope.launch {
            try {
                val response = apiService.postAvatarName(username, AvatarNameRequest(fileName))
                if (response.isSuccessful) {
                    val file = LocalNoteFileApi.loadAvatar(username, fileName, context)
                    val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    apiService.postAvatar(username, fileName, requestBody)
                }
            } catch (e: Exception) {
                Log.d("RemoteFileApi", e.message.toString())
            }
        }
    }

    suspend fun updateProfile(
        username: String, profileResponseBody: ProfileResponse, context: Context,
        coroutineScope: CoroutineScope,
        apiService: MyNoteApiService,
        noteDao: NoteDao
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val fileName = profileResponseBody.avatar
                val response = apiService.getAvatar(username, fileName)
                if (response.isSuccessful) {
                    val file = LocalNoteFileApi.createAvatar(username, fileName, context)
                    FileOutputStream(file).use { stream ->
                        stream.write(response.body()!!.bytes())
                    }
                }
            } catch (e: Exception) {
                Log.d("RemoteFileApi", e.message.toString())
            }

            noteDao.insertProfile(ProfileEntity(username = username))
            noteDao.updateProfile(
                username,
                profileResponseBody.motto,
                profileResponseBody.nickname,
                profileResponseBody.avatar
            )
        }
    }
}

fun getCurrentTime(): String {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MMdd-HHmmss")
    return currentDateTime.format(formatter)
}
