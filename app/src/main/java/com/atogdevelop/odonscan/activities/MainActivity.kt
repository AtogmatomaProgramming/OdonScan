package com.atogdevelop.odonscan.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.atogdevelop.odonscan.R

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main)

        //Botón de acceso a la "SecondActivtiy" o "Menu Screen"
        val btn: Button = findViewById(R.id.door_app)
        btn.setOnClickListener {

            val intent: Intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)

        }





    }
}