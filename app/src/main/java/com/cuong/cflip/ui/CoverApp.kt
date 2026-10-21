package com.cuong.cflip.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuong.cflip.data.AppInfo
import com.cuong.cflip.data.AppRepository
import com.cuong.cflip.data.BackgroundStyles
import com.cuong.cflip.data.CoverSettings
import com.cuong.cflip.data.FavoritesStore
import com.cuong.cflip.data.HiddenAppsStore
import com.cuong.cflip.data.RecentAppsRepository
import com.cuong.cflip.data.SettingsStore
import com.cuong.cflip.display.AppLauncher
import com.cuong.cflip.media.MediaState
import com.cuong.cflip.service.NotificationState
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class CoverTab(val label: String) {
    HOME("Home"), APPS("Apps"), NOTIFS("Notif"), MEDIA("Media"), QUICK("Quick"), STYLE("Style")
}

/**
 * The complete cover experience: 6 tabs, swipe left/right to switch,
 * favourites, recents, notifications, media, quick controls, personalisation.
 * Used in-app (test on the main screen), and mirrored by CoverPresentation
 * on the real cover display.
 */
@Composable
fun CoverApp(modifier: Modifier = Modifier, targetDisplayId: Int? = null) {
    val context = LocalContext.current
    val appRepo = remember { AppRepository(context) }
    val favoritesStore = remember { FavoritesStore(context) }
    val hiddenStore = remember { HiddenAppsStore(context) }
    val settingsStore = remember { SettingsStore(context) }
    val recentRepo = remember { RecentAppsRepository(context) }

    // appsVersion bumps whenever the hidden set changes so the lists reload.
    var appsVersion by remember { mutableStateOf(0) }
    val allApps = remember(appsVersion) { appRepo.loadLaunchableApps() }
    var hidden by remember { mutableStateOf(hiddenStore.getHidden()) }
    val hiddenApps = remember(appsVersion) {
        appRepo.loadLaunchableApps(includeHidden = true).filter { it.packageName in hidden }
    }
    var menuApp by remember { mutableStateOf<AppInfo?>(null) }
    var favorites by remember { mutableStateOf(favoritesStore.getFavorites()) }
    var settings by remember { mutableStateOf(settingsStore.load()) }
    var tab by remember { mutableStateOf(CoverTab.HOME) }
    var search by remember { mutableStateOf("") }

    val notifications by NotificationState.active.collectAsState()
    val media by MediaState.current.collectAsState()

    val byPackage = remember(allApps) { allApps.associateBy { it.packageName } }
    val favApps = favorites.mapNotNull { byPackage[it] }
    val recentApps = remember(favorites) {
        recentRepo.recentPackages().mapNotNull { byPackage[it] }
    }
    val appsOrdered = remember(favorites, settings.favoritesFirst) {
        if (settings.favoritesFirst) allApps.sortedByDescending { it.packageName in favorites } else allApps
    }

    // Background: wallpaper image if chosen, else the selected colour
    val wallpaperBitmap = remember(settings.wallpaperUri) {
        settings.wallpaperUri?.let { uriStr ->
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uriStr))?.use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
        }
    }

    var dragTotal by remember { mutableStateOf(0f) }
    val tabs = CoverTab.entries

    Box(
        modifier = modifier.fillMaxSize()
            .background(Color(BackgroundStyles.colors[settings.backgroundStyle.coerceIn(0, BackgroundStyles.colors.lastIndex)]))
            .pointerInput(tab) {
                detectHorizontalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onHorizontalDrag = { _, amount -> dragTotal += amount },
                    onDragEnd = {
                        val idx = tabs.indexOf(tab)
                        if (dragTotal < -60f && idx < tabs.lastIndex) tab = tabs[idx + 1]
                        else if (dragTotal > 60f && idx > 0) tab = tabs[idx - 1]
                    }
                )
            }
    ) {
        if (wallpaperBitmap != null) {
            Image(bitmap = wallpaperBitmap.asImageBitmap(), contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    CoverTab.HOME -> Column(modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CoverClock(settings)
                        Text("Thong bao moi: ${notifications.size}", fontSize = 13.sp, color = Color.White)
                        media?.let { Text("Dang phat: ${it.title}", fontSize = 13.sp, color = Color.White, maxLines = 1) }
                        Text("Yeu thich (nhan mo, giu de mo menu)", fontSize = 13.sp, color = Color.White)
                        CoverLauncherScreen(
                            apps = favApps,
                            favorites = favorites,
                            settings = settings,
                            onAppClick = { AppLauncher.launch(context, it.packageName, targetDisplayId) },
                            onToggleFavorite = { app ->
                                favoritesStore.toggle(app.packageName)
                                favorites = favoritesStore.getFavorites()
                            },
                            onAppMenu = { menuApp = it },
                        )
                    }
                    CoverTab.APPS -> Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(value = search, onValueChange = { search = it },
                            label = { Text("Tim app") }, modifier = Modifier.fillMaxWidth().padding(8.dp))
                        if (recentApps.isNotEmpty() && search.isEmpty()) {
                            Text("  Gan day", fontSize = 13.sp, color = Color.White)
                        }
                        val base = if (search.isEmpty()) {
                            (recentApps + appsOrdered).distinctBy { it.packageName }
                        } else appsOrdered.filter { it.label.contains(search, ignoreCase = true) }
                        CoverLauncherScreen(
                            apps = base,
                            favorites = favorites,
                            settings = settings,
                            onAppClick = { AppLauncher.launch(context, it.packageName, targetDisplayId) },
                            onToggleFavorite = { app ->
                                favoritesStore.toggle(app.packageName)
                                favorites = favoritesStore.getFavorites()
                            },
                            onAppMenu = { menuApp = it },
                        )
                    }
                    CoverTab.NOTIFS -> NotificationsScreen()
                    CoverTab.MEDIA -> MediaScreen()
                    CoverTab.QUICK -> QuickControlsScreen()
                    CoverTab.STYLE -> SettingsScreen(settings = settings,
                        onSettingsChange = { settings = it },
                        hiddenApps = hiddenApps,
                        onUnhideApp = { app ->
                            hiddenStore.unhide(app.packageName)
                            hidden = hiddenStore.getHidden()
                            appsVersion++
                        })
                }
            }
            // Bottom tab bar - compact for the small cover screen
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xCC111111)),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                tabs.forEach { t ->
                    TextButton(onClick = { tab = t }) {
                        Text(t.label, fontSize = 12.sp,
                            color = if (t == tab) Color(0xFF7FE3C0) else Color.White)
                    }
                }
            }
        }

        // Long-press app menu: favourite toggle + hide (v1.3)
        menuApp?.let { app ->
            AlertDialog(
                onDismissRequest = { menuApp = null },
                title = { Text(app.label) },
                text = {
                    Column {
                        TextButton(onClick = {
                            favoritesStore.toggle(app.packageName)
                            favorites = favoritesStore.getFavorites()
                            menuApp = null
                        }) {
                            Text(if (app.packageName in favorites) "Bo ghim yeu thich" else "Ghim yeu thich")
                        }
                        if (app.packageName != context.packageName) {
                            TextButton(onClick = {
                                hiddenStore.hide(app.packageName)
                                hidden = hiddenStore.getHidden()
                                appsVersion++
                                menuApp = null
                            }) { Text("An app nay") }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { menuApp = null }) { Text("Dong") }
                },
            )
        }
    }
}

@Composable
private fun CoverClock(settings: CoverSettings) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { now = LocalDateTime.now(); delay(1000) } }
    val timeFmt = if (settings.clock24h) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("hh:mm a")
    Text(now.format(timeFmt), fontSize = 44.sp, color = Color.White)
    Text(now.format(DateTimeFormatter.ofPattern("EEE, dd/MM")), fontSize = 14.sp, color = Color.White)
}
