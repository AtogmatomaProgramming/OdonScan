package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class InstructionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions_camera)
        val btn: Button = findViewById(R.id.backCamera)
        btn.setOnClickListener {

            val intent: Intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)

        }
    }
}