package com.example.mynote.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Editor() {
    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier.padding(100.dp)
            ) {
                Button(onClick = { }) {
                    Text(text = "hello")
                }
            }
        },
        modifier = Modifier.imePadding()
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            Column {
                LazyColumn {
                    items(100) {
                        TextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text(text = it.toString())}
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun TextEditorPreview() {
    Editor()
}
