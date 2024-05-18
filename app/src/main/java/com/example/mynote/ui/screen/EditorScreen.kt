package com.example.mynote.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewTreeObserver
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.mynote.data.Block
import com.example.mynote.data.BlockType
import com.example.mynote.data.LocalFileApi
import com.example.mynote.data.NoteLoaderApi
import com.example.mynote.data.getCurrentTime
import com.example.mynote.ui.theme.DarkColorScheme
import com.example.mynote.ui.theme.LightColorScheme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    navigateToHome: () -> Unit,
    navigateToEditor: (String, String) -> Unit,
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
        topBar = {
            TopAppBar(
                title = {  },
                navigationIcon = {
                    IconButton(onClick = {
                            coroutineScope.launch {
                                viewModel.saveNote(context)
                                navigateToHome()
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Back to home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    viewModel.initCategoryList(context)
                    Row(
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                expanded = true
                                      },
                            label = { Text(text = viewModel.category.value) },
                            border = null,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.padding(end = 8.dp),
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {expanded = false}
                        ) {
                            viewModel.categoryList.forEach { categoryItem ->
                                DropdownMenuItem(
                                    text = { Text(categoryItem) },
                                    onClick = {
                                        coroutineScope.launch {
                                            expanded = false
                                            viewModel.moveNote(
                                                categoryItem,
                                                context
                                            )
                                            navigateToEditor(categoryItem, viewModel.fileName.value)
                                        }
                                    }
                                )
                            }
                        }

                        IconButton(onClick = {
                            coroutineScope.launch {
                                viewModel.upload("${viewModel.username.value}/${viewModel.category.value}/${viewModel.fileName.value}", context)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Cloud upload",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        val focusManager = LocalFocusManager.current
        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                    }
                }
                .padding(paddingValues)
        ) {
            Column {

//                监听键盘状态，以确保底部按钮不被键盘遮挡
                val view = LocalView.current
                val density = LocalDensity.current
                val isImeVisible = remember { mutableStateOf(false) }
                val keypadHeightPx = remember { mutableIntStateOf(0) }
                DisposableEffect(view) {
                    val onGlobalListener = ViewTreeObserver.OnGlobalLayoutListener {
                        val rect = android.graphics.Rect()
                        view.getWindowVisibleDisplayFrame(rect)
                        val screenHeight = view.rootView.height
                        keypadHeightPx.value = screenHeight - rect.bottom
                        isImeVisible.value = keypadHeightPx.value > screenHeight * 0.15
                    }
                    view.viewTreeObserver.addOnGlobalLayoutListener(onGlobalListener)

                    onDispose {
                        view.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalListener)
                    }
                }

//                var currentBlockIndex = remember {
//                    mutableStateOf(0)
//                }

                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .weight(1f)
                ) {
//                    标题栏
                    item {
                        val titleTextSize = 32.sp
                        BasicTextField(
                            value = viewModel.noteTitle.value,
                            onValueChange = { newTitle -> viewModel.noteTitle.value = newTitle },
                            textStyle = TextStyle(fontSize = titleTextSize),
                            decorationBox = { innerTextField ->
                                if (viewModel.noteTitle.value.isEmpty()) {
                                    Text(
                                        text = "笔记标题",
                                        style = TextStyle(
                                            color = Color.Gray,
                                            fontSize = titleTextSize
                                        ),
                                    )
                                }
                                innerTextField()
                            },
                        )
                    }

//                    修改时间
                    item {
                        Text(
                            "上次修改：${viewModel.noteEntity?.lastModifiedTime ?: "未知"}",
                            style = TextStyle(color = Color.LightGray),
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }

//                    笔记正文
                    items(viewModel.noteBody.size) { index ->
                        when (viewModel.noteBody[index].type) {
                            BlockType.BODY -> {
                                val normalTextSize = 16.sp
                                val normalTextLineHeight = normalTextSize * 1.5f
                                BasicTextField(
                                    value = viewModel.noteBody[index].data,
                                    onValueChange = { newText ->
                                        viewModel.changeText(index, newText)
                                    },
                                    textStyle = TextStyle(
                                        fontSize = normalTextSize,
                                        lineHeight = normalTextLineHeight,
                                        lineBreak = LineBreak.Paragraph
                                    ),
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = {
                                            viewModel.noteBody.add(index + 1, Block(BlockType.BODY, ""))
                                            focusManager.moveFocus(FocusDirection.Down)
                                        }
                                    ),
                                    modifier = Modifier
                                        .onKeyEvent {
                                            if (it.key == Key.Backspace && viewModel.noteBody[index].data.isEmpty() && viewModel.noteBody.size > 1) {
                                                viewModel.noteBody.removeAt(index)
                                                focusManager.moveFocus(FocusDirection.Up)
                                            }
                                            false
                                        }
                                        .padding(bottom = 16.dp)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                viewModel.currentBlockIndex.value = index
                                            }
                                        }
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
                }
                Row(
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = if (isImeVisible.value) with(density) { keypadHeightPx.value.toDp() + 16.dp } else 16.dp,
                        )
                ) {
                    CameraButton { uri ->
                        coroutineScope.launch {
                            viewModel.saveImage(uri, context)
                        }
                    }

                    ImagePickerButton { uri ->
                        coroutineScope.launch {
                            viewModel.saveImage(uri, context)
                        }
                    }

                    AudioRecorderButton { uri ->
                        viewModel.saveAudio(uri, context)
                    }

                    AudioPickerButton { uri ->
                        viewModel.saveAudio(uri, context)
                    }
                }
            }
        }
    }
}

//fun saveImage(
//    username: String,
//    category: String,
//    fileName: String,
//    noteBody: SnapshotStateList<Block>,
//    uri: Uri,
//    currentBlockIndex: Int,
//    context: Context
//) {
//    val currentTime = getCurrentTime()
//    val path =
//        "$username/$category/assets/$fileName/image/$currentTime"
//    LocalFileApi.saveResource(uri, path, context)
//    noteBody.add(currentBlockIndex + 1, Block(BlockType.IMAGE, path))
//    if (noteBody.size <= currentBlockIndex + 2) {
//        noteBody.add(currentBlockIndex + 2, Block(BlockType.BODY, ""))
//    }
//}

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
) {
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            if (selectedImageUri != null) {
                onImageSelected(selectedImageUri)
            }
        }
    }

    IconButton(
        onClick = {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "Image",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

var tempImageFile = File("")

@Composable
fun CameraButton(
    onImageCaptured: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            // Assuming you are saving the image to a temporary file
            val imageUri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                tempImageFile
            )
            onImageCaptured(imageUri)
        }
    }

    IconButton(
        onClick = {
            val outputDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val photoFile = File.createTempFile(
                "temp_", /* prefix */
                ".jpg", /* suffix */
                outputDirectory /* directory */
            )
            tempImageFile = photoFile // Assigning the temporary file to a global variable
            val photoURI = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                photoFile
            )
            takePictureLauncher.launch(photoURI)
        }
    ) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = "Camera",
            tint = MaterialTheme.colorScheme.primary
        )
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

    IconButton(
        onClick = {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            pickAudioLauncher.launch(intent)
        }
    ) {
        Icon(
            imageVector = Icons.Default.AudioFile,
            contentDescription = "Audio",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AudioRecorderButton(
    onAudioRecorded: (Uri) -> Unit,
) {
    val recordAudioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val audioUri = result.data?.data
            if (audioUri != null) {
                onAudioRecorded(audioUri)
            }
        }
    }

    IconButton(
        onClick = {
            val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
            recordAudioLauncher.launch(intent)
        }
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Record Audio",
            tint = MaterialTheme.colorScheme.primary
        )
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