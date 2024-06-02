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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.mynote.data.LocalNoteFileApi
import com.example.mynote.ui.component.MaxWidthButton
import com.example.mynote.ui.component.TextFieldDialog
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

    viewModel.username = username

    LaunchedEffect(Unit) {
        viewModel.initProfileStateFlow()
        viewModel.insertProfile()
    }

//    profileEntity 包含用户的所有信息
//    其中头像部分为路径，需要访问文件系统获得图片本身
    val profileEntity by viewModel.profileStateFlow.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(text = "用户信息") },
                navigationIcon = {
                    Row {
                        Spacer(modifier = Modifier.width(16.dp))
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
                        }}
                    },
                actions = {
                    IconButton(onClick = {
                        viewModel.isSyncing = true
                        viewModel.downloadProfile(
                            {viewModel.isSyncing = false},
                            context
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Download",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
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
                if (profileEntity.avatar.isNotEmpty()) {
                    val avatarFile = LocalNoteFileApi.loadAvatar(viewModel.username, profileEntity.avatar, context)
                    if (avatarFile.exists() && avatarFile.length() > 0) {
                        val avatarBitmap = BitmapFactory.decodeFile(avatarFile.absolutePath).asImageBitmap()
                        Card(
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Image(
                                bitmap = avatarBitmap,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .height(64.dp)
                                    .width(64.dp)
                            )
                        }
                    } else {
                        Text(text = "未设置", color = Color.Gray)
                    }
                } else {
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
                if (profileEntity.nickname.isNotEmpty()) {
                    Text(text = profileEntity.nickname)
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
                if (profileEntity.motto.isEmpty()) {
                    Text(text = "未设置", color = Color.Gray)
                } else {
                    Text(text = profileEntity.motto)
                }
            }
            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(32.dp))
            MaxWidthButton(
                onClick = { viewModel.isChangingPassword = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondary
                ),
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
            var tempMotto by rememberSaveable {
                mutableStateOf(profileEntity.motto)
            }
            TextFieldDialog(
                title = "修改个性签名",
                text = {
                    TextField(
                        value = tempMotto,
                        onValueChange = { newValue -> tempMotto = newValue }
                    )},
                onConfirmClick = {
                    viewModel.updateMotto(tempMotto)
                    viewModel.showMottoDialog.value = false
                },
                onDismissRequest = { viewModel.showMottoDialog.value = false }
            )
        }

        if (viewModel.showNicknameDialog.value) {
            var tempNickname by rememberSaveable {
                mutableStateOf(profileEntity.nickname)
            }
            TextFieldDialog(
                title = "修改昵称",
                text = {
                    TextField(
                        value = tempNickname,
                        onValueChange = { newValue -> tempNickname = newValue }
                    )},
                onConfirmClick = {
                    viewModel.updateNickname(tempNickname)
                    viewModel.showNicknameDialog.value = false
                },
                onDismissRequest = { viewModel.showNicknameDialog.value = false }
            )
        }

        if (viewModel.isSyncing) {
            AlertDialog(
                shape = RectangleShape,
                title = { Text(text = "同步中") },
                text = { Text(text = "正在同步数据，请稍候") },
                onDismissRequest = { },
                confirmButton = { }
            )
        }

        if (viewModel.isChangingPassword) {
            var newPassword by rememberSaveable {
                mutableStateOf("")
            }
            TextFieldDialog(
                title = "修改密码",
                text = {
                    TextField(
                        value = newPassword,
                        onValueChange = { newValue -> newPassword = newValue }
                    )},
                onConfirmClick = {
                    coroutineScope.launch {
                        viewModel.changePassword(newPassword)
                        viewModel.isChangingPassword = false
                    }
                },
                onDismissRequest = { viewModel.isChangingPassword = false }
            )
        }
    }
}

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
