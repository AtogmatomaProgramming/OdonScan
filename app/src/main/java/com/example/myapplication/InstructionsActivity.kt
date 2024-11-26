package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import org.tensorflow.lite
import androidx.appcompat.app.AppCompatActivity
import java.nio.ByteBuffer
import java.nio.ByteOrder

class InstructionsActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1 // Código para la solicitud de la cámara
    private lateinit var imageView: ImageView
    private lateinit var cameraButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        cameraButton = findViewById(R.id.access_camera)

        cameraButton.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    // Intent para abrir la cámara
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    // Procesar la imagen cuando la cámara devuelve el resultado
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Obtener la imagen capturada
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)

            // Preprocesar la imagen y pasarla al modelo para hacer la predicción
            val processedBitmap = preprocessImage(imageBitmap)
            val predictedClass = runModelInference(processedBitmap)

            // Si la predicción es válida, mostrar la información desde Firebase
            if (predictedClass >= 0) {
                val intent = Intent(this, SpeciesInfoActivity::class.java)
                intent.putExtra("predicted_class", predictedClass)
                startActivity(intent)
            } else {
                // Si no se reconoce la especie, mostrar la pantalla de error
                val intent = Intent(this, WrongID::class.java)
                startActivity(intent)
            }
        }
    }

    // Preprocesar la imagen para el modelo
    private fun preprocessImage(originalImage: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(originalImage, 224, 224, true)  // Ajusta según el tamaño esperado
    }

    // Ejecutar la inferencia del modelo
    private fun runModelInference(bitmap: Bitmap): Int {
        val input = convertBitmapToByteBuffer(bitmap)

        // Obtener dinámicamente el número de clases desde el modelo
        val numClasses = tflite.outputTensor(0).shape()[1]
        val output = Array(1) { FloatArray(numClasses) }

        // Ejecutar la inferencia
        tflite.run(input, output)

        // Encontrar la clase con mayor probabilidad
        var predictedClass = -1
        var maxProbability = -1f

        for (i in output[0].indices) {
            if (output[0][i] > maxProbability) {
                maxProbability = output[0][i]
                predictedClass = i
            }
        }

        // Retornar la clase predicha si supera un umbral de confianza
        return if (maxProbability > 0.5) predictedClass else -1
    }


    // Convertir la imagen a ByteBuffer para la entrada del modelo
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(224 * 224)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val value = intValues[pixel++]
                byteBuffer.putFloat(((value shr 16) and 0xFF) / 255.0f)  // Rojo
                byteBuffer.putFloat(((value shr 8) and 0xFF) / 255.0f)   // Verde
                byteBuffer.putFloat((value and 0xFF) / 255.0f)          // Azul
            }
        }
        return byteBuffer
    }
}

