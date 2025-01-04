package com.atogdevelop.odonscan.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.atogdevelop.odonscan.R

class ConstructionActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_construction)
        Log.d("Debug", "Entrando en BuildingActivity desde ${intent.component?.className}")

        val btn: Button = findViewById(R.id.back)

        btn.setOnClickListener {
            finish()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()

    }
}