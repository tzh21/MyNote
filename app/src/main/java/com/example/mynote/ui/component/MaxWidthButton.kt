package com.example.mynote.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.example.mynote.R
import com.example.mynote.ui.theme.Typography

@Composable
fun MaxWidthButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "",
    colors: ButtonColors = ButtonDefaults.buttonColors(),
) {
    Button(
        onClick = { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = colors,
        modifier = modifier
            .height(dimensionResource(id = R.dimen.text_field_height))
            .fillMaxWidth()
    ) {
        Text(text = text, fontSize = Typography.titleMedium.fontSize)
    }
}