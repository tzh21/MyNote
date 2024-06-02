package com.example.mynote.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynote.network.ErrorResponse
import com.example.mynote.network.LoginRequest
import com.example.mynote.network.MyNoteApiService
import com.example.mynote.network.SignupRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

enum class LoginStatus {
    SUCCESS,
    ERROR,
    LOADING,
    INACTIVE
}

class LoginViewModel(
    val apiService: MyNoteApiService
) : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var loginStatus by mutableStateOf(LoginStatus.INACTIVE)
    var error by mutableStateOf("")

    suspend fun login() {
        loginStatus = LoginStatus.LOADING
        try {
            if (username == "" || password == "") {
                loginStatus = LoginStatus.ERROR
                error = "用户名和密码不能为空"
                return
            }

            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                loginStatus = LoginStatus.SUCCESS
            }
            else {
                val errorBody = response.errorBody()?.string()
                val errorDetail = if (errorBody != null) {
                    Json.decodeFromString<ErrorResponse>(errorBody).error
                } else {
                    "unknown error"
                }

                loginStatus = LoginStatus.ERROR
                error = errorDetail
            }
        } catch (e: Exception) {
            loginStatus = LoginStatus.ERROR
            error = e.message ?: "unknown error"
        }
    }

    suspend fun signup() {
        loginStatus = LoginStatus.LOADING
        try {
            if (username == "" || password == "") {
                loginStatus = LoginStatus.ERROR
                error = "用户名和密码不能为空"
                return
            }

            val response = apiService.signup(SignupRequest(username, password))
            if (response.isSuccessful) {
                loginStatus = LoginStatus.SUCCESS
            }
            else {
                val errorBody = response.errorBody()?.string()
                val errorDetail = if (errorBody != null) {
                    Json.decodeFromString<ErrorResponse>(errorBody).error
                } else {
                    "unknown error"
                }

                loginStatus = LoginStatus.ERROR
                error = errorDetail
            }
        } catch (e: Exception) {
            loginStatus = LoginStatus.ERROR
        }
    }

    fun defaultLogin() {
        viewModelScope.launch {
            delay(200)
            username = "3"
            delay(200)
            password = "4"
            login()
        }
    }
}
