package com.atogdevelop.odonscan.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.atogdevelop.odonscan.R

class SecondActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val id_button: Button = findViewById(R.id.identify)
        id_button.setOnClickListener {

            val intentIdButton: Intent = Intent(this, InstructionsActivity::class.java)
            startActivity(intentIdButton)

        }

        val register_button: Button = findViewById(R.id.log_sign_in)
        register_button.setOnClickListener {

            val intentRegisterButton: Intent = Intent(this, ConstructionActivity::class.java)
            startActivity(intentRegisterButton)

        }

        val more_button: Button = findViewById(R.id.more)
        more_button.setOnClickListener {

            val intentMoreButton: Intent = Intent(this, ConstructionActivity::class.java)
            startActivity(intentMoreButton)

        }

    }
}