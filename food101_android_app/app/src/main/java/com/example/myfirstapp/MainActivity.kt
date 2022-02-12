package com.example.myfirstapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var image: PreviewView
    private lateinit var predictBtn: Button
    private lateinit var fromGalleryBtn: ImageButton
    private lateinit var title: TextView
    private lateinit var hint: TextView

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDir: File
    private var lan = "english"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        image = findViewById(R.id.image)
        predictBtn = findViewById(R.id.predictBtn)
        fromGalleryBtn = findViewById(R.id.fromGallery)
        title = findViewById(R.id.title)
        hint = findViewById(R.id.hint)

        predictBtn.text = if (lan == "chinese") "預測" else "PREDICT"
        title.text = if (lan == "chinese") "食物101" else "Food101"
        hint.text = if (lan == "chinese") "預測你的食物是什麼" else "Make a prediction on your food!"

        outputDir = getOutputDir()

        // check camera permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 111)
        } else {
            startCamera()
        }
    }

    // request camera
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            predictBtn.isEnabled = true
        }
    }

    // open the camera
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                mPreview -> mPreview.setSurfaceProvider(image.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.d("CameraX", "starting camera fail:", e)
            }

            // take a photo and predict
            predictBtn.setOnClickListener {
                takePhoto()
            }

            // choose a picture from gallery
            fromGalleryBtn.setOnClickListener {
                chooseImageFromGallery()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // choose image from gallery
    private val startGalleryForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val intent = Intent(this, Prediction::class.java)
            intent.putExtra("image", it.data?.data.toString())
            intent.putExtra("lan", lan)
            startActivity(intent)
        }
    }
    private fun chooseImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startGalleryForResult.launch(intent)
    }

    // taking a photo
    private fun getOutputDir(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let { mFile ->
            File(mFile, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }

        return if(mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }
    private fun takePhoto() {

        val imageCapture = imageCapture?: return
        val photoFile = File(outputDir,
                             SimpleDateFormat("yy-mm-dd-hh-MM-ss",
                                                    Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg")
        val outputOption = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOption, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val imageUri = Uri.fromFile(photoFile)

                    Toast.makeText(this@MainActivity, "Image saved $imageUri", Toast.LENGTH_LONG).show()

                    val intent = Intent(this@MainActivity, Prediction::class.java)
                    intent.putExtra("image", imageUri.toString())
                    intent.putExtra("lan", lan)
                    startActivity(intent)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("Error", "${exception.message}", exception)
                }
            }
        )
    }
}
