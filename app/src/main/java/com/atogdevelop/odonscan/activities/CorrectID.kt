package com.atogdevelop.odonscan.activities

import com.bumptech.glide.Glide
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.atogdevelop.odonscan.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot

class CorrectID : ComponentActivity() {

    private lateinit var speciesCName: TextView
    private lateinit var speciesVName: TextView
    private lateinit var speciesFaoCode: TextView
    private lateinit var speciesGeoDist: TextView
    private lateinit var speciesSize: TextView
    private lateinit var speciesDescription: TextView
    private lateinit var speciesSimilar: TextView
    private lateinit var speciesImage: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_correct)
        //FirebaseApp.initializeApp(this)

        Log.d("CorrectID", "onCreate() iniciado")
        // Inicializa las vistas
        speciesCName = findViewById(R.id.speciesCNameTextView)
        speciesVName = findViewById(R.id.speciesVNameTextView)
        speciesFaoCode = findViewById(R.id.speciesFaoCodeTextView)
        speciesGeoDist = findViewById(R.id.speciesGeoDistTextView)
        speciesSize = findViewById(R.id.speciesSizeTextView)
        speciesDescription = findViewById(R.id.speciesDescriptionTextView)
        speciesImage = findViewById(R.id.speciesImageView)
        speciesSimilar = findViewById(R.id.speciesSimilarTextView)

        // Obtén la etiqueta desde el intent
        val speciesTag = intent.getStringExtra("SPECIES_TAG")
        Log.d("CorrectID", "Species Tag: $speciesTag")


        // Cargar datos
        if (speciesTag.isNullOrEmpty()) {
            Log.e("CorrectID", "Especie no proporcionada o nula")
            speciesCName.text = "Error: Especie no proporcionada"
        } else {
            loadSpeciesData(speciesTag)
        }

    }

    private fun loadSpeciesData(speciesId: String) {
        // Referencia al documento en Firestore
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("marine_species").document(speciesId)

        Log.d("CorrectID", "Cargando datos de Firestore para especie: $speciesId")

        // Lee los datos del documento
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("CorrectID", "Datos encontrados para la especie: $speciesId")
                    updateUI(document)
                } else {
                    Log.e("CorrectID", "No se encontraron datos para la especie: $speciesId")
                    speciesCName.text = "Error: Especie no encontrada"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CorrectID", "Error al cargar los datos: ${exception.message}")
                speciesCName.text = "Error al cargar los datos"
            }
    }

    private fun updateUI(document: DocumentSnapshot) {
        // Obtiene los datos del documento
        val c_name = document.getString("c_name")
        val v_name = document.getString("v_name")
        val description = document.getString("description")
        val imageUrl = document.getString("image")
        val fao_code = document.getString("fao_code")
        val geo_dist = document.getString("geo_dist")
        val size = document.getString("medium_size")
        val similar = document.getString("similar")

        Log.d("CorrectID", "c_name: $c_name, v_name: $v_name, description: $description")
        Log.d("CorrectID", "imageUrl: $imageUrl, fao_code: $fao_code, geo_dist: $geo_dist")
        Log.d("CorrectID", "size: $size, similar: $similar")

        // Actualiza la UI con los datos obtenidos
        speciesCName.text = "${c_name ?: "Desconocido"}"
        speciesVName.text = "${v_name ?: "Desconocido"}"
        speciesDescription.text = "${description ?: "No disponible"}"
        speciesFaoCode.text = "Código FAO: ${fao_code ?: "No disponible"}"
        speciesGeoDist.text = "${geo_dist ?: "No especificada"}"
        speciesSize.text = "Tamaño Promedio: ${size ?: "No disponible"}"
        speciesSimilar.text = "Especies Similares: ${similar ?: "Ninguna"}"

        // Cargar imagen si está disponible
        if (imageUrl != null && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            Glide.with(this)
                .load(imageUrl)
                .error(R.drawable.image_not_found) // Imagen por defecto si falla la URL
                .into(speciesImage)
            Log.d("CorrectID", "Cargando imagen desde URL válida: $imageUrl")
        } else {
            Log.e("CorrectID", "URL no válida o no encontrada: $imageUrl")
            speciesImage.setImageResource(R.drawable.image_not_found) // Imagen por defecto
        }
    }



    override fun onBackPressed() {
        super.onBackPressed()
        // Aquí también podrías añadir lógica adicional si es necesario
        finish()  // Esto finalizará la actividad al presionar el botón "Atrás"
    }


}


