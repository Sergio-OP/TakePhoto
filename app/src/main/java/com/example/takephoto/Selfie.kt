package com.example.takephoto

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class Selfie : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var textToSpeech: TextToSpeech

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selfie)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        val videoView =  findViewById<VideoView>(R.id.video_selfie)
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.temi_face)
        videoView.setVideoURI(uri)
        videoView.start()
        videoView.requestFocus()
        videoView.setOnPreparedListener { it.setLooping(true) }

        textToSpeech = TextToSpeech(this, this)
        textToSpeech.setOnUtteranceProgressListener(object: UtteranceProgressListener(){
            override fun onStart(p0: String?) {
                Toast.makeText(this@Selfie, "Start Speaking", Toast.LENGTH_SHORT).show()
            }

            override fun onDone(p0: String?) {
                Toast.makeText(this@Selfie, "Finished Speaking", Toast.LENGTH_SHORT).show()
                goToPreviewCamera()
            }

            override fun onError(p0: String?) {
                Toast.makeText(this@Selfie, "Error Speaking", Toast.LENGTH_SHORT).show()
            }

        })

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            textToSpeech.speak("Great! Let's take a selfie", TextToSpeech.QUEUE_FLUSH, null, "MyUniqueId")
        }, 1000)

        val intentHandler = Handler(Looper.getMainLooper())
        intentHandler.postDelayed({
            goToPreviewCamera()
        }, 4000)


    }

    private fun goToPreviewCamera(){
        val intent = Intent(this, PreviewCamera::class.java)
        startActivity(intent)
    }

    override fun onInit(p0: Int) {
        if(p0 == TextToSpeech.SUCCESS){
            val result = textToSpeech.setLanguage(Locale.ENGLISH)

            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED ){
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        if(::textToSpeech.isInitialized){
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}