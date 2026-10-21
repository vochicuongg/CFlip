package com.cuong.cflip.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuong.cflip.control.QuickControls
import com.cuong.cflip.data.AppInfo
import com.cuong.cflip.data.BackgroundStyles
import com.cuong.cflip.data.CoverSettings
import com.cuong.cflip.data.SettingsStore

/** Personalisation: grid, icon size, labels, background colour, cover wallpaper. All free, all instant. */
@Composable
fun SettingsScreen(
    settings: CoverSettings,
    onSettingsChange: (CoverSettings) -> Unit,
    modifier: Modifier = Modifier,
    hiddenApps: List<AppInfo> = emptyList(),
    onUnhideApp: (AppInfo) -> Unit = {},
) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context) }
    var columns by remember { mutableStateOf(settings.columns.toFloat()) }

    val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val next = settings.copy(wallpaperUri = uri.toString())
            store.save(next); onSettingsChange(next)
        }
    }

    fun update(next: CoverSettings) { store.save(next); onSettingsChange(next) }

    Column(modifier = modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Personalize", fontSize = 18.sp)

        Text("Grid columns: ${if (columns.toInt() == 0) "Auto" else columns.toInt().toString()} (0 = Auto, 3-6)", fontSize = 13.sp)
        Slider(value = columns, onValueChange = { columns = it }, valueRange = 0f..6f, steps = 5,
            onValueChangeFinished = { update(settings.copy(columns = columns.toInt())) },
            modifier = Modifier.fillMaxWidth())

        Text("Icon size: ${settings.iconSizeDp} dp", fontSize = 13.sp)
        Slider(value = settings.iconSizeDp.toFloat(),
            onValueChange = { update(settings.copy(iconSizeDp = it.toInt())) },
            valueRange = 40f..84f, modifier = Modifier.fillMaxWidth())

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Show app labels", fontSize = 14.sp)
            Switch(checked = settings.showLabels, onCheckedChange = { update(settings.copy(showLabels = it)) })
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Favorites first", fontSize = 14.sp)
            Switch(checked = settings.favoritesFirst, onCheckedChange = { update(settings.copy(favoritesFirst = it)) })
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("24h clock", fontSize = 14.sp)
            Switch(checked = settings.clock24h, onCheckedChange = { update(settings.copy(clock24h = it)) })
        }

        Text("Background", fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BackgroundStyles.colors.forEachIndexed { index, argb ->
                Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(Color(argb))
                    .border(if (settings.backgroundStyle == index && settings.wallpaperUri == null) 3.dp else 0.dp,
                        Color.White, CircleShape)
                    .clickable { update(settings.copy(backgroundStyle = index, wallpaperUri = null)) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = { wallpaperPicker.launch("image/*") }) { Text("Pick wallpaper") }
            if (settings.wallpaperUri != null) {
                OutlinedButton(onClick = { update(settings.copy(wallpaperUri = null)) }) { Text("Remove wallpaper") }
            }
        }

        OutlinedButton(onClick = { QuickControls.openUsageAccessSettings(context) }) {
            Text("Cap Usage Access (cho Recents that)")
        }

        // Hidden apps management (v1.3): unhide brings the app back everywhere.
        Text("App da an (${hiddenApps.size})", fontSize = 14.sp)
        if (hiddenApps.isEmpty()) {
            Text("Chua an app nao. Giu mot app trong tab Apps de mo menu An.", fontSize = 12.sp)
        }
        hiddenApps.forEach { app ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(app.label, fontSize = 13.sp)
                OutlinedButton(onClick = { onUnhideApp(app) }) { Text("Hien lai") }
            }
        }
    }
}
