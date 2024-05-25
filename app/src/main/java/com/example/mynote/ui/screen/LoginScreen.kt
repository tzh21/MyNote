package com.example.mynote.ui.screen

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynote.Config
import com.example.mynote.R
import com.example.mynote.ui.component.MaxWidthButton
import com.example.mynote.ui.viewmodel.AppViewModelProvider
import com.example.mynote.ui.viewmodel.LoginStatus
import com.example.mynote.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val LoginRoute = "login"
const val SuccessMessage = "登录成功"
fun errorMessage(detail: String) = "登录失败: \n$detail"
const val LoadingMessage = "登录中..."

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navigateToHome: (String) -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) {
            when(viewModel.loginStatus) {
                LoginStatus.SUCCESS -> {
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        snackbarData = it,
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .padding(bottom = 32.dp)
                    )
                }
                LoginStatus.ERROR -> {
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.error,
                        snackbarData = it,
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .padding(bottom = 32.dp)
                    )
                }
                LoginStatus.LOADING -> {
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        snackbarData = it,
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .padding(bottom = 32.dp)
                    )
                }
                else -> {}
            }
        } },
        topBar = {
            MediumTopAppBar(
                title = {
                    Row {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "登录或注册")
                    }
                }
            )
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 1000.dp)
                    .padding(horizontal = 32.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.username,
                    onValueChange = { viewModel.username = it },
                    label = { Text("账号") },
                    shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_corner_size)),
                    modifier = Modifier
                        .fillMaxWidth()
                )

//                    密码输入框
                OutlinedTextField(
                    value = viewModel.password,
                    onValueChange = { viewModel.password = it },
                    label = { Text("密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_corner_size)),
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MaxWidthButton(
                        onClick = { coroutineScope.launch(Dispatchers.IO) { viewModel.login() } },
                        modifier = Modifier.weight(1f),
                        text = "登录"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    MaxWidthButton(
                        onClick = { coroutineScope.launch { viewModel.signup() } },
                        modifier = Modifier
                            .weight(1f),
                        text = "注册",
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (Config.isDebug) {
                    MaxWidthButton(
                        onClick = { viewModel.defaultLogin() },
                        text = "默认用户登录"
                    )
                }

                if (viewModel.loginStatus == LoginStatus.SUCCESS) {
                    navigateToHome(viewModel.username)
                }

                LaunchedEffect(viewModel.loginStatus) {
                    when(viewModel.loginStatus) {
                        LoginStatus.SUCCESS -> {
                            snackbarHostState.showSnackbar(SuccessMessage)
                        }
                        LoginStatus.ERROR -> {
                            snackbarHostState.showSnackbar(errorMessage(viewModel.error))
                        }
                        LoginStatus.LOADING -> {
                            snackbarHostState.showSnackbar(LoadingMessage)
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}