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
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuong.cflip.control.QuickControls
import com.cuong.cflip.data.AppInfo
import com.cuong.cflip.data.BackgroundStyles
import com.cuong.cflip.data.CoverSettings
import com.cuong.cflip.data.SettingsStore

/** Style tab (2.0): personalisation grouped into cards. All free, all instant. */
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

    Column(modifier = modifier.fillMaxSize().padding(top = 8.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Ca nhan hoa", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
            color = CFlipColors.TextPrimary)

        SectionHeader("LUOI APP")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("So cot: ${if (columns.toInt() == 0) "Tu dong" else columns.toInt().toString()}",
                    fontSize = 13.sp, color = CFlipColors.TextPrimary)
                Slider(value = columns, onValueChange = { columns = it }, valueRange = 0f..6f, steps = 5,
                    onValueChangeFinished = { update(settings.copy(columns = columns.toInt())) },
                    colors = mintSlider(), modifier = Modifier.fillMaxWidth())
                Text("Co icon: ${settings.iconSizeDp} dp", fontSize = 13.sp, color = CFlipColors.TextPrimary)
                Slider(value = settings.iconSizeDp.toFloat(),
                    onValueChange = { update(settings.copy(iconSizeDp = it.toInt())) },
                    valueRange = 40f..84f, colors = mintSlider(), modifier = Modifier.fillMaxWidth())
            }
        }

        SectionHeader("HIEN THI")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SwitchRow("Hien nhan app", settings.showLabels) { update(settings.copy(showLabels = it)) }
                SwitchRow("Yeu thich len truoc", settings.favoritesFirst) { update(settings.copy(favoritesFirst = it)) }
                SwitchRow("Dong ho 24h", settings.clock24h) { update(settings.copy(clock24h = it)) }
            }
        }

        SectionHeader("HINH NEN")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BackgroundStyles.colors.forEachIndexed { index, argb ->
                        Box(modifier = Modifier.size(38.dp).clip(CircleShape)
                            .background(Color(argb))
                            .border(if (settings.backgroundStyle == index && settings.wallpaperUri == null) 3.dp else 1.dp,
                                if (settings.backgroundStyle == index && settings.wallpaperUri == null)
                                    CFlipColors.Accent else CFlipColors.CardStroke, CircleShape)
                            .clickable { update(settings.copy(backgroundStyle = index, wallpaperUri = null)) })
                    }
                }
                Text(BackgroundStyles.names[settings.backgroundStyle.coerceIn(0, BackgroundStyles.names.lastIndex)],
                    fontSize = 12.sp, color = CFlipColors.TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { wallpaperPicker.launch("image/*") }) { Text("Chon anh nen") }
                    if (settings.wallpaperUri != null) {
                        OutlinedButton(onClick = { update(settings.copy(wallpaperUri = null)) }) { Text("Bo anh nen") }
                    }
                }
            }
        }

        SectionHeader("QUYEN")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { QuickControls.openUsageAccessSettings(context) },
                modifier = Modifier.fillMaxWidth()) {
                Text("Cap Usage Access (cho Recents that)")
            }
        }

        // Hidden apps management (v1.3): unhide brings the app back everywhere.
        SectionHeader("APP DA AN (${hiddenApps.size})")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hiddenApps.isEmpty()) {
                    Text("Chua an app nao. Giu mot app trong tab Apps de mo menu An.",
                        fontSize = 12.sp, color = CFlipColors.TextSecondary)
                }
                hiddenApps.forEach { app ->
                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(app.label, fontSize = 13.sp, color = CFlipColors.TextPrimary,
                            modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { onUnhideApp(app) }) { Text("Hien lai") }
                    }
                }
            }
        }
    }
}

@Composable
private fun mintSlider() = SliderDefaults.colors(
    thumbColor = CFlipColors.Accent,
    activeTrackColor = CFlipColors.Accent,
    inactiveTrackColor = CFlipColors.CardStroke,
)

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = CFlipColors.TextPrimary, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CFlipColors.AccentDark,
                checkedTrackColor = CFlipColors.Accent,
            ))
    }
}
