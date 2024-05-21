package com.example.mynote.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynote.data.Block
import com.example.mynote.data.BlockType
import com.example.mynote.data.LocalNoteFileApi
import com.example.mynote.data.NoteDao
import com.example.mynote.data.ProfileEntity
import com.example.mynote.data.RemoteFileApi
import com.example.mynote.data.getCurrentTime
import com.example.mynote.network.MottoRequest
import com.example.mynote.network.MyNoteApiService
import com.example.mynote.network.NicknameRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream

class ProfileViewModel(
    val noteDao: NoteDao,
    val apiService: MyNoteApiService,
): ViewModel() {
    companion object {
        private const val TIMEOUT_MILLIS = 50_000L
    }

    lateinit var profileStateFlow: StateFlow<ProfileState>

    var username = mutableStateOf("")

    fun initProfileStateFlow() {
        if (!::profileStateFlow.isInitialized) {
            profileStateFlow = noteDao.getProfile(username.value)
                .filterNotNull()
                .map { ProfileState(it) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                    initialValue = ProfileState()
                )
        }
    }

//    个性签名
    var showMottoDialog = mutableStateOf(false)
    fun updateMotto(
        newMotto: String
    ) {
        viewModelScope.launch {
            noteDao.insertProfile(ProfileEntity(username = username.value))
            noteDao.updateMotto(username.value, newMotto)
            apiService.postMotto(username.value, MottoRequest(newMotto))
        }
    }

//    昵称
    var showNicknameDialog = mutableStateOf(false)
    fun updateNickname(
        newNickname: String
    ) {
        viewModelScope.launch {
            noteDao.insertProfile(ProfileEntity(username = username.value))
            noteDao.updateNickname(username.value, newNickname)
            apiService.postNickname(username.value, NicknameRequest(newNickname))
        }
    }

    var avatarByteArray: MutableState<ByteArray> = mutableStateOf(ByteArray(0))
    fun initAvatar(context: Context) {
        val avatarFile = LocalNoteFileApi.loadAvatar("${username.value}/avatar", context)
        if (avatarFile.exists()) {
            avatarByteArray.value = avatarFile.readBytes()
        }
    }

    fun selectImage(
        imageUri: Uri,
        context: Context
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = "${username.value}/avatar"
            LocalNoteFileApi.saveAvatar(imageUri, path, context)
            avatarByteArray.value = LocalNoteFileApi.loadAvatar(path, context).readBytes()
            RemoteFileApi.uploadAvatar(username.value, context, viewModelScope, apiService)
        }
    }

    suspend fun downloadProfile(
        context: Context
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            var newMotto = ""
            val mottoResponse = apiService.getMotto(username.value)
            if (mottoResponse.isSuccessful) {
                val mottoBody = mottoResponse.body()
                if (mottoBody != null) {
                    newMotto = mottoBody.motto
                }
            }

            var newNickname = ""
            val nicknameResponse = apiService.getNickname(username.value)
            if (nicknameResponse.isSuccessful) {
                val nicknameBody = nicknameResponse.body()
                if (nicknameBody != null) {
                    newNickname = nicknameBody.nickname
                }
            }

            val avatarResponse = apiService.getAvatar(username.value)
            if (avatarResponse.isSuccessful) {
                val avatarBody = avatarResponse.body()
                if (avatarBody != null) {
                    LocalNoteFileApi.writeAvatar("${username.value}/avatar", avatarBody.byteStream(), context)
                    avatarByteArray.value = LocalNoteFileApi.loadAvatar("${username.value}/avatar", context).readBytes()
                }
            }

            noteDao.insertProfile(ProfileEntity(username = username.value))
            noteDao.updateNickname(username.value, newNickname)
            noteDao.updateMotto(username.value, newMotto)
            noteDao.updateAvatar(username.value, "${username.value}/avatar")
        }
    }
}

data class ProfileState(
    val profile: ProfileEntity = ProfileEntity()
)