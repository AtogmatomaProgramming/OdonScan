package com.example.myapplication.activities

import com.bumptech.glide.Glide
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.myapplication.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class CorrectID : ComponentActivity() {

    private lateinit var database: DatabaseReference
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

        // Inicializa Firebase Database
        database = FirebaseDatabase.getInstance().getReference("marine_species")

        // Cargar datos
        loadSpeciesData(speciesTag ?: "elemento_desconocido")

    }

    private fun loadSpeciesData(speciesId: String) {
        // Referencia al nodo species en Firebase
        val databaseRef = FirebaseDatabase.getInstance().getReference("marine_species/$speciesId")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                    val c_name = snapshot.child("c_name").getValue(String::class.java)
                    val v_name = snapshot.child("v_name").getValue(String::class.java)
                    val description = snapshot.child("description").getValue(String::class.java)
                    val imageUrl = snapshot.child("image").getValue(String::class.java)
                    val fao_code = snapshot.child("fao_code").getValue(String::class.java)
                    val geo_dist = snapshot.child("geo_dist").getValue(String::class.java)
                    val size = snapshot.child("medium_size").getValue(String::class.java)
                    val similar = snapshot.child("similar").getValue(String::class.java)

                    // Actualiza la UI con los datos obtenidos
                    Glide.with(this@CorrectID).load(imageUrl).into(speciesImage)

                    speciesCName.text = "Nombre Científico: $c_name"
                    speciesVName.text = "Nombre Vernáculo: $v_name"
                    speciesDescription.text = "Descripción: $description"
                    speciesFaoCode.text = "Código FAO: $fao_code"
                    speciesGeoDist.text = "Distribución Geográfica: $geo_dist"
                    speciesSize.text = "Tamaño Promedio: $size"
                    speciesSimilar.text = "Especies Similares: $similar"

            }

            override fun onCancelled(error: DatabaseError) {
                // Maneja el error (puedes dejarlo vacío si no necesitas manejarlo)
                println("Error al leer los datos: ${error.message}")
            }

        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Aquí también podrías añadir lógica adicional si es necesario
        finish()  // Esto finalizará la actividad al presionar el botón "Atrás"
    }


}


