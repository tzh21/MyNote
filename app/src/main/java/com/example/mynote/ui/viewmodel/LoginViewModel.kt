package com.example.mynote.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynote.network.ErrorResponse
import com.example.mynote.network.LoginRequest
import com.example.mynote.network.MyNoteApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

enum class LoginStatus {
    SUCCESS,
    ERROR,
    LOADING,
    INACTIVE
}

data class LoginState(
    val username: String = "",
    val password: String = "",
    val status: LoginStatus = LoginStatus.INACTIVE,
    val errorDetail: String = ""
)

class LoginViewModel(
    val apiService: MyNoteApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(username = email)
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(password = password)
        }
    }

    fun onLoginTriggered() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(status = LoginStatus.LOADING)
            }
            try {
                val response = apiService.login(LoginRequest(_uiState.value.username, _uiState.value.password))
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(status = LoginStatus.SUCCESS)
                    }
                }
                else {
                    val errorBody = response.errorBody()?.string()
                    val errorDetail = if (errorBody != null) {
                        Json.decodeFromString<ErrorResponse>(errorBody).error
                    } else {
                        "unknown error"
                    }

                    _uiState.update {
                        it.copy(
                            status = LoginStatus.ERROR,
                            errorDetail = errorDetail
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        status = LoginStatus.ERROR,
                        errorDetail = e.message ?: "unknown error"
                    )
                }
            }
        }
    }
    fun defaultLogin() {
        _uiState.update {
            it.copy(username = "3", password = "4")
        }
        onLoginTriggered()
    }
}

