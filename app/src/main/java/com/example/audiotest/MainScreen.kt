package com.example.audiotest

import android.content.Context
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.toPx
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.media3.common.MediaItem


@Composable
fun VideoPlayerScreen(
    context: Context,
    modifier: Modifier = Modifier,
    onExoPlayerReady: (ExoPlayer) -> Unit
) {
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = false
        }
    }

    DisposableEffect(Unit) {
        onExoPlayerReady(exoPlayer)

        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .padding(16.dp)
            .background(Color.Black)
            .graphicsLayer {
                clip = true
                shape = RoundedCornerShape(16.dp)
                shadowElevation = 8.dp.toPx()
            }
    ) {
        AndroidView(
            modifier = Modifier
                .background(Color.Black)
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(16.dp)
                },
            factory = {
                PlayerView(context).apply {
                    useController = true
                    player = exoPlayer
                    val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/raw/samplevideo")
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                }
            },
            update = {
                it.player = exoPlayer
            }
        )
    }
}

@Composable
fun MainScreen(
    context: Context,
    onVideoPlayerReady: (ExoPlayer) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VideoPlayerScreen(
            context = context,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            onExoPlayerReady = onVideoPlayerReady
        )
    }
}
