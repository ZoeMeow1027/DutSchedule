package io.zoemeow.dutapp.android.ui.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import io.zoemeow.dutapp.android.ui.custom.BackgroundImage
import io.zoemeow.dutapp.android.viewmodel.GlobalViewModel

val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun DUTAppForAndroidTheme(
    // Set app mode layout
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set app black background (for AMOLED)
    blackTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val globalViewModel = GlobalViewModel.getInstance()

    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    // Set black background for AMOLED.
    if (darkTheme && blackTheme) {
        colorScheme = colorScheme.copy(background = Color.Black)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = darkTheme
        }
    }

    // If background image not null, directly show here.
//    if (globalViewModel.backgroundDrawable.value != null) {
//    }
    BackgroundImage(drawable = if (globalViewModel.backgroundDrawable.value != null)
        globalViewModel.backgroundDrawable.value else
        ColorDrawable(colorScheme.background.hashCode()))


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
