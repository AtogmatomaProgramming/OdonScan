package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.Animation
import android.widget.Button
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main)

        val btn: Button = findViewById(R.id.acceder)
        btn.setOnClickListener {

            val intent: Intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)

        }



    }
}