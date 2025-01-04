package com.atogdevelop.odonscan.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    //Nota. Actividad de la que heredan el resto, de cara a que las mismas terminen
    //cuando pase un determinado tiempo (en este caso 5 minutos) de inactividad

    private val inactivityTimeout: Long = 5 * 60 * 1000
    private var inactivityHandler: Handler? = null
    private val inactivityRunnable = Runnable {

        finishAffinity()
        System.exit(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Nota. Inicializa el handler para el temporizador
        inactivityHandler = Handler(Looper.getMainLooper())
    }

    override fun onResume() {
        super.onResume()
        //Nota. Detiene el temporizador al reanudar cualquier actividad
        inactivityHandler?.removeCallbacks(inactivityRunnable)
    }

    override fun onPause() {
        super.onPause()
        //Nota. Inicia el temporizador al pausar cualquier actividad
        inactivityHandler?.postDelayed(inactivityRunnable, inactivityTimeout)
    }

    override fun onDestroy() {
        super.onDestroy()
        //Nota. Limpieza del handler para evitar fugas de memoria
        inactivityHandler?.removeCallbacks(inactivityRunnable)
    }
}
