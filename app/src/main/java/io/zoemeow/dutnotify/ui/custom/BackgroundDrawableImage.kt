package io.zoemeow.dutnotify.ui.custom

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun BackgroundDrawableImage(drawable: Drawable? = null) {
    // If background image not null, directly show here.
    Image(
        // https://stackoverflow.com/a/66000760
        modifier = Modifier
            .fillMaxSize()
            .blur(radius = 0.dp),
        painter = rememberDrawablePainter(
            drawable = (
                    drawable ?: ColorDrawable(MaterialTheme.colorScheme.background.toArgb()).current
                    )
        ),
        alignment = Alignment.CenterStart,
        contentDescription = "background_image",
        contentScale = ContentScale.Crop,
    )
}