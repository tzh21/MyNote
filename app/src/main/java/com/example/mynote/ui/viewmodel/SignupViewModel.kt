package com.example.mynote.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynote.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SignupStatus {
    SUCCESS,
    ERROR,
    LOADING,
    INACTIVE
}

data class SignupState(
    val username: String,
    val password: String,
    val status: SignupStatus = SignupStatus.INACTIVE,
)

class SignupViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignupState("", ""))
    val uiState: StateFlow<SignupState> = _uiState.asStateFlow()

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

    fun onSignupTriggered() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(status = SignupStatus.LOADING)
            }
            try {
                userRepository.signup(_uiState.value.username, _uiState.value.password)
                _uiState.update {
                    it.copy(status = SignupStatus.SUCCESS)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(status = SignupStatus.ERROR)
                }
            }
        }
    }
}