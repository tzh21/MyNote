package com.example.mynote.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.RectangleShape

@Composable
fun TextFieldDialog(
    title: String,
    text: @Composable () -> Unit,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        shape = RectangleShape,
        title = { Text(text = title) },
        text = text,
        onDismissRequest = { },
        confirmButton = {
            MaxWidthButton(
                onClick = { onConfirmClick() },
                text = "确定")
        },
        dismissButton = {
            MaxWidthButton(
                onClick = { onDismissRequest() },
                text = "取消",
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface)
            )
        }
    )
}