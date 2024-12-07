package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CameraActivity : ComponentActivity() {

    private lateinit var interpreter: Interpreter
    private lateinit var resultTextView: TextView
    private lateinit var capturedImageView: ImageView
    private val cameraRequestCode = 1001  // Código para identificar la solicitud de cámara

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        capturedImageView = findViewById(R.id.capturedImage)
        resultTextView = findViewById(R.id.resultText)  // Asegúrate de tener un TextView para mostrar los resultados
        val captureButton: Button = findViewById(R.id.capture_button)

        //Cargar Modelo
        loadModel()

        // Abrir la cámara al presionar el botón
        captureButton.setOnClickListener {
            openCamera()
        }
    }

    // Cargar el modelo TensorFlow Lite
    private fun loadModel() {
        val model = assets.open("modelo_gamma.tflite").use {
            it.readBytes()
        }
        interpreter = Interpreter(model)
    }

    // Función para abrir la cámara
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, cameraRequestCode)
    }

    // Función para manejar el resultado de la cámara (cuando se toma la foto)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == cameraRequestCode && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap

            // Mostrar la imagen capturada en el ImageView
            capturedImageView.setImageBitmap(imageBitmap)

            // Procesar la imagen con el modelo
            processImage(imageBitmap)
        }
    }

    // Función para procesar la imagen con el modelo de TensorFlow Lite
    private fun processImage(bitmap: Bitmap) {
        // Redimensionar la imagen a las dimensiones que requiere el modelo
        val resizedImage = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        // Convertir la imagen en un ByteBuffer que TensorFlow Lite pueda procesar
        val byteBuffer = convertBitmapToByteBuffer(resizedImage)

        // Crear un TensorBuffer de salida para la predicción
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 2), DataType.FLOAT32)  // Ajusta según el modelo

        // Ejecutar la inferencia
        interpreter.run(byteBuffer, outputBuffer.buffer.rewind())

        // Obtener las probabilidades de clasificación
        val confidences = outputBuffer.floatArray

        // Obtener el índice de la clase con la mayor confianza
        val maxConfidenceIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1

        // Mostrar el resultado
        if (maxConfidenceIndex != -1) {
            resultTextView.text = "Especie: ${getSpeciesName(maxConfidenceIndex)} (Confianza: ${confidences[maxConfidenceIndex]})"
        } else {
            resultTextView.text = "Problema al identificar la espece"
        }
    }

    // Función para convertir el Bitmap a un ByteBuffer
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)  // Imagen RGB 224x224
        buffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(224 * 224)
        bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)

        for (pixelValue in intValues) {
            buffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)  // Red
            buffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)   // Green
            buffer.putFloat((pixelValue and 0xFF) / 255.0f)           // Blue
        }
        return buffer
    }

    // Función para obtener el nombre de la especie basado en el índice de la predicción
    private fun getSpeciesName(index: Int): String {
        val speciesList = listOf("elemento_desconocido","lepidorhombus_whiffiagonis", "micromessitus_poutassou")
        return speciesList.getOrNull(index) ?: "Desconocido"
    }



}

