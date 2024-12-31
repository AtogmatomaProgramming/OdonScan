package com.atogdevelop.odonscan.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.atogdevelop.odonscan.R

class BuildingActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_obras)
        Log.d("Debug", "Entrando en BuildingActivity desde ${intent.component?.className}")

        val btn: Button = findViewById(R.id.back)
        btn.setOnClickListener {
            finish()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Aquí también podrías añadir lógica adicional si es necesario
        finish()  // Esto finalizará la actividad al presionar el botón "Atrás"
    }
}