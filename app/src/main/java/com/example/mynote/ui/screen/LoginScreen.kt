// 登录界面

package com.example.mynote.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynote.ui.viewmodel.AppViewModelProvider
import com.example.mynote.ui.viewmodel.LoginStatus
import com.example.mynote.ui.viewmodel.LoginViewModel

const val LoginRoute = "login"

@Composable
fun LoginScreen(
    navigateToHome: (String) -> Unit,
    navigateToSignup: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory),
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
            visualTransformation = PasswordVisualTransformation(),
        )

//            登录按钮
        Button(onClick = {
            viewModel.onLoginTriggered()
        }) {
            Text("登录")
        }

//            注册按钮
        Button(onClick = {
            navigateToSignup()
        }) {
            Text("前往注册")
        }

        when (uiState.value.status) {
            LoginStatus.SUCCESS -> {
                Text("登录成功")
                navigateToHome(uiState.value.email)
            }
            LoginStatus.ERROR -> {
                Text("登录失败")
                Text(uiState.value.errorDetail)
            }
            LoginStatus.LOADING -> {
                Text("登录中...")
            }
            LoginStatus.INACTIVE -> {
                Text("未登录")
            }
        }
    }
}

//能够存储上次登录的信息。在使用 ViewModel 重构的过程中暂时废弃。后续会将这个功能加入 ViewModel 中
//暂时不删，以供以后实现时参考

//private const val PREFS_NAME = "MyAppPrefs"
//private const val KEY_EMAIL = "email"
//private const val KEY_PASSWORD = "password"

//@Composable
//fun LoginScreen(
//    navigateToHome: () -> Unit,
//    navigateToSignup: () -> Unit,
//    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory),
//    modifier: Modifier = Modifier,
//) {
//    val uiState = viewModel.uiState.collectAsState()
//
//    var loggedIn by remember { mutableStateOf(false) }
//    var savedEmail by remember { mutableStateOf<String?>(null) }
//    var savedPassword by remember { mutableStateOf<String?>(null) }
//
//    val context = LocalContext.current
//
//    if (!loggedIn) {
//        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
//        savedEmail = sharedPreferences.getString(KEY_EMAIL, null)
//        savedPassword = sharedPreferences.getString(KEY_PASSWORD, null)
//    }
//
//    var email by remember { mutableStateOf(TextFieldValue(savedEmail ?: "")) }
//    var password by remember { mutableStateOf(TextFieldValue(savedPassword ?: "")) }
//    var saveCredentialsChecked by remember { mutableStateOf(true)}
//
//    if (loggedIn) {
//        navigateToHome()
//    }
//    else {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//        ) {
//            TextField(
////                value = email,
//                value = uiState.value.email,
////                onValueChange = { email = it },
//                onValueChange = { viewModel.onEmailChange(it) },
//                label = { Text("账号") },
//            )
//            TextField(
////                value = password,
//                value = uiState.value.password,
//                onValueChange = { viewModel.onPasswordChange(it) },
//                label = { Text("密码") },
//                visualTransformation = PasswordVisualTransformation(),
//            )
//            Row {
//                Text("记住用户")
//                Checkbox(
//                    checked = saveCredentialsChecked,
//                    onCheckedChange = { saveCredentialsChecked = it },
//                )
//            }
//            Button(
//                onClick = {
//                    val loginSuccess = performLogin(email.text, password.text)
//                    if (loginSuccess) {
//                        navigateToHome()
//                        loggedIn = true
//                        if (saveCredentialsChecked) {
//                            saveCredentials(context, email.text, password.text)
//                        } else {
//                            saveCredentials(context, "", "")
//                        }
//                    }
//                },
//            ) {
//                Text("登录")
//            }
//            Button(
//                onClick = {
//                    navigateToSignup()
//                }
//            ) {
//                Text("注册")
//            }
//        }
//    }
//}

//fun saveCredentials(context: Context, email: String, password: String) {
//    val sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
//    val editor = sharedPreferences.edit()
//    editor.putString(KEY_EMAIL, email)
//    editor.putString(KEY_PASSWORD, password)
//    editor.apply()
//}