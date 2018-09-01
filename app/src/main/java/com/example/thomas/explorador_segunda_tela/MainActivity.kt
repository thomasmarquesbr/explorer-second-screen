package com.example.thomas.explorador_segunda_tela

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.example.thomas.explorador_segunda_tela.helper.PreferencesHelper
import com.example.thomas.explorador_segunda_tela.network.MulticastGroup
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
//    private var listCoordinateX = mutableListOf<Float>()
//    private var listCoordinateY = mutableListOf<Float>()
//    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//        listCoordinateX.add(event!!.x)
//        listCoordinateY.add(event!!.y)
//        return true
//    }

    private val heightDevice by lazy {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.heightPixels
    }

    private val widthDevice by lazy {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }

    private val multicastGroup by lazy { MulticastGroup(this) }
//    private val preferencesHelper by lazy { PreferencesHelper(this) }
    private val controladorDesenho by lazy { ControladorDesenho(this, canvas_view, root_view, widthDevice, heightDevice) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        hideSystemUI()

        reload.setOnClickListener {
            controladorDesenho.inicializarDesenhoOffline()
        }
        clear_button.setOnClickListener {
            controladorDesenho.limparDesenho()
        }
//        canvas_view.setOnTouchListener(this)
    }

    override fun onResume() {
        super.onResume()
        multicastGroup.startMessageReceiver()
        controladorDesenho.carregarDesenho()
    }

    override fun onStop() {
        multicastGroup.stopMessageReceiver()
//        Log.e("TAG", "${listCoordinateX.size}")
//        for (i in 0 until listCoordinateX.size) {
//            Log.e("X", "${listCoordinateX[i]}")
//        }
//        for (i in 0 until listCoordinateY.size) {
//            Log.e("Y", "${listCoordinateY[i]}")
//        }
        super.onStop()
    }

    // Methods

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    fun inicializarDesenho(duration: Int) {
        controladorDesenho.inicializarDesenho(duration)
    }

    fun retomarDesenho() {
        controladorDesenho.retomarDesenho()
    }

    fun pausarDesenho() {
        controladorDesenho.pausarDesenho()
    }

    fun finalizarDesenho() {
        controladorDesenho.finalizarDesenho()
    }

    fun exibirLupa(id: Int, corLupa: String) {
        controladorDesenho.exibirLupa(id, corLupa, false)
    }

}
