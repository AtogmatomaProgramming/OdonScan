package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val btnUno: Button = findViewById(R.id.reconocimiento)
        btnUno.setOnClickListener {

            val intentUno: Intent = Intent(this, InstructionsActivity::class.java)
            startActivity(intentUno)

        }

        val btnDos: Button = findViewById(R.id.inicio_sesion)
        btnDos.setOnClickListener {

            val intentDos: Intent = Intent(this, ObrasActivity::class.java)
            startActivity(intentDos)

        }


    }
}