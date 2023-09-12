package com.example.takephoto

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Question : AppCompatActivity() {
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        val buttonNotTakeSelfie = findViewById<Button>(R.id.btn_dont_take_selfie)
        buttonNotTakeSelfie.setOnClickListener { notTakeSelfie() }
    }

    private fun notTakeSelfie() {
        val intent = Intent(this, NoSelfie::class.java)
        startActivity(intent)
    }
}