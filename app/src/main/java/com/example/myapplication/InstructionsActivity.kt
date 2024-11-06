package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil

@Suppress("DEPRECATION")
class InstructionsActivity : AppCompatActivity() {

    private lateinit var tflite: Interpreter
    private lateinit var database: DatabaseReference
    private val speciesLabels = mutableMapOf<Int, String>()  // Mapear índice a nombre de especie

    private val imgHeight = 180
    private val imgWidth = 180
    private val numSpecies = 100  // Ajusta según el número de especies en tu modelo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions_camera)

        // Inicializar el botón de cámara
        val cameraButton = findViewById<Button>(R.id.access_camera)
        cameraButton.setOnClickListener {
            openCamera()
        }

        // Inicializar Firebase y cargar etiquetas desde JSON
        database = FirebaseDatabase.getInstance().reference
        loadSpeciesLabelsFromFirebase()

        // Cargar el modelo TFLite
        val modelFile = FileUtil.loadMappedFile(this, "modelo_beta.tflite")
        tflite = Interpreter(modelFile)

        //Boton de regreso
        val btn: Button = findViewById(R.id.back_camera)
        btn.setOnClickListener {

            val intent: Intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)

        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            classifyImage(imageBitmap)
        }
    }

    private fun classifyImage(bitmap: Bitmap) {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, imgWidth, imgHeight, true)
        val inputArray = convertBitmapToInputArray(resizedBitmap)
        val outputArray = Array(1) { FloatArray(numSpecies) }

        tflite.run(inputArray, outputArray)

        // Obtener el índice de la especie predicha
        val speciesIndex = outputArray[0].indexOfMaxOrNull() ?: -1
        val speciesName = speciesLabels[speciesIndex] ?: "Especie desconocida"

        // Buscar datos en Firebase usando el nombre de la especie
        fetchSpeciesData(speciesName)
    }

    private fun convertBitmapToInputArray(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val input = Array(1) { Array(imgHeight) { Array(imgWidth) { FloatArray(3) } } }
        for (x in 0 until imgWidth) {
            for (y in 0 until imgHeight) {
                val pixel = bitmap.getPixel(x, y)
                input[0][x][y][0] = (pixel shr 16 and 0xFF) / 255.0f  // R
                input[0][x][y][1] = (pixel shr 8 and 0xFF) / 255.0f   // G
                input[0][x][y][2] = (pixel and 0xFF) / 255.0f         // B
            }
        }
        return input
    }

    private fun fetchSpeciesData(speciesName: String) {
        val speciesRef = database.child("species").child(speciesName)
        speciesRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val speciesInfo = dataSnapshot.value.toString()
                displaySpeciesInfo(speciesName, speciesInfo)
            } else {
                Toast.makeText(this, "Datos no encontrados para $speciesName", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displaySpeciesInfo(speciesName: String, speciesInfo: String) {
        // Aquí navegas o muestras la información de la especie
        Toast.makeText(this, "Especie: $speciesName\nInfo: $speciesInfo", Toast.LENGTH_LONG).show()
    }

    private fun loadSpeciesLabelsFromFirebase() {
        // Descargar el archivo JSON desde Firebase o cargar las etiquetas en speciesLabels
        val speciesRef = database.child("species_labels")
        speciesRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                for (speciesEntry in dataSnapshot.children) {
                    val code = speciesEntry.key?.toIntOrNull()
                    val name = speciesEntry.value?.toString()
                    if (code != null && name != null) {
                        speciesLabels[code] = name
                    }
                }
            } else {
                Toast.makeText(this, "No se encontraron etiquetas de especies", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar etiquetas de especies", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }
}

// Extensión para obtener el índice del valor máximo en un Array<Float>
fun FloatArray.indexOfMaxOrNull(): Int? {
    if (isEmpty()) return null
    var maxIndex = 0
    var maxValue = this[0]
    for (i in 1 until size) {
        if (this[i] > maxValue) {
            maxIndex = i
            maxValue = this[i]
        }
    }
    return maxIndex
}
