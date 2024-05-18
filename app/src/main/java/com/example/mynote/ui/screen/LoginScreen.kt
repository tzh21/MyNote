// 登录界面

package com.example.mynote.ui.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.mynote.ui.component.MyNoteTopBar
import com.example.mynote.ui.theme.DarkColorScheme
import com.example.mynote.ui.theme.LightColorScheme
import com.example.mynote.ui.theme.Typography
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
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            MyNoteTopBar(
                title = "登录或注册",
                canNavigateBack = false
            )
        }
    ) {
        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                    }
                }
                .padding(it)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 1000.dp)
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                OutlinedTextField(
                    value = uiState.value.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = { Text("账号") },
                    shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_corner_size)),
                    modifier = Modifier
                        .fillMaxWidth()
                )

//                    密码输入框
                OutlinedTextField(
                    value = uiState.value.password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    label = { Text("密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_corner_size)),
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Button(
                        onClick = {
                            viewModel.onLoginTriggered()
                        },
                        shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_corner_size)),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .height(dimensionResource(id = R.dimen.text_field_height))
                            .weight(1f)
                    ) {
                        Text(
                            text = "登录",
                            fontSize = Typography.titleMedium.fontSize,
                        )
                    }

    //                    注册按钮
                    FilledTonalButton(
                        onClick = {
                            navigateToSignup()
                        },
                        shape = RoundedCornerShape(dimensionResource(R.dimen.rounded_corner_size)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.text_field_height))
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text(
                            "注册",
                            fontSize = Typography.titleMedium.fontSize,
                        )
                    }
                }

                if (Config.isDebug) {
                    Button(onClick = {
                        viewModel.defaultLogin()
                    }) {
                        Text(text = "默认用户登录")
                    }
                }

                if (uiState.value.status != LoginStatus.INACTIVE) {
                    val cardColors = if(uiState.value.status == LoginStatus.ERROR) {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        CardDefaults.cardColors()
                    }

                    Card(
                        colors = cardColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        @Composable
                        fun StatusText(text: String) {
                            Text(
                                text = text,
                                modifier = Modifier.padding(16.dp),
                            )
                        }

                        when (uiState.value.status) {
                            LoginStatus.SUCCESS -> {
                                StatusText("登录成功")
                                navigateToHome(uiState.value.email)
                            }
                            LoginStatus.ERROR -> {
                                StatusText("登录失败")
                                StatusText(uiState.value.errorDetail)
                            }
                            LoginStatus.LOADING -> {
                                StatusText("登录中...")
                            }
                            else -> {}
                        }
                    }
                }
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