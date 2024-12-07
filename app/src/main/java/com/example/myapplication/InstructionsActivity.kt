package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class InstructionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions_camera)

        val accessCameraButton: Button = findViewById(R.id.access_camera)

        accessCameraButton.setOnClickListener {
            // Inicia CameraActivity para capturar la imagen
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        val btn: Button = findViewById(R.id.back_camera)
        btn.setOnClickListener {
            val intent: Intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }
}

