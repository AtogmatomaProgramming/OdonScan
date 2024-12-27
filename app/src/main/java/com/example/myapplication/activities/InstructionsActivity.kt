package com.example.myapplication.activities


import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.example.myapplication.R
import com.example.myapplication.processing.ImageProcessor
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil



class InstructionsActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private lateinit var photoUri: Uri
    private val CAMERA_PERMISSION_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions_camera)

        // Botón para acceder a la cámara
        val accessCameraButton: Button = findViewById(R.id.access_camera)
        accessCameraButton.setOnClickListener {
            showImageSourceDialog()  // Muestra el diálogo para elegir entre cámara o galería
        }

        // Botón de retroceso
        val backButton: Button = findViewById(R.id.back_camera)
        backButton.setOnClickListener {
            finish()
        }
        // Cargar Modelo
        loadModel()
    }

    private fun loadModel() {
        // Cargar el modelo desde la carpeta "assets"
        val modelFile = FileUtil.loadMappedFile(this, "modelo_iota.tflite")
        interpreter = Interpreter(modelFile)
    }

    // Mostrar el diálogo para elegir entre la cámara o la galería
    private fun showImageSourceDialog() {
        val options = arrayOf("Tomar foto", "Seleccionar de la galería")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccionar origen de la imagen")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> openCamera()  // Opción para tomar una foto con la cámara
                1 -> openGallery() // Opción para seleccionar una imagen de la galería
            }
        }
        builder.show()
    }

    // Verificar si el permiso de cámara está concedido
    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Solicitar el permiso de la cámara si no está concedido
    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)) {
            AlertDialog.Builder(this)
                .setMessage("Para usar la cámara y tomar fotos, la aplicación necesita acceso. ¿Deseas permitirlo?")
                .setPositiveButton("Aceptar") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_REQUEST_CODE
                    )
                }
                .setNegativeButton("Cancelar", null)
                .create()
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de cámara concedido.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun openCamera() {
        if (isCameraPermissionGranted()) {
            val photoFile = File(externalCacheDir, "photo_${System.currentTimeMillis()}.jpg")
            photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            requestCameraPermission()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    // Manejar el resultado de la cámara
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("Debug", "onActivityResult() -> requestCode: $requestCode, resultCode: $resultCode")

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == RESULT_OK && ::photoUri.isInitialized) {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
                    saveImageAndProcess(bitmap)
                }
            }
            GALLERY_REQUEST_CODE -> {
                if (resultCode == RESULT_OK && data != null) {
                    val selectedImageUri = data.data
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                    saveImageAndProcess(bitmap)
                }
            }
        }
    }

    private fun saveImageAndProcess(bitmap: Bitmap) {
        try {
            // Guardar la imagen como archivo temporal
            Log.d("Debug", "Entrando en saveImageAndProcess()")
            val file = File(externalCacheDir, "photo_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()

            Log.d("Debug", "Imagen guardada correctamente en: ${file.absolutePath}")
            // Aquí delegamos el procesamiento de la imagen a la clase ImageProcessor
            val imageProcessor = ImageProcessor(interpreter, this)
            imageProcessor.processImage(Uri.fromFile(file))
            Log.d("Debug", "Imagen enviada a processImage()")

        } catch (e: IOException) {
            Log.e("Debug", "Error al guardar la imagen: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val GALLERY_REQUEST_CODE = 102
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Aquí también podrías añadir lógica adicional si es necesario
        finish()  // Esto finalizará la actividad al presionar el botón "Atrás"
    }
}


