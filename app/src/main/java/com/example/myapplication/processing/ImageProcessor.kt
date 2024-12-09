package com.example.myapplication.processing

import android.content.Intent
import com.example.myapplication.activities.WrongID
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.myapplication.activities.CorrectID
import com.example.myapplication.activities.InstructionsActivity
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImageProcessor(private val interpreter: Interpreter, private val context: InstructionsActivity) {

    // Método para procesar la imagen y devolver el resultado
    fun processImage(imageUri: Uri) {
        // Convertir la URI de la imagen en un Bitmap
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)

        // Redimensionar la imagen a las dimensiones que requiere el modelo
        val resizedImage = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        // Convertir la imagen en un ByteBuffer que TensorFlow Lite pueda procesar
        val byteBuffer = convertBitmapToByteBuffer(resizedImage)

        // Crear un TensorBuffer de salida para la predicción
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 3), DataType.FLOAT32)  // Ajusta según el modelo

        // Ejecutar la inferencia
        interpreter.run(byteBuffer, outputBuffer.buffer.rewind())

        // Obtener las probabilidades de clasificación
        val confidences = outputBuffer.floatArray

        // Obtener el índice de la clase con la mayor confianza
        val maxConfidenceIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1

        if (maxConfidenceIndex != -1) {
            val speciesName = getSpeciesName(maxConfidenceIndex)

            if (speciesName == "elemento_desconocido") {
                navigateToWrongID()
            } else {
                navigateToCorrectID(speciesName)
            }
        } else {
            // Manejo de caso excepcional
            navigateToWrongID()
        }
    }

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
        val speciesList = listOf("elemento_desconocido", "lepidorhombus_whiffiagonis", "micromesistius_poutassou")
        return speciesList.getOrNull(index) ?: "Null identification"
    }

    private fun navigateToWrongID() {
        Log.d("Navigation", "Navigating to WrongID")
        val intent = Intent(context, WrongID::class.java)
        context.startActivity(intent)
    }

    // Redirigir a CorrectID
    private fun navigateToCorrectID(speciesName: String) {
        Log.d("Navigation", "Navigating to CorrectID with species: $speciesName")
        val intent = Intent(context, CorrectID::class.java)
        intent.putExtra("SPECIES_TAG", speciesName)
        context.startActivity(intent)
    }
}
