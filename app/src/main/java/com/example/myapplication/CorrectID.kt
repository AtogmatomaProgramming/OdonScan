package com.example.myapplication

import android.os.Bundle
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CorrectID : ComponentActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var speciesCName: TextView
    private lateinit var speciesVName: TextView
    private lateinit var speciesFaoCode: TextView
    private lateinit var speciesGeoDist: TextView
    private lateinit var speciesSize: TextView
    private lateinit var speciesDescription: TextView
    private lateinit var speciesImage: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_correct)

        // Inicializa las vistas
        speciesCName = findViewById(R.id.speciesCName)
        speciesVName = findViewById(R.id.speciesVName)
        speciesFaoCode = findViewById(R.id.speciesFaoCode)
        speciesGeoDist = findViewById(R.id.speciesGeoDist)
        speciesSize = findViewById(R.id.speciesSize)
        speciesDescription = findViewById(R.id.speciesDescription)
        speciesImage = findViewById(R.id.speciesImage)

        // Obtén la etiqueta desde el intent
        val speciesTag = intent.getStringExtra("SPECIES_TAG")

        // Inicializa Firebase Database
        database = FirebaseDatabase.getInstance().getReference("species")

        // Cargar datos
        loadSpeciesData(speciesTag ?: "unknown_species")

    }

    private fun loadSpeciesData(speciesTag: String) {
        database.child(speciesTag).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val  = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    val description = snapshot.child("description").getValue(String::class.java) ?: "No description available."
                    val imageUrl = snapshot.child("image").getValue(String::class.java)

                    // Actualiza las vistas
                    speciesCName.text = name
                    speciesDescription.text = description
                    Glide.with(this@CorrectID).load(imageUrl).into(speciesImage)
                } else {
                    speciesCName.text = "Species not found"
                    speciesDescription.text = "No information available."
                }
            }

            override fun onCancelled(error: DatabaseError) {
                speciesName.text = "Error"
                speciesDescription.text = "Failed to load data: ${error.message}"
            }
        })
    }

}


