package com.example.mynote.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.mynote.data.Block
import com.example.mynote.data.BlockType
import com.example.mynote.data.LocalFileApi
import com.example.mynote.data.getCurrentTime
import com.example.mynote.ui.component.MyNoteTopBar
import com.example.mynote.ui.viewmodel.AppViewModelProvider
import com.example.mynote.ui.viewmodel.EditorViewModel
import kotlinx.coroutines.launch
import java.io.File

data object EditorRoute {
    const val base = "note"
    const val username = "user"
    const val category = "category"
    const val noteTitle = "noteTitle"
    const val complete = "$base/{$username}/{$category}/{$noteTitle}"
}

// 对于 IMAGE 类型的 block，data 为图片的 uri
// 对于 BODY 类型的 block，data 为文本内容
// 后续可能会添加更多类型，比如标题、引用、todolist 等

@Composable
fun EditorScreen(
    navigateUp: () -> Unit,
    username: String,
    category: String,
    fileName: String,
    viewModel: EditorViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    viewModel.username.value = username
    viewModel.category.value = category
    viewModel.fileName.value = fileName
    viewModel.loadNote(context)
    viewModel.initExoPlayer(context)

    Scaffold(
        topBar = { MyNoteTopBar(
            title = "Editor",
            canNavigateBack = true,
            navigateUp = {
//                退出时自动保存
//                目前不支持使用系统返回动作（如滑动），只点击支持返回按钮
//                可以将系统返回动作视为放弃更改；在展示时尽量避免执行系统返回动作。
                coroutineScope.launch {
                    viewModel.updateNote(context)
                    navigateUp()
                }
            }
        ) }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .focusTarget()
        ) {
            item {
                TextField(
                    value = viewModel.noteTitle.value,
                    onValueChange = { newTitle -> viewModel.noteTitle.value = newTitle },
                    placeholder = { Text("标题") }
                )
            }

            item {
                Text("创建日期：${viewModel.fileName.value}")
            }

            items(viewModel.noteBody.size) { index ->
                when (viewModel.noteBody[index].type) {
                    BlockType.BODY -> {
                        TextBlock(
                            viewModel.noteBody[index].data,
                            onValueChange = { newText ->
                                viewModel.changeText(index, newText)
                            },
                        )
                    }

                    BlockType.IMAGE -> {
                        val path = viewModel.noteBody[index].data
                        val uri = File(context.filesDir, path).toUri().toString()
                        ImageBlock(
                            imageUri = uri,
                            removeBlock = {
                                removeAndCat(viewModel.noteBody, index, context)
                            }
                        )
                    }

                    BlockType.AUDIO -> {
                        val path = viewModel.noteBody[index].data
                        val uri = File(context.filesDir, path).toUri().toString()
                        AudioBlock(
                            audioUri = uri,
                            viewModel.player,
                            removeBlock = {
                                removeAndCat(viewModel.noteBody, index, context)
                            }
                        )
                    }
                }
            }

            item {
                Column {
                    ImagePickerButton(onImageSelected = { uri ->
                        val currentTime = getCurrentTime()
                        val imageParentDir = "${viewModel.username.value}/${viewModel.category.value}/assets/image"
                        LocalFileApi.saveResource(uri, imageParentDir, currentTime, context)
                        viewModel.addBlockData(Block(BlockType.IMAGE, "$imageParentDir/$currentTime"))
                        viewModel.addBlockData(Block(BlockType.BODY, ""))
                    })

                    AudioPickerButton(onAudioSelected = { uri ->
                        val currentTime = getCurrentTime()
                        val audioParentDir = "${viewModel.username.value}/${viewModel.category.value}/assets/audio"
                        LocalFileApi.saveResource(uri, audioParentDir, currentTime, context)
                        viewModel.addBlockData(Block(BlockType.AUDIO, "$audioParentDir/$currentTime"))
                        viewModel.addBlockData(Block(BlockType.BODY, ""))
                    })

//                    保存数据到 json 文件
                    Button(onClick = {
                        coroutineScope.launch {
                            viewModel.updateNote(context)
                        }
                    }) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

@Composable
fun TextBlock(
    text: String,
    onValueChange: (String) -> Unit,
) {
    TextField(
        value = text, onValueChange = onValueChange,
        placeholder = { Text("Enter text here") },
    )
}

//isError = true, // 展示错误提示
//keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), // 将键盘的回车键定义为搜索
//给回车键定义点击搜索事件，弹出搜索内容
//keyboardActions = KeyboardActions(onSearch = { Toast.makeText(context, "search $text", Toast.LENGTH_SHORT).show() })
//singleLine = true // 重新定义回车键，一定要定义为单行，否则回车键还是换行，重定义不生效

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageBlock(
    imageUri: String,
    removeBlock: () -> Unit = {},
) {
    var showOption by rememberSaveable {
        mutableStateOf(false)
    }
    var pressOffset by remember {
        mutableStateOf(DpOffset.Zero)
    }
    var itemHeight by remember {
        mutableStateOf(0.dp)
    }
    val density = LocalDensity.current

    Box {
        GlideImage(
            model = imageUri,
            contentDescription = "It is an image.",
            modifier = Modifier
                .padding(top = 16.dp)
                .size(200.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            showOption = true
                            pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                        }
                    )
                }
                .onSizeChanged {
                    itemHeight = with(density) { it.height.toDp() }
                }
        )

        DropdownMenu(
            expanded = showOption,
            offset = pressOffset.copy(y = pressOffset.y - itemHeight),
            onDismissRequest = { showOption = false },
        ) {
            DropdownMenuItem(text = { Text("删除") }, onClick = { removeBlock() })
        }
    }
}

@Composable
fun AudioBlock(
    audioUri: String,
    player: ExoPlayer,
    removeBlock: () -> Unit = {}
) {
    var showOption by rememberSaveable {
        mutableStateOf(false)
    }

    var pressOffset by remember {
        mutableStateOf(DpOffset.Zero)
    }

    var itemHeight by remember {
        mutableStateOf(0.dp)
    }

    val density = LocalDensity.current

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            showOption = true
                            pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                        }
                    )
                }
                .onSizeChanged {
                    itemHeight = with(density) { it.height.toDp() }
                }
        ) {
            Row {
                Button(onClick = {
                    val mediaItem = MediaItem.fromUri(audioUri)
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.setMediaItem(mediaItem)
                        player.prepare()
                        player.play()
                    }
                }) {
                    Text("播放/暂停")
                }
            }
        }

        DropdownMenu(
            expanded = showOption,
            offset = pressOffset.copy(y = pressOffset.y - itemHeight),
            onDismissRequest = { showOption = false },
        ) {
            DropdownMenuItem(text = { Text("删除") }, onClick = { removeBlock() })
        }
    }
}

// 从系统相册中选择图片的按钮
// 将图片的 uri 作为参数调用 onImageSelected 函数
@Composable
fun ImagePickerButton(
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            if (selectedImageUri != null) {
                onImageSelected(selectedImageUri)
            }
        }
    }

    Button(
        onClick = {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        },
        modifier = modifier
    ) {
        // Replace with your button text
        Text("导入图片")
    }
}

@Composable
fun AudioPickerButton(
    onAudioSelected: (Uri) -> Unit,
) {
    val pickAudioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            if (selectedImageUri != null) {
                onAudioSelected(selectedImageUri)
            }
        }
    }

    Button(
        onClick = {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            pickAudioLauncher.launch(intent)
        }
    ) {
        Text("导入音频")
    }
}

fun removeAndCat(
    blockList: SnapshotStateList<Block>,
    index: Int,
    context: Context
) {
    val imagePath = blockList[index].data
    if (
        index + 1 < blockList.size &&
        blockList[index + 1].type == BlockType.BODY &&
        index - 1 >= 0 &&
        blockList[index - 1].type == BlockType.BODY
    ) {
        val previousBlock = blockList[index - 1].data
        val nextBlock = blockList[index + 1].data
        blockList[index - 1] = blockList[index - 1].copy(data = previousBlock + "\n" + nextBlock)
        blockList.removeAt(index + 1)
    }
    blockList.removeAt(index)
    LocalFileApi.deleteFile(imagePath, context)
}

// 后续考虑使用的富文本编辑器，暂时不用删
//@Composable
//fun RichEditor() {
//    val basicRichTextState = rememberRichTextState()
//    var isChecked by remember { mutableStateOf(false) }
//    var markdownString by remember { mutableStateOf("") }
//    Column {
//        Checkbox(
//            checked = isChecked,
//            onCheckedChange = {
//                isChecked = it
//                if (isChecked) {
//                    basicRichTextState.toggleSpanStyle(
//                        SpanStyle(
//                            fontStyle = FontStyle.Italic
//                        )
//                    )
//                }
//                else {
//                    basicRichTextState.toggleSpanStyle(
//                        SpanStyle(
//                            fontStyle = FontStyle.Normal
//                        )
//                    )
//                }
//            },
//            modifier = Modifier.padding(end = 8.dp)
//        )
//        Button(onClick = { markdownString = basicRichTextState.toMarkdown() }) {
//            Text("Save as markdown")
//        }
//        Text(text = markdownString, modifier = Modifier.padding(8.dp))
//        BasicRichTextEditor(
//            state = basicRichTextState,
//            modifier = Modifier.fillMaxWidth()
//        )
//    }
//}