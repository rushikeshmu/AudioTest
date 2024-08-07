package com.example.audiotest

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.audiotest.ui.theme.AudioTestTheme


class MainActivity : ComponentActivity() {
    private var videoPlayer: ExoPlayer? = null
    private var musicServiceBound = false
    private lateinit var musicService: MusicService

    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest
//
//    private val serviceConnection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            val binder = service as? MusicService.LocalBinder
//            musicService = binder?.getService() ?: return
//            musicServiceBound = true
//        }
//
//        override fun onServiceDisconnected(name: ComponentName?) {
//            musicServiceBound = false
//        }
//    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
            AudioManager.AUDIOFOCUS_LOSS -> {
                videoPlayer?.playWhenReady = false
                Log.d("MainActivity", "Audio focus lost, video paused")
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Optionally, resume playback if needed
                Log.d("MainActivity", "Audio focus gained")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build()
            )
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()

//        val intent = Intent(this, MusicService::class.java)
//        startForegroundService(intent)
//        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        setContent {
            AudioTestTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen(
                        context = this,
                        onVideoPlayerReady = { player ->
                            videoPlayer = player
                            requestAudioFocus()
                            videoPlayer?.addListener(object : Player.Listener {
                                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                                    if (playWhenReady) {
                                        requestAudioFocus()
                                    } else {
                                        abandonAudioFocus()
                                    }
                                }
                            })
                        }
                    )
                }
            }
        }
    }

    private fun requestAudioFocus() {
        val result = audioManager.requestAudioFocus(audioFocusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d("MainActivity", "Audio focus request granted")
        } else {
            Log.d("MainActivity", "Audio focus request denied")
        }
    }

    private fun abandonAudioFocus() {
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
        Log.d("MainActivity", "Audio focus abandoned")
    }

    override fun onDestroy() {
        super.onDestroy()
//        if (musicServiceBound) {
//            unbindService(serviceConnection)
//            musicServiceBound = false
//        }
        videoPlayer?.release()
        videoPlayer = null
        abandonAudioFocus()
    }
}

