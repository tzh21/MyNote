package com.example.mynote.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynote.data.Block
import com.example.mynote.data.BlockType
import com.example.mynote.data.LocalNoteFileApi
import com.example.mynote.data.simplifyTime
import com.example.mynote.ui.component.TextFieldDialog
import com.example.mynote.ui.theme.Typography
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
    navigateToEditor: (category: String, fileName: String) -> Unit,
    username: String,
    category: String,
    fileName: String,
    viewModel: EditorViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    viewModel.username = username
    viewModel.category = category
    viewModel.fileName = fileName

    LaunchedEffect(Unit) {
        viewModel.loadCategoryList(username)
        viewModel.loadNote(context)
        viewModel.loadLastModifiedTime()
        viewModel.initExoPlayer(context)
    }

    BackHandler {
        coroutineScope.launch {
            viewModel.saveNote(context)
            navigateToHome()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        Row {
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(onClick = { coroutineScope.launch {
                                viewModel.saveNote(context)
                                navigateToHome()
                            }}) {
                                Icon(
                                    imageVector = Icons.Filled.Home,
                                    contentDescription = "Back to home",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        Box {
                            var categoriesExpanded by remember { mutableStateOf(false) }
                            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                                AssistChip(
                                    onClick = { categoriesExpanded = true },
                                    label = { Text(text = viewModel.category) },
                                    border = null,
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                )
                            }
                            DropdownMenu(
                                expanded = categoriesExpanded,
                                onDismissRequest = { categoriesExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(text = "新建分类") },
                                    onClick = { viewModel.showNewCategoryDialog = true }
                                )
                                val categoryList by viewModel.categoryList.collectAsState()
                                categoryList.forEach { categoryItem ->
                                    DropdownMenuItem(
                                        text = { Text(categoryItem) },
                                        onClick = {
                                            coroutineScope.launch {
                                                categoriesExpanded = false
                                                viewModel.moveNote(categoryItem, context)
                                                navigateToEditor(categoryItem, viewModel.fileName)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { coroutineScope.launch {
                            viewModel.uploadNote(context)
                        }}) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                )
            }
                 },
    ) { scaffoldPadding ->
        val focusManager = LocalFocusManager.current
        Box(
            modifier = Modifier
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                .padding(scaffoldPadding)
                .imePadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .weight(1f)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
//                    笔记标题
                    item {
                        val titleTextSize = Typography.headlineMedium.fontSize
                        BasicTextField(
                            value = viewModel.noteTitle,
                            onValueChange = { newTitle -> viewModel.noteTitle = newTitle },
                            textStyle = LocalTextStyle.current.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = titleTextSize,
                            ),
                            decorationBox = { innerTextField ->
                                if (viewModel.noteTitle.isEmpty()) {
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
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
//                    修改时间
                    item {
                        val lastModifiedTime by viewModel.lastModifiedTime.collectAsState()
                        Text(
                            "上次修改：${simplifyTime(lastModifiedTime)}",
                            style = TextStyle(color = Color.LightGray),
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
//                    笔记正文
                    items(viewModel.noteBody.size) { index ->
                        val blockData = viewModel.noteBody[index].data
                        when(viewModel.noteBody[index].type) {
                            BlockType.BODY -> {
                                BodyBlock(
                                    value = blockData,
                                    onValueChanged = { newText -> viewModel.noteBody[index] = viewModel.noteBody[index].copy(data = newText) },
                                    onNext = {
                                        viewModel.noteBody.add(index + 1, Block(BlockType.BODY, ""))
                                        focusManager.moveFocus(FocusDirection.Down)
                                    },
                                    onKeyEvent = { keyEvent ->
                                        if (keyEvent.key == Key.Backspace && blockData.isEmpty() && viewModel.noteBody.size > 1) {
                                            viewModel.noteBody.removeAt(index)
                                            focusManager.moveFocus(FocusDirection.Up)
                                        }
                                        false
                                    },
                                    onFocusChanged = { focusState ->
                                        if (focusState.isFocused) {
                                            viewModel.currentBlockIndex = index
                                            if (index == viewModel.noteBody.size - 1) {
                                                viewModel.noteBody.add(Block(BlockType.BODY, ""))
                                            }
                                        }
                                    },
//                                    onImageSelected = { viewModel.insertImage(it, context) },
//                                    onAudioSelected = { viewModel.insertAudio(it, context) }
                                )
                            }
                            BlockType.IMAGE -> {
                                ImageBlock(
                                    username = viewModel.username,
                                    fileName = blockData,
                                    context = context,
                                    removeBlock = {
                                        viewModel.noteBody.removeAt(index)
                                        viewModel.deleteImage(blockData, context)
                                    }
                                )
                            }
                            BlockType.AUDIO -> {
                                val uri = LocalNoteFileApi.loadAudio(username, blockData, context).toUri()
                                val isPlaying = (viewModel.isPlaying && viewModel.currentAudioUri == uri)
                                AudioBlock(
                                    isPlaying = isPlaying,
                                    removeBlock = {
                                        viewModel.noteBody.removeAt(index)
                                        viewModel.deleteAudio(blockData, context)
                                    },
                                    playAudio = {viewModel.playOrPauseAudio(uri)}
                                )
                            }
                        }
                    }
                }
                Divider()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    ImagePicker { viewModel.insertImage(it, context) }
                    AudioPicker { viewModel.insertAudio(it, context) }
                    CameraButton { viewModel.insertImage(it, context) }
                    AudioRecorderButton { viewModel.insertAudio(it, context) }
                }
            }
        }

        if (viewModel.showNewCategoryDialog) {
            var newCategory by remember { mutableStateOf("") }
            TextFieldDialog(
                title = "新建分类",
                text = {TextField(value = newCategory, onValueChange = { newCategory = it })},
                onConfirmClick = {
                    if (newCategory.isNotEmpty()) {
                        viewModel.createCategory(newCategory)
                    }
                    viewModel.showNewCategoryDialog = false
                },
                onDismissRequest = {
                    viewModel.showNewCategoryDialog = false
                }
            )
        }
    }
}

@Composable
fun BodyBlock(
    value: String,
    onValueChanged: (String) -> Unit,
    onNext: () -> Unit,
    onKeyEvent: (KeyEvent) -> Boolean,
    onFocusChanged: (FocusState) -> Unit,
) {
    val normalTextSize = Typography.bodyMedium.fontSize
    val normalTextLineHeight = normalTextSize * 1.5f
    Column {
        BasicTextField(
            value = value,
            onValueChange = { onValueChanged(it) },
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = normalTextSize,
                lineHeight = normalTextLineHeight,
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext() }
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
                .fillMaxWidth()
                .onKeyEvent { onKeyEvent(it) }
                .padding(vertical = 8.dp)
                .onFocusChanged {
                    onFocusChanged(it)
                }
        )
    }
}

@Composable
fun ImageBlock(
    username: String, fileName: String,
    context: Context, removeBlock: () -> Unit
) {
    val imageFile = LocalNoteFileApi.loadImage(username, fileName, context)
    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)?.asImageBitmap()
    if (bitmap == null) {
        Log.e("ImageBlock", "Failed to decode image file $fileName.")
        return
    }
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier

        ) {
            Image(
                bitmap = bitmap,
                contentDescription = "It is an image.",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) { detectTapGestures(onLongPress = { expanded = true }) }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(text = { Text("删除") }, onClick = { removeBlock() })
        }
    }
}

@Composable
fun ImagePicker(onImageSelected: (Uri) -> Unit) {
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
fun CameraButton(onImageCaptured: (Uri) -> Unit) {
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
fun AudioBlock(
    isPlaying: Boolean,
    removeBlock: () -> Unit = {},
    playAudio: () -> Unit = {}
) {
    var expanded by remember {mutableStateOf(false)}

    Box(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier
                .pointerInput(Unit) { detectTapGestures(onLongPress = { expanded = true }) }
                .height(56.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 8.dp),
            ) {
                IconButton(onClick = {playAudio()}) {
                    if (isPlaying) {
                        Icon(
                            imageVector = Icons.Default.PauseCircleOutline,
                            contentDescription = "play the audio",
                        )
                    }
                    else {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = "pause the audio",
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {removeBlock()}) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete audio"
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(text = { Text("删除") }, onClick = { removeBlock() })
        }
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

@Composable
fun AudioPicker(onAudioSelected: (Uri) -> Unit) {
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
