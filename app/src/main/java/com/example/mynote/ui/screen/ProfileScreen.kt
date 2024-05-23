package com.example.mynote.ui.screen

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynote.R
import com.example.mynote.ui.component.MaxWidthButton
import com.example.mynote.ui.component.TextFieldDialog
import com.example.mynote.ui.theme.Typography
import com.example.mynote.ui.viewmodel.AppViewModelProvider
import com.example.mynote.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

data object ProfileRoute {
    const val base = "profile"
    const val username = "username"
    const val complete = "$base/{$username}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navigateToHome: () -> Unit,
    navigateToLogin: () -> Unit,
    username: String,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current

    viewModel.username.value = username
    viewModel.initProfileStateFlow()
    viewModel.initAvatar(context)

    val profileState = viewModel.profileStateFlow.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(text = "用户信息") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigateToHome()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }},
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            viewModel.downloadProfile(context)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Download",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .padding(scaffoldPadding)
                .padding(horizontal = 16.dp)
        ) {
            Divider(thickness = 1.dp)
            val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val selectedImageUri = result.data?.data
                    if (selectedImageUri != null) {
                        viewModel.selectImage(selectedImageUri, context)
                    }
                }
            }
            ItemRow(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable {
                        val intent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        pickImageLauncher.launch(intent)
                    }
            ) {
                Text("头像")
                Spacer(modifier = Modifier.weight(1f))
                val avatarByteArray = viewModel.avatarByteArray.value
                if (avatarByteArray.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val avatarBitmap = BitmapFactory.decodeByteArray(avatarByteArray, 0, avatarByteArray.size).asImageBitmap()
                        Image(
                            bitmap = avatarBitmap,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .height(64.dp)
                                .width(64.dp)
                        )
                    }
                }
                else {
                    Text(text = "未设置", color = Color.Gray)
                }
            }
            Divider(thickness = 1.dp)
            ItemRow {
                Text(text = "用户名", modifier = Modifier.widthIn(100.dp))
                Spacer(modifier = Modifier.weight(1f))
                Text(text = username)
            }
            Divider(thickness = 1.dp)
            ItemRow(
                modifier = Modifier.clickable { viewModel.showNicknameDialog.value = true}
            ) {
                Text(text = "昵称", modifier = Modifier.widthIn(100.dp))
                Spacer(modifier = Modifier.weight(1f))
                if (profileState.value.nickname.isNotEmpty()) {
                    Text(text = profileState.value.nickname)
                } else {
                    Text(text = "未设置", color = Color.Gray)
                }
            }
            Divider(thickness = 1.dp)
            ItemRow(
                modifier = Modifier.clickable { viewModel.showMottoDialog.value = true }
            ) {
                Text(text = "个性签名", modifier = Modifier.widthIn(100.dp))
                Spacer(modifier = Modifier.weight(1f))
                if (profileState.value.motto.isEmpty()) {
                    Text(text = "点击设置个性签名", color = Color.Gray)
                } else {
                    Text(text = profileState.value.motto)
                }
            }
            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(32.dp))
            MaxWidthButton(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                text = "修改密码"
            )
            Spacer(modifier = Modifier.height(8.dp))
            MaxWidthButton(
                onClick = { navigateToLogin() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                text = "退出登录"
            )
        }

        if (viewModel.showMottoDialog.value) {
            val tempMotto = rememberSaveable {
                mutableStateOf(profileState.value.motto)
            }
            TextFieldDialog(
                title = "修改个性签名",
                text = {
                    TextField(
                        value = tempMotto.value,
                        onValueChange = { newValue -> tempMotto.value = newValue }
                    )},
                onConfirmClick = {
                    viewModel.updateMotto(tempMotto.value)
                    viewModel.showMottoDialog.value = false
                },
                onDismissRequest = { viewModel.showMottoDialog.value = false }
            )
        }

        if (viewModel.showNicknameDialog.value) {
            val tempNickname = rememberSaveable {
                mutableStateOf(profileState.value.nickname)
            }
            TextFieldDialog(
                title = "修改昵称",
                text = {
                    TextField(
                        value = tempNickname.value,
                        onValueChange = { newValue -> tempNickname.value = newValue }
                    )},
                onConfirmClick = {
                    viewModel.updateNickname(tempNickname.value)
                    viewModel.showNicknameDialog.value = false
                },
                onDismissRequest = { viewModel.showNicknameDialog.value = false }
            )
        }

        if (viewModel.isSyncing.value) {
            AlertDialog(
                shape = RectangleShape,
                title = { Text(text = "同步中") },
                text = { Text(text = "正在同步数据，请稍候") },
                onDismissRequest = { },
                confirmButton = { }
            )
        }
    }
}

//@Composable
//fun TextFieldDialog(
//    title: String,
//    text: @Composable () -> Unit,
//    onConfirmClick: () -> Unit,
//    onDismissRequest: () -> Unit,
//) {
//    AlertDialog(
//        shape = RectangleShape,
//        title = { Text(text = title) },
//        text = text,
//        onDismissRequest = { },
//        confirmButton = {
//            ProfileButton(
//                onClick = { onConfirmClick() },
//                text = "确定")},
//        dismissButton = {
//            ProfileButton(
//                onClick = { onDismissRequest() },
//                text = "取消",
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.surface,
//                    contentColor = MaterialTheme.colorScheme.onSurface)
//            )
//        }
//    )
//}

@Composable
fun ItemRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .heightIn(min = dimensionResource(id = R.dimen.text_field_height))
            .padding(horizontal = 8.dp)
    ) {
        content()
    }
}

//@Composable
//fun MaxWidthButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    text: String = "",
//    colors: ButtonColors = ButtonDefaults.buttonColors(),
//) {
//    Button(
//        onClick = { onClick() },
//        shape = RoundedCornerShape(8.dp),
//        colors = colors,
//        modifier = modifier
//            .height(dimensionResource(id = R.dimen.text_field_height))
//            .fillMaxWidth()
//    ) {
//        Text(text = text, fontSize = Typography.titleMedium.fontSize)
//    }
//}