package com.atogdevelop.odonscan.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.atogdevelop.odonscan.R

class MainActivity : BaseActivity() {
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