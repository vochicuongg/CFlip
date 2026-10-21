package com.cuong.cflip.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.cuong.cflip.control.QuickControls
import com.cuong.cflip.cover.CoverPresentation
import com.cuong.cflip.display.DisplayRepository
import com.cuong.cflip.service.CoverForegroundService

/** Main-screen home of CFlip Launcher: setup, permissions, service, cover test. */
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val displayRepo = remember { DisplayRepository(context) }
    var showCover by remember { mutableStateOf(false) }
    var presentation by remember { mutableStateOf<CoverPresentation?>(null) }

    if (showCover) {
        Column(modifier = Modifier.fillMaxSize()) {
            Button(onClick = { showCover = false }) { Text("< Quay lai") }
            CoverApp(modifier = Modifier.weight(1f),
                targetDisplayId = displayRepo.secondaryDisplay()?.displayId)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("CFlip Launcher", fontSize = 26.sp)
        Text("Cover launcher ca nhan cua Cuong - cho Galaxy Z Flip5. Moi tinh nang deu mien phi, khong paywall.", fontSize = 13.sp)
        Text("Man hinh phat hien: " + displayRepo.listDisplays().joinToString { "#${it.id} ${it.name} ${it.widthPx}x${it.heightPx}" }, fontSize = 12.sp)

        Text("1) Cap quyen", fontSize = 18.sp)
        Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }) {
            Text("Notification Access (thong bao + media)") }
        Button(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
            Text("Accessibility (khoa may, Recents, Quick Settings)") }
        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.packageName)))
        }) { Text("Overlay (hien tren ung dung khac)") }
        OutlinedButton(onClick = { QuickControls.openUsageAccessSettings(context) }) {
            Text("Usage Access (danh sach app gan day that)") }
        OutlinedButton(onClick = { QuickControls.openWriteSettingsPermission(context) }) {
            Text("Modify system settings (chinh do sang)") }

        Text("2) Cover service", fontSize = 18.sp)
        Button(onClick = {
            ContextCompat.startForegroundService(context, Intent(context, CoverForegroundService::class.java))
            Toast.makeText(context, "CFlip service started - gap may lai de thu", Toast.LENGTH_LONG).show()
        }) { Text("Start foreground service") }
        OutlinedButton(onClick = { context.stopService(Intent(context, CoverForegroundService::class.java)) }) {
            Text("Stop foreground service") }

        Text("3) Test cover", fontSize = 18.sp)
        Button(onClick = { showCover = true }) { Text("Mo CFlip ngay tren man hinh chinh (test khong can gap)") }
        Button(onClick = {
            val secondary = displayRepo.secondaryDisplay()
            if (secondary == null) {
                Toast.makeText(context,
                    "Chua thay man hinh phu. Tren Flip5 that: gap may lai. Tren emulator: them secondary display.",
                    Toast.LENGTH_LONG).show()
            } else {
                presentation?.dismiss()
                presentation = CoverPresentation(context, secondary).also {
                    runCatching { it.show() }.onFailure { e ->
                        Toast.makeText(context, "Presentation bi chan: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }) { Text("Show Presentation on secondary display") }
        Text("Widget duong chinh thong: Good Lock > MultiStar > I Love Galaxy Foldable > Launcher Widget > them widget CFlip Launcher. Sau do vuot cover sang trang widget de dung.", fontSize = 12.sp)
    }
}
