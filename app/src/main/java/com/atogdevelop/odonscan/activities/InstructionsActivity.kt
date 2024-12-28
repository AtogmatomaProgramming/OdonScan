package com.atogdevelop.odonscan.activities


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
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.atogdevelop.odonscan.R
import com.atogdevelop.odonscan.processing.ImageProcessor
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

        Log.d("InstructionsActivity", "onCreate() iniciado")

        // Botón para acceder a la cámara
        val accessCameraButton: Button = findViewById(R.id.access_camera)
        accessCameraButton.setOnClickListener {
            Log.d("InstructionsActivity", "Botón de cámara presionado")
            showImageSourceDialog()  // Muestra el diálogo para elegir entre cámara o galería
        }

        // Botón de retroceso
        val backButton: Button = findViewById(R.id.back_camera)
        backButton.setOnClickListener {
            Log.d("InstructionsActivity", "Botón de retroceso presionado")
            finish()
        }
        // Cargar Modelo
        try {
            loadModel()
            Log.d("InstructionsActivity", "Modelo cargado exitosamente")
        } catch (e: Exception) {
            Log.e("InstructionsActivity", "Error al cargar el modelo: ${e.message}")
        }
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
                0 -> {
                    Log.d("InstructionsActivity", "Seleccionada opción: Tomar foto")
                    openCamera()
                }
                1 -> {
                    Log.d("InstructionsActivity", "Seleccionada opción: Seleccionar de la galería")
                    openGallery()
                }
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
            try {
                val photoFile = File(externalCacheDir, "photo_${System.currentTimeMillis()}.jpg")
                photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)

                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
                Log.d("InstructionsActivity", "Cámara iniciada con URI: $photoUri")
            } catch (e: Exception) {
                Log.e("InstructionsActivity", "Error al abrir la cámara: ${e.message}")
            }
        } else {
            Log.d("InstructionsActivity", "Permiso de cámara no concedido")
            requestCameraPermission()
        }
    }

    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
            Log.d("InstructionsActivity", "Galería abierta")
        } catch (e: Exception) {
            Log.e("InstructionsActivity", "Error al abrir la galería: ${e.message}")
        }
    }

    // Manejar el resultado de la cámara
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("Debug", "onActivityResult() -> requestCode: $requestCode, resultCode: $resultCode")

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == RESULT_OK && ::photoUri.isInitialized) {
                    Log.d("InstructionsActivity", "Foto capturada con éxito: $photoUri")
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
                    saveImageAndProcess(bitmap)
                } else {
                    Log.d("InstructionsActivity", "Captura de foto cancelada o fallida")
                }
            }
            GALLERY_REQUEST_CODE -> {
                if (resultCode == RESULT_OK && data != null) {
                    val selectedImageUri = data.data
                    Log.d("InstructionsActivity", "Imagen seleccionada de la galería: $selectedImageUri")

                    if (selectedImageUri != null) {
                        // Mostrar imagen y confirmar con el usuario
                        showImageConfirmationDialog(selectedImageUri)
                    }
                } else {
                    Log.d("InstructionsActivity", "Selección de imagen cancelada o fallida")
                }
            }
        }
    }

    private fun showImageConfirmationDialog(imageUri: Uri) {
        try {
            // Convertir URI en Bitmap para mostrarlo
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

            // Crear un ImageView para mostrar la imagen en el diálogo
            val imageView = ImageView(this)
            imageView.setImageBitmap(bitmap)
            imageView.adjustViewBounds = true

            // Configurar el diálogo
            val builder = AlertDialog.Builder(this)
            builder.setTitle("¿Quieres usar esta imagen?")
            builder.setView(imageView) // Añadir el ImageView al diálogo
            builder.setPositiveButton("Usar") { dialog, _ ->
                dialog.dismiss()
                // Confirmar imagen seleccionada y enviarla para procesamiento
                saveImageAndProcess(bitmap)
                Log.d("InstructionsActivity", "Imagen confirmada por el usuario")
            }
            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                Log.d("InstructionsActivity", "Imagen rechazada por el usuario")
            }

            // Mostrar el diálogo
            builder.create().show()
        } catch (e: Exception) {
            Log.e("InstructionsActivity", "Error al mostrar la imagen: ${e.message}")
        }
    }


    private fun saveImageAndProcess(bitmap: Bitmap) {
        try {
            val file = File(externalCacheDir, "photo_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()

            Log.d("InstructionsActivity", "Imagen guardada en: ${file.absolutePath}")

            val imageProcessor = ImageProcessor(interpreter, this)
            imageProcessor.processImage(Uri.fromFile(file))
            Log.d("InstructionsActivity", "Imagen enviada para procesamiento")
        } catch (e: IOException) {
            Log.e("InstructionsActivity", "Error al guardar la imagen: ${e.message}")
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


