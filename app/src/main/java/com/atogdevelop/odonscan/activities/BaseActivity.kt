package com.atogdevelop.odonscan.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    private val inactivityTimeout: Long = 30 * 60 * 1000 // 30 minutos en milisegundos
    private var inactivityHandler: Handler? = null
    private val inactivityRunnable = Runnable {
        // Finaliza la aplicación tras inactividad
        finishAffinity() // Cierra todas las actividades de la pila
        System.exit(0) // Opcional: Termina el proceso
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inactivityHandler = Handler(Looper.getMainLooper()) // Inicializa el handler para el temporizador
    }

    override fun onResume() {
        super.onResume()
        // Detiene el temporizador al reanudar cualquier actividad
        inactivityHandler?.removeCallbacks(inactivityRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Inicia el temporizador al pausar cualquier actividad
        inactivityHandler?.postDelayed(inactivityRunnable, inactivityTimeout)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpia el handler para evitar fugas de memoria
        inactivityHandler?.removeCallbacks(inactivityRunnable)
    }
}
