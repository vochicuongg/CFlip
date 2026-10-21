package com.cuong.cflip.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuong.cflip.service.NotificationController
import com.cuong.cflip.service.NotificationState
import java.text.DateFormat
import java.util.Date

/** Cover notification hub: open, dismiss, action buttons, inline reply. */
@Composable
fun NotificationsScreen(modifier: Modifier = Modifier) {
    val notifications by NotificationState.active.collectAsState()
    var replyTarget by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(10.dp)) {
        Text("Notifications (${notifications.size})", fontSize = 18.sp)
        if (!NotificationController.isConnected()) {
            Text("Notification Access chua duoc cap - vao trang Chuong trinh chinh de cap quyen.", fontSize = 12.sp)
        }
        if (replyTarget != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = replyText, onValueChange = { replyText = it },
                    label = { Text("Reply") }, modifier = Modifier.weight(1f))
                Button(onClick = {
                    NotificationController.reply(replyTarget!!, replyText)
                    replyText = ""; replyTarget = null
                }) { Text("Send") }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(notifications, key = { it.key }) { n ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(n.title, fontSize = 14.sp, maxLines = 1)
                    if (n.text.isNotEmpty()) Text(n.text, fontSize = 12.sp, maxLines = 3)
                    Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(n.timeMs)), fontSize = 10.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { NotificationController.open(n.key) }) { Text("Open") }
                        OutlinedButton(onClick = { NotificationController.dismiss(n.key) }) { Text("Dismiss") }
                        if (n.canReply) OutlinedButton(onClick = { replyTarget = n.key }) { Text("Reply") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        n.actionTitles.forEachIndexed { index, title ->
                            if (title.isNotEmpty()) {
                                OutlinedButton(onClick = { NotificationController.clickAction(n.key, index) }) {
                                    Text(title, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
