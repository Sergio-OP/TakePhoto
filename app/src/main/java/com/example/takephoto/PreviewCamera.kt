package com.example.takephoto

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PreviewCamera : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var textToSpeech: TextToSpeech

    private lateinit var capReq: CaptureRequest.Builder
    private lateinit var cameraManager: CameraManager
    private lateinit var textureView: TextureView
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var cameraDevice: CameraDevice
    private lateinit var handler: Handler
    private lateinit var handlerThread: HandlerThread
    private lateinit var imageReader: ImageReader

    lateinit var storage: FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_camera)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        val buttonToTakeSelfie = findViewById<Button>(R.id.btn_capture)
        buttonToTakeSelfie.setOnClickListener {
            capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            capReq.addTarget(imageReader.surface)
            cameraCaptureSession.capture(capReq.build(), null, null)
        }

        textToSpeech = TextToSpeech(this, this)
        textToSpeech.setOnUtteranceProgressListener(object: UtteranceProgressListener(){
            override fun onStart(p0: String?) {
                Log.i("TAG", "start speaking")
            }

            override fun onDone(p0: String?) {
                Log.i("TAG", "finished speaking")
                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                capReq.addTarget(imageReader.surface)
                cameraCaptureSession.capture(capReq.build(), null, null)
            }

            override fun onError(p0: String?) {
                Log.i("TAG", "error speaking")
            }
        })

        val handler2 = Handler(Looper.getMainLooper())
        handler2.postDelayed({
            textToSpeech.speak("Cheers!", TextToSpeech.QUEUE_FLUSH, null, "1")
        }, 4000)


        storage = Firebase.storage


        textureView = findViewById<TextureView>(R.id.texture_view)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("CameraThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

            }

        }

        imageReader = ImageReader.newInstance(1920,1080, ImageFormat.JPEG, 1)
        imageReader.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {
            override fun onImageAvailable(p0: ImageReader?) {

                var image = p0?.acquireLatestImage()
                var buffer = image!!.planes[0].buffer
                var bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                val file: File = createImageFile()
                var opStream = FileOutputStream(file)
                opStream.write(bytes)
                opStream.close()

                val rotatedBitmap = rotateBitmap(BitmapFactory.decodeFile(file.path), 180f)

                val rotatedFile: File = createImageFile()

                val rotatedOutputStream = FileOutputStream(rotatedFile)
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, rotatedOutputStream)
                rotatedOutputStream.close()

                image.close()

                val frameBitmapNoSized = BitmapFactory.decodeResource(resources, R.drawable.photo_frame_1920_1200)
                val frameBitmap = Bitmap.createScaledBitmap(frameBitmapNoSized, 1600, 1200, true)
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                val combinedBitmap = combineImages(rotatedBitmap, frameBitmap)
                val combinedFile: File = createImageFile()
                val combinedOutputStream = FileOutputStream(combinedFile)
                combinedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, combinedOutputStream)
                combinedOutputStream.close()
                MediaScannerConnection.scanFile(
                    this@PreviewCamera, arrayOf(combinedFile.toString()), null, null
                )
                Log.i("CAMERA_SIZE", "Width: ${rotatedBitmap.width} Height: ${rotatedBitmap.height}")
                Log.i("CAMERA_SIZE_FRAME", "Width: ${frameBitmap.width} Height: ${frameBitmap.height}")

                Toast.makeText(this@PreviewCamera, "Image Captured", Toast.LENGTH_SHORT).show()

                val url = uploadImage(combinedFile)
            }
        }, handler)



    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        cameraManager.openCamera(cameraManager.cameraIdList[0], @SuppressLint("MissingPermission")
        object : CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0
                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                var surface = Surface(textureView.surfaceTexture)
                capReq.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface, imageReader.surface), object : CameraCaptureSession.StateCallback(){
                    override fun onConfigured(p0: CameraCaptureSession) {
                        cameraCaptureSession = p0
                        cameraCaptureSession.setRepeatingRequest(capReq.build(), null, null)
                        Log.i("MiApp", "OnConfigured cameraCapture session")
                    }

                    override fun onConfigureFailed(p0: CameraCaptureSession) {
                        Log.i("MiApp", "OnConfigureFailed cameraCapture session")
                    }

                }, handler)
            }

            override fun onDisconnected(p0: CameraDevice) {
                Log.i("MiApp", "OnDisconnected")
            }

            override fun onError(p0: CameraDevice, p1: Int) {
                Log.i("MiApp", "OnError")
            }

        }, handler)
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
                val intent = Intent(this@PreviewCamera, AfterSelfie::class.java)
                intent.putExtra("url", downloadUri)
                startActivity(intent)
            } else {
                // Handle Failures
            }

        }
        return downloadUri
    }

    override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS){
            val result = textToSpeech.setLanguage(Locale.ENGLISH)

            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED ){
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun combineImages(capturedBitmap: Bitmap, frameBitmap: Bitmap): Bitmap {
        val combinedBitmap = Bitmap.createBitmap(
            capturedBitmap.width, capturedBitmap.height, capturedBitmap.config
        )
        val canvas = Canvas(combinedBitmap)
        canvas.drawBitmap(capturedBitmap, 0f, 0f, null)
        canvas.drawBitmap(frameBitmap, 0f, 0f, null)
        return combinedBitmap
    }

}