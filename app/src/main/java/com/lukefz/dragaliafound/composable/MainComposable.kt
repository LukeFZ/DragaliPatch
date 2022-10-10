package com.lukefz.dragaliafound.composable

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun SpacedLine(width: Dp) {
    Spacer(modifier = Modifier.size(width))
    Divider(thickness = width / 2)
    Spacer(modifier = Modifier.size(width))
}


@Composable
fun AppContainer(name: String, icon: Drawable?) {
    Row(
        modifier = Modifier
            .padding(
                top = 4.dp,
                start = 4.dp,
                end = 4.dp
            )
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name)

        if (icon != null) {
            Image(
                rememberDrawablePainter(drawable = icon),
                contentDescription = "App Icon")
        }
    }
}