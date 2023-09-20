package com.example.takephoto

import android.graphics.Picture
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.caverock.androidsvg.SVG
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PreviewCamera : AppCompatActivity() {


    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    lateinit var storage: FirebaseStorage

    lateinit var client : OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_camera)

        startCamera()

        val buttonToTakeSelfie = findViewById<Button>(R.id.btn_capture)
        buttonToTakeSelfie.setOnClickListener {takePhoto()}

        cameraExecutor = Executors.newSingleThreadExecutor()

        storage = Firebase.storage

    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun postHttpPetition(url: String) {
        client = OkHttpClient()

        //val url = "https://api.qr-code-generator.com/v1/create?access-token=KA5clFZchvejQnpvvg-H0U68CJFr3EZd6XF2HBvVsW37ea67jx_1gbmKAGH8VEd1"

        val requestBody = FormBody.Builder()
            .add("frame_name", "no-frame")
            .add("qr_code_text", "https://www.google.com/")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
                Log.d("HTTP Client", "Error: $e")
            }

            override fun onResponse(call: Call, response: Response) {

                Log.d("HTTP Client", "Respuesta recibida")

                val imageQr = findViewById<ImageView>(R.id.img_qr_code)
                if(response.isSuccessful){
                    Log.d("HTTP Client", "Respuesta exitosa")
                    val bytes = response.body?.bytes()


                    val pictureDrawable = try {
                        val inputStream = ByteArrayInputStream(bytes)
                        val picture = Picture()
                        val svg = SVG.getFromInputStream(inputStream)
                        svg.renderToPicture().draw(picture.beginRecording(100,100))
                        PictureDrawable(picture)
                    } catch (e: Exception) {
                        null
                    }

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


    private fun startCamera(){
        var previewView = this.findViewById<androidx.camera.view.PreviewView>(R.id.previewView).surfaceProvider
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
                .also {
                    it.setSurfaceProvider(previewView)
                }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("TAG", "Use case binding failed")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto(){
        val imageCapture = imageCapture ?: return

        val photoFile = createImageFile()

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

//                    val url = uploadImage(photoFile)
                    val url = "https://www.google.com/"
                    postHttpPetition(url)
                }
            }
        )

        var storageRef = storage.reference
        val selfieRef = storageRef.child("images/${photoFile.name}")
        var selfieFile = Uri.fromFile(photoFile)
        var uploadTask = selfieRef.putFile(selfieFile)

        uploadTask.addOnFailureListener{
            Log.i("CloudFirebase", "Update file failure")
        }.addOnSuccessListener{
            Log.i("CloudFirebase", "Update file success")
        }

        val urlTask = uploadTask.continueWithTask {task ->
            if(!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            selfieRef.downloadUrl
        }.addOnCompleteListener { task ->
            if( task.isSuccessful ) {
                val downloadUri = task.result
                Log.i("CloudFirebase", "Url Selfie: $downloadUri")
            } else {
                // Handle Failures
            }

        }

    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())

        // Define la carpeta de destino en la que deseas guardar las fotos.
        val folderName = "TemiPhotos"
        val storageDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), folderName)

        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val imageFileName = "JPEG_${timeStamp}.jpg"
        return File(storageDir, imageFileName)
    }

    private fun uploadImage(photoFile: File): String {
        var downloadUri = "Error"
        var storageRef = storage.reference
        /*var imagesRef: StorageReference? = storageRef.child("images")*/
        val selfieRef = storageRef.child("images/selfie${photoFile.name}.jpg")
        var selfieFile = Uri.fromFile(photoFile)
        var uploadTask = selfieRef.putFile(selfieFile)

        uploadTask.addOnFailureListener{
            Log.i("CloudFirebase", "Update file failure")
        }.addOnSuccessListener{
            Log.i("CloudFirebase", "Update file success")
        }

        val urlTask = uploadTask.continueWithTask {task ->
            if(!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            selfieRef.downloadUrl
        }.addOnCompleteListener { task ->
            if( task.isSuccessful ) {
                downloadUri = task.result.toString()
                Log.i("CloudFirebase", "Url Selfie: $downloadUri")
            } else {
                // Handle Failures
            }

        }
        return downloadUri
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    }
}