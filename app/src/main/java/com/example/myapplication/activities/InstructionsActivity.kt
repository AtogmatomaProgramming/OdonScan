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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions_camera)

        // Botón para acceder a la cámara
        val accessCameraButton: Button = findViewById(R.id.access_camera)
        accessCameraButton.setOnClickListener {
            // Verificar permisos y abrir la cámara
            if (isCameraPermissionGranted()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
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
        val modelFile = FileUtil.loadMappedFile(this, "modelo_gamma.tflite")
        interpreter = Interpreter(modelFile)
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
        val photoFile = File(externalCacheDir, "photo_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    // Manejar el resultado de la cámara
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("Debug", "onActivityResult() -> requestCode: $requestCode, resultCode: $resultCode")

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d("Debug", "Intent data: $data")
                if (::photoUri.isInitialized) {
                    Log.d("Debug", "URI válida: $photoUri")
                    saveImageAndProcess(MediaStore.Images.Media.getBitmap(contentResolver, photoUri))
                } else {
                    Log.e("Debug", "Error: No se recibió URI válida")
                }
            } else {
                Log.e("Debug", "Error: Resultado inválido")
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
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Aquí también podrías añadir lógica adicional si es necesario
        finish()  // Esto finalizará la actividad al presionar el botón "Atrás"
    }
}


