package com.example.quizapp.music

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.example.quizapp.R

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false  // Track the music state

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.quizmusic)
        mediaPlayer?.isLooping = true // Loop music
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra("ACTION")) {
            "PLAY" -> if (!isPlaying) {
                mediaPlayer?.start()
                isPlaying = true
            }
            "PAUSE" -> if (isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
            }
        }
        return START_STICKY
    }



    override fun onDestroy() {
        mediaPlayer?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

