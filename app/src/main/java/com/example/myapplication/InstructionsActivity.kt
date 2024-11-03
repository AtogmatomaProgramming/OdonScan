package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.tensorflow.lite.Interpreter

class InstructionsActivity : AppCompactActivity() {

    private val cameraRequestCode = 100
    private lateinit var imageView: ImageView
    private lateinit var firebaseDatabase: DatabaseReference
    lateinit var tflite: Interpreter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions_camera)

        firebaseDatabase = FirebaseDatabase.getInstance().getReference("especies")

        val accessCameraButton: Button = findViewById(R.id.access_camera)
        imageView = findViewById(R.id.image_view)

        val modelFile = FileUtil.loadMappedFile(this, "modelo_beta.tflite")
        tflite = Interpreter(modelFile)

        accessCameraButton.setOnClickListener {
            openCamera()
        }

    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, cameraRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
            classifyImage(imageBitmap)
        }
    }

    // Función para obtener el nombre de la especie a partir del índice de predicción
    private fun getSpeciesNameFromIndex(index: Int): String {
        return speciesLabels[index]  // `speciesLabels` es la lista de nombres de especies, obtenida del archivo JSON
    }

    private fun convertBitmapToInputArray(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val input = Array(1) { Array(180) { Array(180) { FloatArray(3) } } }
        for (x in 0 until 180) {
            for (y in 0 until 180) {
                val pixel = bitmap.getPixel(x, y)
                input[0][x][y][0] = (pixel shr 16 and 0xFF) / 255.0f  // R
                input[0][x][y][1] = (pixel shr 8 and 0xFF) / 255.0f   // G
                input[0][x][y][2] = (pixel and 0xFF) / 255.0f         // B
            }
        }
        return input
    }



    private fun classifyImage(bitmap: Bitmap) {
        // Redimensionar la imagen al tamaño esperado por el modelo
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 180, 180, true)

        // Convertir el bitmap en un array de entrada para TFLite
        val inputArray = convertBitmapToInputArray(resizedBitmap)

        // Preparar el array de salida
        val outputArray = Array(1) { FloatArray(numSpecies) }  // `numSpecies` es el número total de especies

        // Ejecutar el modelo
        tflite.run(inputArray, outputArray)

        // Interpretar los resultados
        val speciesIndex = outputArray[0].indexOfMaxOrNull() ?: -1
        val speciesName = getSpeciesNameFromIndex(speciesIndex)  // Mapear el índice a un nombre de especie

        // Buscar datos en Firebase usando el nombre de la especie
        fetchSpeciesData(speciesName)
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
