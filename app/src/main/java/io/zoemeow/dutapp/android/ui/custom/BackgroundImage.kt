package io.zoemeow.dutapp.android.ui.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun BackgroundImage(painter: MutableState<Painter?>) {
    // If background image not null, directly show here.
    if (painter.value != null) {
        Image(
            // https://stackoverflow.com/a/66000760
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 0.dp),
            painter = painter.value!!,
            alignment = Alignment.CenterStart,
            contentDescription = "background_image",
            contentScale = ContentScale.Crop,
        )
    }
}