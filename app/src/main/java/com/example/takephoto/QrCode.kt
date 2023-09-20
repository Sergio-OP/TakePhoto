package com.example.takephoto

import android.content.pm.ActivityInfo
import android.graphics.Picture
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
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

class QrCode : AppCompatActivity() {

    lateinit var client : OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        val url = intent.getStringExtra("url")
        if (url != null) {
            postHttpPetition(url)
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
                    svg.renderToPicture().draw(picture.beginRecording(500,500))
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