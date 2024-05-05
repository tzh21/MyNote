package com.example.mynote.ui.screen

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

const val EditorArg = "noteId"
const val EditorRoute = "note/$EditorArg"

enum class BlockType {
    BODY,
    IMAGE
}

// 对于 IMAGE 类型的 block，data 为图片的 uri
// 对于 BODY 类型的 block，data 为文本内容
// 后续可能会添加更多类型，比如标题、引用、todolist 等
data class BlockData(
    val type: BlockType,
    val data: String
)

@Composable
fun EditorScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

//    hello world 用来测试 LazyColumn 是否 work，暂时不要删
//    后续会考虑把所有的数据和操作移入 view model 中
    var blockDataList by remember {
        mutableStateOf(
            mutableListOf(
                mutableStateOf(BlockData(BlockType.BODY, "hello")),
                mutableStateOf(BlockData(BlockType.BODY, "world"))
            )
        )
    }

    //    将 blockDataList 中下标为 index 的元素的 data 字段修改为 newText
    fun changeText(index: Int, newData: String) {
        val t = blockDataList[index].value.type
        blockDataList[index].value = BlockData(t, newData)
    }

//    将 blockDataList 保存到本地 json 文件中
//    目录暂时为 test_dir，后续会根据笔记的分类调整
//    文件名暂时为 test_file.json，后续会根据笔记的标题、创建时间调整
    fun saveBlockDataList(prefix: String, fileName: String) {
        val gson = Gson()
        val jsonList = blockDataList.map {
            BlockData(it.value.type, it.value.data)
        }
        val jsonString = gson.toJson(jsonList)

        val directory = File(context.filesDir, prefix)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)

        // 创建文件并写入内容
        FileOutputStream(file).use { stream ->
            stream.write(jsonString.toByteArray())
        }
    }

    fun readBlockDataList(prefix: String, fileName: String) {
        val gson = Gson()
        val jsonString = File(context.filesDir, "$prefix/$fileName").readText()
        val listType = object : TypeToken<List<BlockData>>() {}.type
        val newBlockDataList = gson.fromJson<List<BlockData>>(jsonString, listType)
        blockDataList.clear()
        blockDataList = blockDataList.toMutableList().apply {
            addAll(newBlockDataList.map { mutableStateOf(it) })
        }
    }

    LazyColumn {
        items(blockDataList.size) { index ->
            when (blockDataList[index].value.type) {
                BlockType.BODY -> {
                    TextBlock(
                        blockDataList[index].value.data,
                        onValueChange = { newText -> changeText(index, newText) }
                    )
                }
                BlockType.IMAGE -> {
                    ImageBlock(
                        blockDataList[index].value.data,
                        onValueChange = { changeText(index, it) }
                    )
                }
            }
        }

        item {
            Row {
                IconButton(onClick = {
                    blockDataList = blockDataList.toMutableList().apply {
                        add(mutableStateOf(BlockData(BlockType.BODY, "new text")))
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Text")
                }
                ImagePickerButton(onImageSelected = { uri ->
                    blockDataList = blockDataList.toMutableList().apply {
                        add(mutableStateOf(BlockData(BlockType.IMAGE, uri)))
                    }
                })
//                保存数据到 json 文件
                IconButton(onClick = {
                    saveBlockDataList("test_dir", "test_file.json")
                }) {
                    Icon(Icons.Default.Done, contentDescription = "Save Data")
                }
//                从 json 文件中加载数据
                IconButton(onClick = {
                    readBlockDataList("test_dir", "test_file.json")
                }) {
                    Icon(Icons.Default.KeyboardArrowUp, "Load Data")
                }
            }
        }
    }
}

@Composable
fun TextBlock(
    text: String,
    onValueChange: (String) -> Unit
) {
    TextField(value = text, onValueChange = onValueChange)
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageBlock(
    imageUri: String,
    onValueChange: (String) -> Unit
) {
    GlideImage(
        model = imageUri,
        contentDescription = "It is an image.",
        modifier = Modifier
            .padding(top = 16.dp)
            .size(200.dp)
    )
}

// 从系统相册中选择图片的按钮
// 将图片的 uri 作为参数调用 onImageSelected 函数
@Composable
fun ImagePickerButton(
    onImageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            onImageSelected(selectedImageUri.toString())
        }
    }

    IconButton(
        onClick = {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        },
        modifier = modifier
    ) {
        // Replace with your button text
        Icon(Icons.Default.MailOutline, contentDescription = "Add Image")
    }
}

@Preview
@Composable
fun EditorScreenPreview() {
    EditorScreen()
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