package com.example.mynote.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynote.ui.viewmodel.AppViewModelProvider
import com.example.mynote.ui.viewmodel.SignupStatus
import com.example.mynote.ui.viewmodel.SignupViewModel

const val SignupRoute = "signup"

@Composable
fun SignupScreen(
    navigateToHome: () -> Unit,
    viewModel: SignupViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState = viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
//            账号输入框
        TextField(
            value = uiState.value.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("账号") },
        )

//            密码输入框
        TextField(
            value = uiState.value.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("密码") },
//                visualTransformation = PasswordVisualTransformation(),
        )

//            注册按钮
        Button(onClick = {
            viewModel.onSignupTriggered()
        }) {
            Text("注册")
        }

        when (uiState.value.status) {
            SignupStatus.SUCCESS -> {
                Text("注册成功")
                navigateToHome()
            }
            SignupStatus.ERROR -> {
                Text("注册失败")
            }
            SignupStatus.LOADING -> {
                Text("注册中")
            }
            SignupStatus.INACTIVE -> {
                Text("未注册")
            }
        }
    }
}