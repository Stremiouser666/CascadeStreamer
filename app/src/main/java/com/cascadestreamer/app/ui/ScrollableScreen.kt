package com.cascadestreamer.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ScrollableScreen(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .background(backgroundColor)
            .verticalScroll(rememberScrollState()),
        color = Color.Transparent
    ) {
        content()
    }
}
