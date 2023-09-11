package com.example.takephoto

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        val videoView =  findViewById<VideoView>(R.id.video_face)
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.temi_face)
        videoView.setVideoURI(uri)
        videoView.start()
        videoView.requestFocus()
        videoView.setOnPreparedListener { it.setLooping(true) }

    }


}