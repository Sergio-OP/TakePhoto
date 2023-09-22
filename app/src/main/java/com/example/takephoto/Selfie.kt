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
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.robotemi.sdk.Robot
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import java.util.Locale

class Selfie : AppCompatActivity(), TextToSpeech.OnInitListener, OnGoToLocationStatusChangedListener {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var robot: Robot

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
                //Toast.makeText(this@Selfie, "Start Speaking", Toast.LENGTH_SHORT).show()
                Log.i("TemiPhoto", "start speaking")
            }

            override fun onDone(p0: String?) {
//                Toast.makeText(this@Selfie, "Finished Speaking", Toast.LENGTH_SHORT).show()
                Log.i("TemiPhoto", "finished speaking")
                goToSelfie()
            }

            override fun onError(p0: String?) {
//                Toast.makeText(this@Selfie, "Error Speaking", Toast.LENGTH_SHORT).show()
            }

        })


        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            textToSpeech.speak("Great! Let's take a selfie.", TextToSpeech.QUEUE_FLUSH, null, "7")
        }, 1000)

        Robot.getInstance().addOnGoToLocationStatusChangedListener(this)
        // robot = Robot.getInstance()

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

    override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String
    ) {
        when(status) {
            OnGoToLocationStatusChangedListener.START -> { Log.i("Temi", "Start Walking") }
            OnGoToLocationStatusChangedListener.CALCULATING -> { Log.i("Temi", "Calculating Walking") }
            OnGoToLocationStatusChangedListener.GOING -> { Log.i("Temi", "Going Walking") }
            OnGoToLocationStatusChangedListener.COMPLETE -> {
                Log.i("Temi", "Complete Walking")
                goToPreviewCamera()
            }
            OnGoToLocationStatusChangedListener.ABORT -> { Log.i("Temi", "Aborting Walking") }
            OnGoToLocationStatusChangedListener.REPOSING -> { Log.i("Temi", "Reposing Walking") }
        }
    }

    private fun goToSelfie() {
        Robot.getInstance().goTo("selfie")

    }
}