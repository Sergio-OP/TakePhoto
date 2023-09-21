package com.example.takephoto

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PreviewCamera : AppCompatActivity() {


    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    lateinit var storage: FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_camera)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

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

                    // TODO: implementar intent cuando uploadImage sea éxitoso
                    val intent = Intent(this@PreviewCamera, AfterSelfie::class.java)
                    intent.putExtra("url", url)
                    startActivity(intent)
                }
            }
        )


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

    }
}