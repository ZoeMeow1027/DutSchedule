package io.zoemeow.dutnotify.ui.custom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NewsScreenCore(
    content: @Composable () -> Unit
) {
    // A box container using the 'background' color from the theme
    Box(modifier = Modifier.fillMaxSize()) {
        content()
    }
}
