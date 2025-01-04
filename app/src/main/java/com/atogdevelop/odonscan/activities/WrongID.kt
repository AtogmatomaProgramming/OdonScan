package com.atogdevelop.odonscan.activities

import android.os.Bundle
import android.widget.Button
import com.atogdevelop.odonscan.R

class WrongID: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wrong)

        val btn: Button = findViewById(R.id.back_wrong_id)
        btn.setOnClickListener {
            finish()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}