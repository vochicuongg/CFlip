package com.cuong.cflip.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuong.cflip.data.AppInfo
import com.cuong.cflip.data.CoverSettings

/**
 * The cover app grid. Tap = launch, long-press = pin/unpin favourite.
 * Designed against the real cover sizes: Flip 5/6 = 720x748, Flip 7 = 948x1048.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverLauncherScreen(
    apps: List<AppInfo>,
    favorites: Set<String>,
    settings: CoverSettings,
    onAppClick: (AppInfo) -> Unit,
    onToggleFavorite: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val columns = if (settings.columns in 3..6) GridCells.Fixed(settings.columns)
        else GridCells.Adaptive(minSize = (settings.iconSizeDp + 28).dp)
    LazyVerticalGrid(
        columns = columns,
        contentPadding = PaddingValues(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(apps, key = { it.packageName }) { app ->
            val isFav = app.packageName in favorites
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .combinedClickable(
                        onClick = { onAppClick(app) },
                        onLongClick = { onToggleFavorite(app) }
                    )
                    .padding(4.dp)
            ) {
                AppIcon(app.icon, settings.iconSizeDp)
                if (settings.showLabels) {
                    Text(
                        text = (if (isFav) "* " else "") + app.label,
                        fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
