package com.shitflix.app.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.shitflix.app.data.model.StreamLink
import com.shitflix.app.data.repo.ProviderRegistry

@androidx.media3.common.util.UnstableApi
@Composable
fun PlayerScreen(nav: NavController, providerId: String, movieId: String) {
    val context = LocalContext.current
    var streams by remember { mutableStateOf<List<StreamLink>>(emptyList()) }
    var selectedIdx by remember { mutableStateOf(0) }
    var showQuality by remember { mutableStateOf(false) }
    var showSubs by remember { mutableStateOf(false) }

    LaunchedEffect(providerId, movieId) {
        streams = ProviderRegistry.get(providerId)?.load(movieId).orEmpty()
    }

    val player = remember {
        ExoPlayer.Builder(context).build().apply { playWhenReady = true }
    }

    LaunchedEffect(streams, selectedIdx) {
        val s = streams.getOrNull(selectedIdx) ?: return@LaunchedEffect
        val subItems = s.subtitles.map { sub ->
            MediaItem.SubtitleConfiguration.Builder(Uri.parse(sub.url))
                .setMimeType(if (sub.mime == "application/x-subrip") MimeTypes.APPLICATION_SUBRIP else MimeTypes.TEXT_VTT)
                .setLanguage(sub.lang)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
        }
        val item = MediaItem.Builder()
            .setUri(s.url)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(movieId).build())
            .setSubtitleConfigurations(subItems)
            .apply {
                when {
                    s.isHls -> setMimeType(MimeTypes.APPLICATION_M3U8)
                    s.isDash -> setMimeType(MimeTypes.APPLICATION_MPD)
                }
            }
            .build()
        player.setMediaItem(item)
        player.prepare()
    }

    DisposableEffect(Unit) { onDispose { player.release() } }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                    setShowSubtitleButton(true)
                }
            },
        )

        // Top overlay: back + quality + subs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { showSubs = true }) {
                Icon(Icons.Filled.ClosedCaption, "Subtitles", tint = Color.White)
            }
            IconButton(onClick = { showQuality = true }) {
                Icon(Icons.Filled.HighQuality, "Quality", tint = Color.White)
            }
        }

        if (showQuality) {
            QualityDialog(streams, selectedIdx, onSelect = {
                selectedIdx = it
                showQuality = false
            }, onDismiss = { showQuality = false })
        }
        if (showSubs) {
            SubtitleDialog(player, streams.getOrNull(selectedIdx)?.subtitles?.map { it.lang }.orEmpty(),
                onDismiss = { showSubs = false })
        }
    }
}

@Composable
private fun QualityDialog(streams: List<StreamLink>, selected: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Quality") },
        text = {
            Column {
                streams.forEachIndexed { i, s ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = i == selected, onClick = { onSelect(i) })
                        Spacer(Modifier.width(8.dp))
                        Text(s.qualityLabel ?: s.name)
                    }
                }
                if (streams.isEmpty()) Text("No streams available.")
            }
        },
    )
}

@androidx.media3.common.util.UnstableApi
@Composable
private fun SubtitleDialog(player: ExoPlayer, langs: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Subtitles") },
        text = {
            Column {
                TextButton(onClick = {
                    val params = player.trackSelectionParameters.buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                        .build()
                    player.trackSelectionParameters = params
                    onDismiss()
                }) { Text("Off") }
                langs.forEach { lang ->
                    TextButton(onClick = {
                        val params = player.trackSelectionParameters.buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                            .setPreferredTextLanguage(lang)
                            .build()
                        player.trackSelectionParameters = params
                        onDismiss()
                    }) { Text(lang) }
                }
                if (langs.isEmpty()) Text("No subtitles available.")
            }
        },
    )
}
