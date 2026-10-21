package com.cuong.cflip.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuong.cflip.media.MediaControllerRepository
import com.cuong.cflip.media.MediaState
import kotlinx.coroutines.delay

private fun fmt(ms: Long): String {
    val s = (ms / 1000).toInt()
    return "%d:%02d".format(s / 60, s % 60)
}

/** Full media card: metadata, play/pause, skip, seek. Polls the session every second. */
@Composable
fun MediaScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repo = remember { MediaControllerRepository(context) }
    val media by MediaState.current.collectAsState()

    LaunchedEffect(Unit) {
        while (true) { repo.refresh(); delay(1000) }
    }

    Column(modifier = modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Media", fontSize = 18.sp)
        val m = media
        if (m == null) {
            Text("Chua co phien phat nhac nao. Mo YouTube Music / Spotify roi quay lai.", fontSize = 13.sp)
        } else {
            Text(m.title, fontSize = 16.sp, maxLines = 2)
            if (m.artist.isNotEmpty()) Text(m.artist, fontSize = 13.sp, maxLines = 1)
            if (m.durationMs > 0) {
                Slider(
                    value = m.positionMs.toFloat(),
                    onValueChange = { repo.seekTo(it.toLong()) },
                    valueRange = 0f..m.durationMs.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("${fmt(m.positionMs)} / ${fmt(m.durationMs)}", fontSize = 11.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { repo.previous() }) { Text("Prev") }
                Button(onClick = { repo.playPause() }) { Text(if (m.isPlaying) "Pause" else "Play") }
                Button(onClick = { repo.next() }) { Text("Next") }
            }
        }
    }
}
