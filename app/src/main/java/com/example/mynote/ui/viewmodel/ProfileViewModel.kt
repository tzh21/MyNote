package com.example.mynote.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynote.data.LocalNoteFileApi
import com.example.mynote.data.NoteDao
import com.example.mynote.data.ProfileEntity
import com.example.mynote.data.RemoteFileApi
import com.example.mynote.data.getCurrentTime
import com.example.mynote.network.MottoRequest
import com.example.mynote.network.MyNoteApiService
import com.example.mynote.network.NicknameRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    val noteDao: NoteDao,
    val apiService: MyNoteApiService,
): ViewModel() {
    var isSyncing by mutableStateOf(false)

    companion object {
        private const val TIMEOUT_MILLIS = 50_000L
    }

    var profileStateFlow = MutableStateFlow<ProfileEntity>(ProfileEntity())

    var username = ""

    fun initProfileStateFlow() {
        viewModelScope.launch {
            noteDao.getProfile(username)
                .filterNotNull()
                .collect { newProfile -> profileStateFlow.value = newProfile }
        }
    }

    fun insertProfile() {
        viewModelScope.launch {
            noteDao.insertProfile(ProfileEntity(username = username))
        }
    }

//    个性签名
    var showMottoDialog = mutableStateOf(false)
    fun updateMotto(
        newMotto: String
    ) {
        viewModelScope.launch {
            noteDao.updateMotto(username, newMotto)
//            apiService.postMotto(username, MottoRequest(newMotto))
        }
    }

//    昵称
    var showNicknameDialog = mutableStateOf(false)
    fun updateNickname(
        newNickname: String
    ) {
        viewModelScope.launch {
            noteDao.updateNickname(username, newNickname)
//            apiService.postNickname(username, NicknameRequest(newNickname))
        }
    }

//    ByteArray 格式的头像图片
//    思路：无法将图片路径作为 State，因为图片路径始终不变。
//    var avatarByteArray = mutableStateOf(ByteArray(0))
//    fun initAvatar(context: Context) {
//        val avatarFile = LocalNoteFileApi.loadAvatar("${username}/avatar", context)
//        if (avatarFile.exists()) {
//            avatarByteArray.value = avatarFile.readBytes()
//        }
//    }

    fun selectImage(imageUri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileName = getCurrentTime()
            LocalNoteFileApi.saveAvatar(imageUri, username, fileName, context)
            noteDao.updateAvatar(username, fileName)
        }
    }

//    suspend fun downloadProfile(
//        context: Context
//    ) {
//        viewModelScope.launch(Dispatchers.IO) {
//            isSyncing = true
//
//            try {
//                var newMotto = ""
//                val mottoResponse = apiService.getMotto(username)
//                if (mottoResponse.isSuccessful) {
//                    val mottoBody = mottoResponse.body()
//                    if (mottoBody != null) {
//                        newMotto = mottoBody.motto
//                    }
//                }
//
//                var newNickname = ""
//                val nicknameResponse = apiService.getNickname(username)
//                if (nicknameResponse.isSuccessful) {
//                    val nicknameBody = nicknameResponse.body()
//                    if (nicknameBody != null) {
//                        newNickname = nicknameBody.nickname
//                    }
//                }
//
//                val avatarResponse = apiService.getAvatar(username)
//                if (avatarResponse.isSuccessful) {
//                    val avatarBody = avatarResponse.body()
//                    if (avatarBody != null) {
//                        LocalNoteFileApi.writeAvatar("${username}/avatar", avatarBody.byteStream(), context)
//                        avatarByteArray.value = LocalNoteFileApi.loadAvatar("${username}/avatar", context).readBytes()
//                    }
//                }
//
//                noteDao.insertProfile(ProfileEntity(username = username))
//                noteDao.updateNickname(username, newNickname)
//                noteDao.updateMotto(username, newMotto)
//            } finally {
//                isSyncing = false
//            }
//        }
//    }
}
