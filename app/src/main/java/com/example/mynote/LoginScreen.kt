package com.example.mynote

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

private const val PREFS_NAME = "MyAppPrefs"
private const val KEY_EMAIL = "email"
private const val KEY_PASSWORD = "password"

@Composable
fun LoginScreen(
    navController: NavController,
    onLoginTriggered: () -> Unit
) {
    var loggedIn by remember { mutableStateOf(false) }
    var savedEmail by remember { mutableStateOf<String?>(null) }
    var savedPassword by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    if (!loggedIn) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        savedEmail = sharedPreferences.getString(KEY_EMAIL, null)
        savedPassword = sharedPreferences.getString(KEY_PASSWORD, null)
    }

    var email by remember { mutableStateOf(TextFieldValue(savedEmail ?: "")) }
    var password by remember { mutableStateOf(TextFieldValue(savedPassword ?: "")) }
    var saveCredentialsChecked by remember { mutableStateOf(true)}

    if (loggedIn) {
        onLoginTriggered()
    }
    else {
//        TODO UI
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("邮箱") },
                modifier = Modifier.padding(16.dp)
            )
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(16.dp)
            )
            Row {
                Text("记住用户")
                Checkbox(
                    checked = saveCredentialsChecked,
                    onCheckedChange = { saveCredentialsChecked = it },
                )
            }
            Button(
                onClick = {
                    val loginSuccess = performLogin(email.text, password.text)
                    if (loginSuccess) {
                        onLoginTriggered()
                        loggedIn = true
                        if (saveCredentialsChecked) {
                            saveCredentials(context, email.text, password.text)
                        } else {
                            saveCredentials(context, "", "")
                        }
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("登录")
            }
        }
    }
}

fun saveCredentials(context: Context, email: String, password: String) {
    val sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString(KEY_EMAIL, email)
    editor.putString(KEY_PASSWORD, password)
    editor.apply()
}

fun performLogin(email: String, password: String): Boolean {
    // TODO 向后端发送登录请求
    return true
}
