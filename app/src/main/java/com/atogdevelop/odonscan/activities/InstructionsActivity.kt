package com.atogdevelop.odonscan.activities


import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
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


class InstructionsActivity : BaseActivity() {

    private lateinit var photoUri: Uri
    private val CAMERA_PERMISSION_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions_camera)

        Log.d("InstructionsActivity", "onCreate() iniciado")

        val accessCameraButton: Button = findViewById(R.id.access_camera)
        accessCameraButton.setOnClickListener {
            Log.d("InstructionsActivity", "Botón de cámara presionado")
            showImageSourceDialog()
        }

        val backButton: Button = findViewById(R.id.back_camera)
        backButton.setOnClickListener {
            Log.d("InstructionsActivity", "Botón de retroceso presionado")
            finish()
        }

    }

    //Función que muestra el diálogo para elegir entre la cámara o la galería
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

    //Función para verificar si el permiso de cámara está concedido
    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    //Función para solicitar el permiso de la cámara, en caso de que no esté concedido
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

    //Función para gestionar el permiso de uso de la cámara
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

    //Función para abrir la cámara para tomar la foto
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

    //Función para abrir la galería, en caso de que se elija esa opción
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

    //Función para manejar la imagen que se va a analizar (de la cámara o de la galería)
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

                        showImageConfirmationDialog(selectedImageUri)
                    }
                } else {
                    Log.d("InstructionsActivity", "Selección de imagen cancelada o fallida")
                }
            }
        }
    }

    //Función que muestra la imagen para confirmarla por el usuario
    private fun showImageConfirmationDialog(imageUri: Uri) {
        try {

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

            val dialogView = layoutInflater.inflate(R.layout.dialog_image_confirmation, null)

            val imageView = dialogView.findViewById<ImageView>(R.id.dialogImageView)
            imageView.setImageBitmap(bitmap)

            val buttonUse = dialogView.findViewById<Button>(R.id.buttonUse)
            val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)

            val dialog = builder.create()

            buttonUse.setOnClickListener {
                dialog.dismiss()
                saveImageAndProcess(bitmap)
                Log.d("InstructionsActivity", "Imagen confirmada por el usuario")
            }

            buttonCancel.setOnClickListener {
                dialog.dismiss()
                Log.d("InstructionsActivity", "Imagen rechazada por el usuario")
            }

            dialog.show()
        } catch (e: Exception) {
            Log.e("InstructionsActivity", "Error al mostrar la imagen: ${e.message}")
        }
    }

    //Función para procesar la imagen a través de la clase "ImageProcessor"
    private fun saveImageAndProcess(bitmap: Bitmap) {
        try {
            val file = File(externalCacheDir, "photo_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()

            Log.d("InstructionsActivity", "Imagen guardada en: ${file.absolutePath}")

            val imageProcessor = ImageProcessor(this)
            imageProcessor.processImage(Uri.fromFile(file))
            Log.d("InstructionsActivity", "Imagen enviada para procesamiento")
        } catch (e: IOException) {
            Log.e("InstructionsActivity", "Error al guardar la imagen: ${e.message}")
        }
    }

    //Variables para la gestión de permisos de la cámara o de la galería
    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val GALLERY_REQUEST_CODE = 102
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
