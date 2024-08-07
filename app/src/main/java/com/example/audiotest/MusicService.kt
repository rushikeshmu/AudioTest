package com.example.audiotest

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MusicService : Service() {
    private lateinit var mediaSession: MediaSession
    private lateinit var exoPlayer: ExoPlayer
    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        val intent = Intent("ACTION_PAUSE_VIDEO")
                        sendBroadcast(intent)
                        Log.d("MusicService", "Broadcast sent: ACTION_PAUSE_VIDEO")
                    }
                }
            })
        }

        mediaSession = MediaSession.Builder(this, exoPlayer).build()

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        mediaSession.release()
        exoPlayer.release()
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "MusicServiceChannel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notificationChannelId,
                "Music Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Music Player")
            .setContentText("Playing music")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    fun getMediaSession(): MediaSession {
        return mediaSession
    }

    companion object {
        const val NOTIFICATION_ID = 1
    }
}