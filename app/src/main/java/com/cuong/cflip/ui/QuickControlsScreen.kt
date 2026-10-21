package com.cuong.cflip.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
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
import com.cuong.cflip.control.QuickControls
import com.cuong.cflip.service.MyAccessibilityService

/** Quick controls tab: torch, volume, brightness, panels, accessibility system actions. */
@Composable
fun QuickControlsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var torch by remember { mutableStateOf(QuickControls.torchOn) }
    val (volInit, volMax) = remember { QuickControls.volume(context) }
    var volume by remember { mutableStateOf(volInit.toFloat()) }
    var brightness by remember { mutableStateOf(QuickControls.brightness(context).toFloat()) }
    val a11y = MyAccessibilityService.instance

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    Column(modifier = modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Quick Controls", fontSize = 18.sp)

        Button(onClick = {
            val next = !torch
            if (QuickControls.setTorch(context, next)) torch = next else toast("Khong tim thay den flash")
        }, modifier = Modifier.fillMaxWidth()) { Text(if (torch) "Flashlight: ON" else "Flashlight: OFF") }

        Text("Volume ${volume.toInt()}/$volMax", fontSize = 13.sp)
        Slider(value = volume, onValueChange = { volume = it; QuickControls.setVolume(context, it.toInt()) },
            valueRange = 0f..volMax.toFloat(), modifier = Modifier.fillMaxWidth())

        Text("Brightness ${brightness.toInt()}/255", fontSize = 13.sp)
        Slider(value = brightness, onValueChange = { brightness = it
            if (!QuickControls.setBrightness(context, it.toInt())) toast("Can cap quyen Modify system settings")
        }, valueRange = 0f..255f, modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = { QuickControls.openWriteSettingsPermission(context) }) {
            Text("Cap quyen chinh do sang he thong")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = { QuickControls.openWifiPanel(context) }) { Text("Wi-Fi") }
            OutlinedButton(onClick = { QuickControls.openInternetPanel(context) }) { Text("Internet") }
            OutlinedButton(onClick = { QuickControls.openBluetoothSettings(context) }) { Text("Bluetooth") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = { QuickControls.openNfcPanel(context) }) { Text("NFC") }
            OutlinedButton(onClick = { QuickControls.openDeviceSettings(context) }) { Text("Settings") }
        }

        Text("System actions (can Accessibility)", fontSize = 14.sp)
        if (a11y == null) Text("Accessibility chua bat - cac nut duoi se khong hoat dong.", fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = { if (a11y?.lockScreen() != true) toast("Can Accessibility") }) { Text("Lock") }
            Button(onClick = { if (a11y?.openRecents() != true) toast("Can Accessibility") }) { Text("Recents") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = { if (a11y?.openNotifications() != true) toast("Can Accessibility") }) { Text("Notifications") }
            Button(onClick = { if (a11y?.openQuickSettings() != true) toast("Can Accessibility") }) { Text("Quick Settings") }
        }

        MirrorControls()

        BubbleControls()
    }
}
