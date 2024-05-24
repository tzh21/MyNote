package com.example.mynote.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.flow.filterNotNull
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
            apiService.postMotto(username, MottoRequest(newMotto))
        }
    }

//    昵称
    var showNicknameDialog = mutableStateOf(false)
    fun updateNickname(
        newNickname: String
    ) {
        viewModelScope.launch {
            noteDao.updateNickname(username, newNickname)
            apiService.postNickname(username, NicknameRequest(newNickname))
        }
    }

    fun selectImage(imageUri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileName = getCurrentTime()
            LocalNoteFileApi.saveAvatar(imageUri, username, fileName, context)
            noteDao.updateAvatar(username, fileName)
            RemoteFileApi.uploadAvatar(username, fileName, context, viewModelScope, apiService)
        }
    }

    suspend fun downloadProfile(
        context: Context
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getProfile(username)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("focus", "downloadProfile: ${responseBody.toString()}")
                    if (responseBody != null) {
//                        获取头像图片并更新数据库
                        RemoteFileApi.updateProfile(
                            username, responseBody, context, viewModelScope, apiService, noteDao
                        )

//                        noteDao.insertProfile(ProfileEntity(username = username))
//                        noteDao.updateProfile(username, responseBody.motto, responseBody.nickname, responseBody.avatar)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
