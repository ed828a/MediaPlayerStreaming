package com.dew.edward.mediaplayerstreaming

import android.graphics.Point
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        val surface = holder!!.surface
        setupMediaPlayer(surface)
        prepareMediaPlayer()
    }

    private lateinit var mediaPlayer: MediaPlayer
    private var playbackPosition = 0
    private val rtspUrl = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val holder = surfaceView.holder
        holder.addCallback(this)
    }

    override fun onPause() {
        super.onPause()

        playbackPosition = mediaPlayer.currentPosition
    }

    override fun onStop() {
        mediaPlayer.stop()
        mediaPlayer.release()

        super.onStop()
    }

    private fun createAudioAttributes(): AudioAttributes {
        val builder = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)

        return builder.build()
    }

    private fun setupMediaPlayer(surface: Surface) {
        progressBar.visibility = View.VISIBLE
        mediaPlayer = MediaPlayer()
        mediaPlayer.setSurface(surface)
        val audioAttributes = createAudioAttributes()
        mediaPlayer.setAudioAttributes(audioAttributes)
        val uri = Uri.parse(rtspUrl)
        try {
            mediaPlayer.setDataSource(this, uri)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun prepareMediaPlayer(){
        try {
            mediaPlayer.prepareAsync() // prepare in background
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        mediaPlayer.setOnPreparedListener {
            progressBar.visibility = View.INVISIBLE
            mediaPlayer.seekTo(playbackPosition)
            mediaPlayer.start()
        }

        mediaPlayer.setOnVideoSizeChangedListener { player, width, height ->
            setSurfaceDimensions(player, width, height)
        }
    }

    private fun setSurfaceDimensions(player: MediaPlayer, width: Int, height: Int){
        if (width > 0 && height > 0){
            val aspectRatio = height.toFloat() / width.toFloat()
            val screenDimensions = Point()
            windowManager.defaultDisplay.getSize(screenDimensions)
            val surfaceWidth = screenDimensions.x
            val surfaceHeight = (surfaceWidth * aspectRatio).toInt()
            val params = FrameLayout.LayoutParams(surfaceWidth, surfaceHeight)
            surfaceView.layoutParams = params
            player.setDisplay(surfaceView.holder)
        }
    }
}
