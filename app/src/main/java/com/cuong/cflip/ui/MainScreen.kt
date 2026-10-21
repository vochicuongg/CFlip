package com.cuong.cflip.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.cuong.cflip.control.QuickControls
import com.cuong.cflip.cover.CoverPresentation
import com.cuong.cflip.display.DisplayRepository
import com.cuong.cflip.service.CoverForegroundService

/** Main-screen home of CFlip Launcher (2.0): brand header, setup steps in cards. */
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val displayRepo = remember { DisplayRepository(context) }
    var showCover by remember { mutableStateOf(false) }
    var presentation by remember { mutableStateOf<CoverPresentation?>(null) }

    if (showCover) {
        Column(modifier = Modifier.fillMaxSize().background(CFlipColors.Bg)) {
            TextButton(onClick = { showCover = false }) { Text("< Quay lai", color = CFlipColors.Accent) }
            CoverApp(modifier = Modifier.weight(1f),
                targetDisplayId = displayRepo.secondaryDisplay()?.displayId)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().background(CFlipColors.Bg)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Brand header
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp))
                .background(CFlipColors.Accent), contentAlignment = Alignment.Center) {
                Text("C", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = CFlipColors.AccentDark)
            }
            Column {
                Text("CFlip Launcher", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = CFlipColors.TextPrimary)
                Text("Cover launcher cua Cuong - Z Flip5", fontSize = 13.sp,
                    color = CFlipColors.TextSecondary)
            }
        }
        Text("Moi tinh nang deu mien phi, khong paywall. Ban 2.0 - giao dien moi.",
            fontSize = 13.sp, color = CFlipColors.TextSecondary)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Man hinh phat hien: " +
                displayRepo.listDisplays().joinToString { "#${it.id} ${it.name} ${it.widthPx}x${it.heightPx}" },
                fontSize = 12.sp, color = CFlipColors.TextSecondary)
        }

        SectionHeader("1 - CAP QUYEN")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Notification Access (thong bao + media)") }
                Button(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Accessibility (khoa may, Recents, Quick Settings)") }
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.packageName)))
                }, modifier = Modifier.fillMaxWidth()) { Text("Overlay (hien tren ung dung khac)") }
                OutlinedButton(onClick = { QuickControls.openUsageAccessSettings(context) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Usage Access (danh sach app gan day that)") }
                OutlinedButton(onClick = { QuickControls.openWriteSettingsPermission(context) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Modify system settings (chinh do sang)") }
            }
        }

        SectionHeader("2 - COVER SERVICE")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    ContextCompat.startForegroundService(context, Intent(context, CoverForegroundService::class.java))
                    Toast.makeText(context, "CFlip service started - gap may lai de thu", Toast.LENGTH_LONG).show()
                }, modifier = Modifier.fillMaxWidth()) { Text("Start foreground service") }
                OutlinedButton(onClick = { context.stopService(Intent(context, CoverForegroundService::class.java)) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Stop foreground service") }
            }
        }

        SectionHeader("3 - TEST COVER")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showCover = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Mo CFlip ngay tren man hinh chinh (test khong can gap)") }
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
                }, modifier = Modifier.fillMaxWidth()) { Text("Show Presentation on secondary display") }
                Text("Widget duong chinh thong: Good Lock > MultiStar > I Love Galaxy Foldable > Launcher Widget > them widget CFlip Launcher. Sau do vuot cover sang trang widget de dung.",
                    fontSize = 12.sp, color = CFlipColors.TextSecondary)
            }
        }
    }
}
