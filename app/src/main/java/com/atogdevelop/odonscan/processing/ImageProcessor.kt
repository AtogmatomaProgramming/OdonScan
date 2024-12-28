package com.atogdevelop.odonscan.processing

import android.content.Intent
import com.atogdevelop.odonscan.activities.WrongID
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.atogdevelop.odonscan.activities.CorrectID
import com.atogdevelop.odonscan.activities.InstructionsActivity
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImageProcessor(private val interpreter: Interpreter, private val context: InstructionsActivity) {

    // Método para procesar la imagen y devolver el resultado
    fun processImage(imageUri: Uri) {

        try {

            Log.d("ImageProcessor", "Entrando en processImage() con URI: $imageUri")

            // Convertir la URI de la imagen en un Bitmap

            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            Log.d("ImageProcessor", "Imagen convertida a Bitmap con éxito")

            // Redimensionar la imagen a las dimensiones que requiere el modelo
            val resizedImage = Bitmap.createScaledBitmap(bitmap, 256, 256, true)
            Log.d("ImageProcessor", "Imagen redimensionada a 256x256")

            // Convertir la imagen en un ByteBuffer que TensorFlow Lite pueda procesar
            val byteBuffer = convertBitmapToByteBuffer(resizedImage)
            Log.d("ImageProcessor", "Imagen convertida a ByteBuffer")

            // Crear un TensorBuffer de salida para la predicción
            val outputBuffer = TensorBuffer.createFixedSize(
                intArrayOf(1, 3),
                DataType.FLOAT32
            )  // Ajusta según el modelo
            Log.d("ImageProcessor", "TensorBuffer de salida creado")

            // Ejecutar la inferencia
            interpreter.run(byteBuffer, outputBuffer.buffer.rewind())
            Log.d("ImageProcessor", "Inferencia ejecutada con éxito")

            // Obtener las probabilidades de clasificación
            val confidences = outputBuffer.floatArray
            Log.d("ImageProcessor", "Confidences obtenidas: ${confidences.joinToString(", ")}")

            // Obtener el índice de la clase con mayor confianza
            val maxConfidenceIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1
            Log.d("ImageProcessor", "Índice con mayor confianza: $maxConfidenceIndex")

            if (maxConfidenceIndex != -1) {
                val speciesName = getSpeciesName(maxConfidenceIndex)
                Log.d("ImageProcessor", "Nombre de la especie: $speciesName")

                if (speciesName == "elemento_desconocido") {
                    Log.d("ImageProcessor", "Especie desconocida, navegando a WrongID")
                    navigateToWrongID()
                } else {
                    Log.d(
                        "ImageProcessor",
                        "Especie reconocida: $speciesName, navegando a CorrectID"
                    )
                    navigateToCorrectID(speciesName)
                }
            } else {
                Log.e("ImageProcessor", "Error: No se encontró un índice válido")
                navigateToWrongID()
            }
        } catch (e: Exception) {
            Log.e("ImageProcessor", "Error durante el procesamiento de la imagen: ${e.message}")
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        Log.d("ImageProcessor", "Convirtiendo Bitmap a ByteBuffer")
        val buffer = ByteBuffer.allocateDirect(4 * 256 * 256 * 3) // Imagen RGB 256x256
        buffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(256 * 256)
        bitmap.getPixels(intValues, 0, 256, 0, 0, 256, 256)

        for (pixelValue in intValues) {
            buffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f) // Red
            buffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)  // Green
            buffer.putFloat((pixelValue and 0xFF) / 255.0f)          // Blue
        }
        Log.d("ImageProcessor", "Bitmap convertido a ByteBuffer con éxito")
        return buffer
    }

    // Función para obtener el nombre de la especie basado en el índice de la predicción
    private fun getSpeciesName(index: Int): String {
        Log.d("ImageProcessor", "Obteniendo nombre de la especie para índice: $index")
        val speciesList = listOf("elemento_desconocido", "lepidorhombus_whiffiagonis", "micromesistius_poutassou")
        val speciesName = speciesList.getOrNull(index) ?: "Null identification"
        Log.d("ImageProcessor", "Nombre de la especie obtenido: $speciesName")
        return speciesName
    }

    private fun navigateToWrongID() {
        Log.d("ImageProcessor", "Navegando a WrongID")
        val intent = Intent(context, WrongID::class.java)
        context.startActivity(intent)
    }

    private fun navigateToCorrectID(speciesName: String) {
        Log.d("ImageProcessor", "Navegando a CorrectID con nombre de especie: $speciesName")
        val intent = Intent(context, CorrectID::class.java)
        intent.putExtra("SPECIES_TAG", speciesName)
        context.startActivity(intent)
    }

}
