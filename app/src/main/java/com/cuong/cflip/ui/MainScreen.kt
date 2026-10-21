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
            TextButton(onClick = { showCover = false }) { Text("← Quay lại", color = CFlipColors.Accent) }
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
                .background(CFlipColors.AccentBrush), contentAlignment = Alignment.Center) {
                Text("C", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = CFlipColors.AccentDark)
            }
            Column {
                Text("CFlip Launcher", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = CFlipColors.TextPrimary)
                Text("Cover launcher của Cường · Galaxy Z Flip5", fontSize = 13.sp,
                    color = CFlipColors.TextSecondary)
            }
        }
        Text("Mọi tính năng đều miễn phí, không paywall. Bản 2.1 — giao diện cao cấp, tiếng Việt có dấu.",
            fontSize = 13.sp, color = CFlipColors.TextSecondary)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Màn hình phát hiện: " +
                displayRepo.listDisplays().joinToString { "#${it.id} ${it.name} ${it.widthPx}x${it.heightPx}" },
                fontSize = 12.sp, color = CFlipColors.TextSecondary)
        }

        SectionHeader("1 · CẤP QUYỀN")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Truy cập thông báo (thông báo + nhạc)") }
                Button(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Accessibility (khoá máy, Gần đây, Cài đặt nhanh)") }
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.packageName)))
                }, modifier = Modifier.fillMaxWidth()) { Text("Hiển thị trên ứng dụng khác (Overlay)") }
                OutlinedButton(onClick = { QuickControls.openUsageAccessSettings(context) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Usage Access (danh sách ứng dụng gần đây thật)") }
                OutlinedButton(onClick = { QuickControls.openWriteSettingsPermission(context) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Sửa cài đặt hệ thống (chỉnh độ sáng)") }
            }
        }

        SectionHeader("2 · DỊCH VỤ COVER")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    ContextCompat.startForegroundService(context, Intent(context, CoverForegroundService::class.java))
                    Toast.makeText(context, "Đã chạy dịch vụ CFlip — gập máy lại để thử", Toast.LENGTH_LONG).show()
                }, modifier = Modifier.fillMaxWidth()) { Text("Bắt đầu dịch vụ chạy nền") }
                OutlinedButton(onClick = { context.stopService(Intent(context, CoverForegroundService::class.java)) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Dừng dịch vụ") }
            }
        }

        SectionHeader("3 · THỬ MÀN HÌNH PHỤ")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showCover = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Mở CFlip ngay trên màn hình chính (thử không cần gập)") }
                Button(onClick = {
                    val secondary = displayRepo.secondaryDisplay()
                    if (secondary == null) {
                        Toast.makeText(context,
                            "Chưa thấy màn hình phụ. Trên Flip5 thật: gập máy lại. Trên emulator: thêm màn hình phụ.",
                            Toast.LENGTH_LONG).show()
                    } else {
                        presentation?.dismiss()
                        presentation = CoverPresentation(context, secondary).also {
                            runCatching { it.show() }.onFailure { e ->
                                Toast.makeText(context, "Presentation bị chặn: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Hiện Presentation lên màn hình phụ") }
                Text("Đường chính thống bằng widget: Good Lock › MultiStar › I ♥ Galaxy Foldable › Launcher Widget › thêm widget CFlip Launcher. Sau đó vuốt cover sang trang widget để dùng.",
                    fontSize = 12.sp, color = CFlipColors.TextSecondary)
            }
        }
    }
}
