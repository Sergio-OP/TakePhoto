package com.example.takephoto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Question : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

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