package com.cuong.cflip.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun CFlipTheme(content: @Composable () -> Unit) {
    // Cover screens are AMOLED: default to dark.
    MaterialTheme(colorScheme = darkColorScheme(), content = content)
}
