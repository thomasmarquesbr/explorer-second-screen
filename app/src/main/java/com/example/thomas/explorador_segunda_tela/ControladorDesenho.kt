package com.example.thomas.explorador_segunda_tela

import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.text.Layout
import android.view.Gravity
import android.view.Gravity.START
import android.view.Gravity.TOP
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.example.thomas.explorador_segunda_tela.dao.LupasDAO
import com.example.thomas.explorador_segunda_tela.model.Lupa
import com.example.thomas.explorador_segunda_tela.model.Tipo
import com.example.thomas.explorador_segunda_tela.view.CanvasView
import java.io.BufferedReader
import java.io.InputStreamReader

class ControladorDesenho(val context: Context,
                         val canvas: CanvasView,
                         val rootView: FrameLayout,
                         val widthDevice: Int,
                         val heightDevice: Int) {

    private var sizeImage: Int = 0
    private val lupasDAO = LupasDAO(context)
    private var listCoordinateX = mutableListOf<Float>()
    private var listCoordinateY = mutableListOf<Float>()
    private var currentCoordinateX: Float = Float.MIN_VALUE
    private var currentCoordinateY: Float = Float.MIN_VALUE
    private var isDrawing = false
    private val HEIGHT_BASE = 2048
    private val WIDTH_BASE = 1536
    private var tempoAtual = 0
    private var mostrarAcima = false

    private lateinit var timerDrawing: CountDownTimer

    init {
        sizeImage = if (widthDevice > 1079) 120 else 60
        canvas.setOnTouchListener { v, event ->
            canvas.onTouchEvent(event)
            return@setOnTouchListener false
        }
    }

    fun inicializarDesenho(duracaoTotal: Int) {
        lerCoordenadas()
        isDrawing = true
        tempoAtual = 0
        val size = if (listCoordinateX.size <= listCoordinateY.size) listCoordinateX.size else listCoordinateY.size
        currentCoordinateX = widthDevice * listCoordinateX[0] / WIDTH_BASE
        currentCoordinateY = heightDevice * listCoordinateY[0] / HEIGHT_BASE
        (context as MainActivity).runOnUiThread {
            canvas.shootEventTouch(MotionEvent.ACTION_DOWN, currentCoordinateX - 1, currentCoordinateY - 1)
            timerDrawing = object : CountDownTimer(((duracaoTotal - tempoAtual) * 1000).toLong(), 500) {
                private var i = 0
                override fun onFinish() {
                    canvas.shootEventTouch(MotionEvent.ACTION_DOWN, currentCoordinateX + 1, currentCoordinateY + 1)
                }
                override fun onTick(millisUntilFinished: Long) {
                currentCoordinateX = widthDevice * listCoordinateX[i] / WIDTH_BASE
                currentCoordinateY = heightDevice * listCoordinateY[i] / HEIGHT_BASE
                    moveChapeu()
                    canvas.shootEventTouch(MotionEvent.ACTION_MOVE, currentCoordinateX, currentCoordinateY)
                    i++
                }
            }.start()
        }
    }

    fun pausarDesenho() {

    }

    fun retomarDesenho() {

    }

    fun finalizarDesenho() {
        (context as MainActivity).runOnUiThread {
            timerDrawing.cancel()
            val xImage = rootView.findViewById<ImageView>(R.id.x_image)
            with(xImage) {
                visibility = View.VISIBLE
                setImageDrawable(context.getDrawable(R.mipmap.x_image))
                x = (currentCoordinateX.toInt() - sizeImage / 2).toFloat()
                y = (currentCoordinateY.toInt() - sizeImage / 2).toFloat()
            }
            val chapeu = rootView.findViewById<ImageView>(R.id.image_hat)
            chapeu.visibility = View.INVISIBLE
        }
    }

    fun exibirLupa(id: Int, corLupa: String) {
        val lupa = lupasDAO.lupas[id]
        lupa.tipo = Tipo.valueOf(corLupa)
        (context as MainActivity).runOnUiThread {
            var paramsLupa = FrameLayout.LayoutParams(120, 120)
            with(paramsLupa) {
                gravity = TOP or START
                leftMargin = currentCoordinateX.toInt() - paramsLupa.width / 2
                topMargin = currentCoordinateY.toInt() - paramsLupa.height / 2
            }
            val imageButton = ImageButton(context)
            with(imageButton) {
                setImageDrawable(context.getDrawable(lupa.tipo.resIdLupa))
                layoutParams = paramsLupa
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener {
                    exibirDialog(lupa)
                }
            }
            mostrarImagem(lupa.resIdImage)
            rootView.addView(imageButton)

            val paramsTitulo = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            with(paramsTitulo) {
                gravity = START
                if (mostrarAcima) {
                    leftMargin = currentCoordinateX.toInt()
                    topMargin = currentCoordinateY.toInt() + 10
                } else {
                    leftMargin = currentCoordinateX.toInt() - 120
                    topMargin = currentCoordinateY.toInt() - 120
                }
                mostrarAcima = !mostrarAcima
            }
            val tituloLupa = TextView(context)
            with(tituloLupa) {
                text = lupa.title
                textSize = 22F
                layoutParams = paramsTitulo
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(ContextCompat.getColor(context, R.color.orange))
            }
            rootView.addView(tituloLupa)
        }
    }

    private fun exibirDialog(lupa: Lupa) {
        val metrics = context.getResources().displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val dialog = Dialog(context)
        with(dialog) {
            window?.requestFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.custom_dialog)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setGravity(Gravity.BOTTOM)
            window?.setLayout(ActionBar.LayoutParams.MATCH_PARENT, height / 3)
        }
        val titulo = dialog.findViewById<TextView>(R.id.tv_title)
        with(titulo) {
            textSize = (if (widthDevice > 1079) 24 else 16).toFloat()
            text = lupa.title
        }
        val descricao = dialog.findViewById<TextView>(R.id.tv_text)
        descricao.text = lupa.description
        val imagem = dialog.findViewById<ImageView>(R.id.iv_image)
        imagem.setImageResource(lupa.resIdImage)
        val botaoFechar = dialog.findViewById<ImageButton>(R.id.ib_close)
        botaoFechar.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun mostrarImagem(resIdImage: Int) {
        val imageView = rootView.findViewById<ImageView>(R.id.image_view)
        with(imageView) {
            setImageDrawable(context.getDrawable(resIdImage))
            visibility = View.VISIBLE
        }
        val handler = Handler()
        handler.postDelayed({
            imageView.visibility = View.INVISIBLE
        }, 5000)
    }

    fun moveChapeu() {
        val imagemChapeu = rootView.findViewById<ImageView>(R.id.image_hat)
        with(imagemChapeu) {
            minimumWidth = sizeImage
            minimumHeight = sizeImage
            if (visibility == View.INVISIBLE)
                visibility = View.VISIBLE
            x = currentCoordinateX - width / 2
            y = currentCoordinateY - height / 2
        }
    }

    private fun lerCoordenadas() {
        listCoordinateX = mutableListOf()
        listCoordinateY = mutableListOf()
        val readerX = BufferedReader(InputStreamReader(context.assets.open("coordinates/coord_x"), "UTF-8"))
        val readerY = BufferedReader(InputStreamReader(context.assets.open("coordinates/coord_y"), "UTF-8"))
        var mLineX = readerX.readLine()
        while (mLineX  != null) {
            listCoordinateX.add(java.lang.Float.parseFloat(mLineX))
            mLineX = readerX.readLine()
        }
        var mLineY = readerY.readLine()
        while (mLineY  != null) {
            listCoordinateY.add(java.lang.Float.parseFloat(mLineY))
            mLineY = readerY.readLine()
        }
        readerX.close()
        readerY.close()
    }

}