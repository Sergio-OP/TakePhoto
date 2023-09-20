package com.example.takephoto

import android.content.pm.ActivityInfo
import android.graphics.Picture
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.caverock.androidsvg.SVG
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.Locale

class QrCode : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var textToSpeech: TextToSpeech
    lateinit var client : OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        textToSpeech = TextToSpeech(this, this)
        textToSpeech.setOnUtteranceProgressListener(object: UtteranceProgressListener(){
            override fun onStart(p0: String?) {
                Log.i("TAG", "start speaking")
            }

            override fun onDone(p0: String?) {
                Log.i("TAG", "finished speaking")
            }

            override fun onError(p0: String?) {
                Log.i("TAG", "error speaking")
            }
        })

        val videoView =  findViewById<VideoView>(R.id.temi_face_qr)
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.temi_face)
        videoView.setVideoURI(uri)
        videoView.start()
        videoView.requestFocus()
        videoView.setOnPreparedListener { it.setLooping(true) }

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            textToSpeech.speak("You look amazing! Don't forget to scan this QR code and download your selfie.", TextToSpeech.QUEUE_FLUSH, null, "1")
        }, 3000)

        val url = intent.getStringExtra("url")
        if (url != null) {
            postHttpPetition(url)
        }

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
        super.onDestroy()
        if(::textToSpeech.isInitialized){
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    private fun postHttpPetition(url: String) {
        client = OkHttpClient()

        val endpointUrl = "https://api.qr-code-generator.com/v1/create?access-token=KA5clFZchvejQnpvvg-H0U68CJFr3EZd6XF2HBvVsW37ea67jx_1gbmKAGH8VEd1"

        val requestBody = FormBody.Builder()
            .add("frame_name", "no-frame")
            .add("qr_code_text", url)
            .build()

        val request = Request.Builder()
            .url(endpointUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
                Log.d("HTTP Client", "Error: $e")
            }

            override fun onResponse(call: Call, response: Response) {

                Log.d("HTTP Client", "Respuesta recibida")


                if(response.isSuccessful){
                    Log.d("HTTP Client", "Respuesta exitosa")
                    val bytes = response.body?.bytes()

                    val inputStream = ByteArrayInputStream(bytes)
                    val picture = Picture()
                    val svg = SVG.getFromInputStream(inputStream)
                    svg.renderToPicture().draw(picture.beginRecording(1000,1000))
                    val pictureDrawable = PictureDrawable(picture)

                    val imageQr = findViewById<ImageView>(R.id.img_qr)

                    if(pictureDrawable != null) {
                        runOnUiThread {
                            imageQr.setImageDrawable(pictureDrawable)
                        }
                    } else {
                        // Si no es un formato compatible, puedes manejarlo de otra manera (por ejemplo, mostrar un mensaje de error)
                    }



                } else {
                    // Manejar una respuesta no exitosa aquí
                }
            }
        })
    }
}