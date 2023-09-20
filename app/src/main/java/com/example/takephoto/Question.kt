package com.example.takephoto

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Question : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        val buttonNotTakeSelfie = findViewById<Button>(R.id.btn_dont_take_selfie)
        buttonNotTakeSelfie.setOnClickListener { notTakeSelfie() }

        val buttonTakeSelfie = findViewById<Button>(R.id.btn_take_selfie)
        buttonTakeSelfie.setOnClickListener { takeSelfie() }
    }

    private fun notTakeSelfie() {
        val intent = Intent(this, NoSelfie::class.java)
        startActivity(intent)
    }

    private fun takeSelfie() {
        val intent = Intent(this, Selfie::class.java)
        startActivity(intent)
    }
}