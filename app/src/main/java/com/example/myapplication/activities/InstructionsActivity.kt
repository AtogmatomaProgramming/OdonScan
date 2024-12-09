package com.example.myapplication.activities


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
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.R
import com.example.myapplication.processing.ImageProcessor
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil



class InstructionsActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
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
            // Mostrar una explicación al usuario de por qué se necesita el permiso
            AlertDialog.Builder(this)
                .setMessage("Esta aplicación necesita acceso a la cámara para tomar fotos.")
                .setPositiveButton("Aceptar") { _, _ ->
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
                }
                .setNegativeButton("Cancelar", null)
                .create()
                .show()
        } else {
            // Solicitar permiso directamente si nunca se ha solicitado
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    // Manejar la respuesta del usuario a la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, abrir la cámara
                openCamera()
            } else {
                // Permiso denegado, mostrar mensaje
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        }
    }

    // Manejar el resultado de la cámara
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            // Obtener el bitmap de la imagen tomada
            val imageBitmap = data?.extras?.get("data") as Bitmap

            // Guardar la imagen temporalmente y procesarla con la IA
            saveImageAndProcess(imageBitmap)
        }
    }

    private fun saveImageAndProcess(bitmap: Bitmap) {
        try {
            // Guardar la imagen como archivo temporal
            val file = File(externalCacheDir, "photo_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()

            // Aquí delegamos el procesamiento de la imagen a la clase ImageProcessor
            val imageProcessor = ImageProcessor(interpreter, this)
            imageProcessor.processImage(Uri.fromFile(file))

        } catch (e: IOException) {
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


