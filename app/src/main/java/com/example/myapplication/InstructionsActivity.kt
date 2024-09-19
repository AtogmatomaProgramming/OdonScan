package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Bitmap
import android.provider.MediaStore
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class InstructionsActivity : AppCompactActivity() {

    private lateinit var interpreter: Interpreter
    private val inputImageSize = 180  // Asumiendo que el modelo espera imágenes de 180x180
    private val numChannels = 3       // Asumiendo que el modelo trabaja con imágenes en color (RGB)
    private val numClasses = 5        // Ajustar al número real de clases en tu modelo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions_camera)

        // Cargar el modelo TFLite
        interpreter = Interpreter(loadModelFile())

        // El resto de tu código para los botones (back y cámara) permanece igual
    }

    // Cargar el modelo desde la carpeta assets
    private fun loadModelFile(): ByteBuffer {
        val assetFileDescriptor = assets.openFd("modelo_alpha.tflite")
        val fileInputStream = assetFileDescriptor.createInputStream()
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Función para preprocesar la imagen y clasificarla
    private fun classifyImage(bitmap: Bitmap) {
        // Redimensionar la imagen a la que espera el modelo
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputImageSize, inputImageSize, true)

        // Convertir la imagen a un ByteBuffer adecuado para el modelo
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

        // Array para recibir la salida del modelo
        val output = Array(1) { FloatArray(numClasses) }

        // Hacer la inferencia
        interpreter.run(inputBuffer, output)

        // Obtener el índice de la clase con mayor probabilidad
        val predictedIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1

        // Mostrar o procesar el resultado de la predicción
        val speciesName = getSpeciesName(predictedIndex)
        println("Especie predicha: $speciesName")
    }

    // Convertir la imagen en un ByteBuffer que pueda usar TensorFlow Lite
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputImageSize * inputImageSize * numChannels)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputImageSize * inputImageSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        // Recorrer los píxeles y agregarlos al ByteBuffer
        var pixelIndex = 0
        for (i in 0 until inputImageSize) {
            for (j in 0 until inputImageSize) {
                val pixel = intValues[pixelIndex++]
                // Extraer los canales de color y agregar al buffer
                byteBuffer.putFloat(((pixel shr 16 and 0xFF) / 255.0f))
                byteBuffer.putFloat(((pixel shr 8 and 0xFF) / 255.0f))
                byteBuffer.putFloat(((pixel and 0xFF) / 255.0f))
            }
        }

        return byteBuffer
    }

    // Función para mapear el índice de la predicción a una especie
    private fun getSpeciesName(index: Int): String {
        val speciesList = listOf("Especie 1", "Especie 2", "Especie 3", "Especie 4", "Especie 5")
        return speciesList.getOrElse(index) { "Especie desconocida" }
    }
}


    // Función para clasificar la imagen
    private fun classifyImage(bitmap: Bitmap) {
        // Implementar la función de clasificación aquí
    }
}
