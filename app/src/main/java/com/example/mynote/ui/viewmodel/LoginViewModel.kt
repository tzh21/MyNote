package com.example.mynote.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynote.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LoginStatus {
    SUCCESS,
    ERROR,
    LOADING,
    INACTIVE
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val status: LoginStatus = LoginStatus.INACTIVE
)

class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(email = email)
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
                val response = userRepository.login(_uiState.value.email, _uiState.value.password)
                Log.d("LoginViewModel", "response: $response")
                _uiState.update {
                    it.copy(status = LoginStatus.SUCCESS)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(status = LoginStatus.ERROR)
                }
            }
        }
    }
}
