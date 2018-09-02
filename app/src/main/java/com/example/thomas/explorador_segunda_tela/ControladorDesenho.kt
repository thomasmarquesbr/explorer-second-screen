package com.example.thomas.explorador_segunda_tela

import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.CountDownTimer
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.Gravity.START
import android.view.Gravity.TOP
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.*
import com.example.thomas.explorador_segunda_tela.dao.LupasDAO
import com.example.thomas.explorador_segunda_tela.helper.PreferencesHelper
import com.example.thomas.explorador_segunda_tela.model.Lupa
import com.example.thomas.explorador_segunda_tela.model.Tipo
import com.example.thomas.explorador_segunda_tela.view.CanvasView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.ArrayList

class ControladorDesenho(val context: Context,
                         val canvas: CanvasView,
                         val rootView: FrameLayout,
                         val widthDevice: Int,
                         val heightDevice: Int) {

    private val mPref = PreferencesHelper(context)
    private var sizeImage: Int = 0
    private var countTimer = 0
    private val lupasDAO = LupasDAO(context)
    private var listCoordinateX = mutableListOf<Float>()
    private var listCoordinateY = mutableListOf<Float>()
    private var currentCoordinateX: Float = Float.MIN_VALUE
    private var currentCoordinateY: Float = Float.MIN_VALUE
    private var isDrawing = false
    private val HEIGHT_BASE = 2048
    private val WIDTH_BASE = 1536
    private var tempoAtual = 0
    private var mostrarAcima = true
    private var mostrarEsquerda = true
    private var currentCoordinateIndex = 0

    private lateinit var timer: CountDownTimer
    private lateinit var timerDrawing: CountDownTimer

    init {
        sizeImage = if (widthDevice > 1079) 120 else 60
        canvas.setOnTouchListener { v, event ->
            return@setOnTouchListener true
        }
    }

    fun carregarDesenho() {
        val duration = mPref.getDuration()
        val listTimes = mPref.getListTimes()
        if (duration == 0 || listTimes == null || listTimes?.size < 9)
            return

        lerCoordenadas()
        val cores = criarListaCoresDasLupas(listTimes)

        val totalPontos = (duration * 1000) / 380
        canvas.shootEventTouch(
                MotionEvent.ACTION_DOWN, listCoordinateX[0] - 1, listCoordinateY[0] - 1)
        for (i in 0..totalPontos) {
            canvas.shootEventTouch(
                    MotionEvent.ACTION_MOVE, listCoordinateX[i], listCoordinateY[i])
        }
        canvas.shootEventTouch(
                MotionEvent.ACTION_DOWN, listCoordinateX[listCoordinateX.size-1] + 1, listCoordinateY[listCoordinateY.size-1] - 1)

        mostrarAcima = true
        mostrarEsquerda = true
        listTimes.forEachIndexed { i, it ->
            val currentTime = it.toInt()*1000/380
            currentCoordinateX = listCoordinateX[currentTime]
            currentCoordinateY = listCoordinateY[currentTime]
            exibirLupa(i, cores[i].cor, true)
        }
        currentCoordinateX = listCoordinateX.last()
        currentCoordinateY = listCoordinateY.last()
        finalizarDesenho()

    }

    fun inicializarDesenhoOffline() {
        val duration = mPref.getDuration()
        val listTimes = mPref.getListTimes()
        val cores = criarListaCoresDasLupas(listTimes)

        mostrarAcima = true
        mostrarEsquerda = true
        var currentTime = 0
        val timer = object : CountDownTimer((duration * 1000).toLong(), 1000) {
            var i = 0
            override fun onFinish() {
                finalizarDesenho()
            }
            override fun onTick(millisUntilFinished: Long) {
                listTimes?.forEach {
                    if (it.toInt() == currentTime) {
                        exibirLupa(i, cores[i].cor, true)
                        i++
                    }
                }
                currentTime++
            }
        }.start()

        currentCoordinateX = widthDevice * listCoordinateX[0] / WIDTH_BASE
        currentCoordinateY = heightDevice * listCoordinateY[0] / HEIGHT_BASE
        isDrawing = true
        (context as MainActivity).run {
            rootView.findViewById<ImageView>(R.id.x_image)
                    .visibility = View.INVISIBLE
            rootView.findViewById<ImageButton>(R.id.reload)
                    .visibility = View.INVISIBLE
            removeTodasAsLupas()
            val videoView = rootView.findViewById<VideoView>(R.id.video_view)
            with(videoView) {
                val videoPath = "android.resource://" + packageName + "/" + R.raw.animacao
                visibility = View.VISIBLE
                setVideoURI(Uri.parse(videoPath))
//                setMediaController(MediaController(context))
                setOnCompletionListener { videoView.visibility = View.INVISIBLE }
                start()
            }
            canvas.clearCanvas()
            canvas.shootEventTouch(MotionEvent.ACTION_DOWN, currentCoordinateX - 1, currentCoordinateY - 1)
            timerDrawing = object : CountDownTimer((duration * 1000).toLong(), 380) {
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

    private fun criarListaCoresDasLupas(listTimes: ArrayList<String>?): MutableList<Tipo> {
        val colors = mutableListOf<Tipo>()
        var index = 0
        listTimes?.let {
            it.forEach {
                if (index == 4)
                    index = 0
                colors.add(Tipo.values()[index])
                index++
            }
        }
        return colors
    }

    private fun removeTodasAsLupas() {
        (context as MainActivity).runOnUiThread {
            val canvasView = rootView.findViewById<CanvasView>(R.id.canvas_view)
            val imageView = rootView.findViewById<ImageView>(R.id.image_view)
            val videoView = rootView.findViewById<VideoView>(R.id.video_view)
            videoView.visibility = View.INVISIBLE
            val xImage = rootView.findViewById<ImageView>(R.id.x_image)
            xImage.visibility = View.INVISIBLE
            val ibReload = rootView.findViewById<ImageButton>(R.id.reload)
            ibReload.visibility = View.INVISIBLE
            val imageHat = rootView.findViewById<ImageView>(R.id.image_hat)
            val clearButton = rootView.findViewById<Button>(R.id.clear_button)
            rootView.removeAllViews()
            rootView.addView(canvasView)
            rootView.addView(imageView)
            rootView.addView(videoView)
            rootView.addView(xImage)
            rootView.addView(ibReload)
            rootView.addView(imageHat)
            rootView.addView(clearButton)
        }
    }

    fun inicializarDesenho(duracaoTotal: Int) {
        removeTodasAsLupas()
        lerCoordenadas()
        mPref.clear()
        mPref.putDuration(duracaoTotal)
        isDrawing = true
        tempoAtual = 0
        val size = if (listCoordinateX.size <= listCoordinateY.size) listCoordinateX.size else listCoordinateY.size
        currentCoordinateX = widthDevice * listCoordinateX[0] / WIDTH_BASE
        currentCoordinateY = heightDevice * listCoordinateY[0] / HEIGHT_BASE
        iniciarTemporizador(duracaoTotal)
        currentCoordinateIndex = 0
        (context as MainActivity).runOnUiThread {
            canvas.clearCanvas()
            canvas.shootEventTouch(MotionEvent.ACTION_DOWN, currentCoordinateX - 1, currentCoordinateY - 1)
            timerDrawing = object : CountDownTimer(((duracaoTotal - tempoAtual) * 1000).toLong(), 380) {
                override fun onFinish() {
                    canvas.shootEventTouch(MotionEvent.ACTION_DOWN, currentCoordinateX + 1, currentCoordinateY + 1)
                    isDrawing = false
                }
                override fun onTick(millisUntilFinished: Long) {
                    currentCoordinateX = widthDevice * listCoordinateX[currentCoordinateIndex] / WIDTH_BASE
                    currentCoordinateY = heightDevice * listCoordinateY[currentCoordinateIndex] / HEIGHT_BASE
                    moveChapeu()
                    canvas.shootEventTouch(MotionEvent.ACTION_MOVE, currentCoordinateX, currentCoordinateY)
                    currentCoordinateIndex++
                }
            }.start()
        }
    }

    private fun iniciarTemporizador(duracaoTotal: Int) {
        (context as MainActivity).runOnUiThread {
            timer = object : CountDownTimer(((duracaoTotal-tempoAtual) * 1000).toLong(), 1000) {
                override fun onFinish() {}
                override fun onTick(millisUntilFinished: Long) {
                    countTimer++
                }
            }.start()
        }
    }

    fun pausarDesenho(currentDuration: Int) {
        tempoAtual = currentDuration
        (context as MainActivity).runOnUiThread {
            if (timerDrawing != null)
                timerDrawing.cancel()
            if (timer != null)
                timer.cancel()
        }
    }

    fun retomarDesenho(currentDuration: Int) {
        tempoAtual = currentDuration
        if (mPref.getDuration() == 0)
            return

        isDrawing = true
        iniciarTemporizador(mPref.getDuration())
        (context as MainActivity).runOnUiThread {
            timerDrawing = object : CountDownTimer(((mPref.getDuration() - tempoAtual) * 1000).toLong(), 380) {
                override fun onFinish() {
                    canvas.shootEventTouch(MotionEvent.ACTION_DOWN, currentCoordinateX + 1, currentCoordinateY + 1)
                    isDrawing = false
                }
                override fun onTick(millisUntilFinished: Long) {
                    currentCoordinateX = widthDevice * listCoordinateX[currentCoordinateIndex] / WIDTH_BASE
                    currentCoordinateY = heightDevice * listCoordinateY[currentCoordinateIndex] / HEIGHT_BASE
                    moveChapeu()
                    canvas.shootEventTouch(MotionEvent.ACTION_MOVE, currentCoordinateX, currentCoordinateY)
                    currentCoordinateIndex++
                }
            }.start()
        }
    }

    fun finalizarDesenho() {
        (context as MainActivity).runOnUiThread {
            if (isDrawing)
                timerDrawing.cancel()
            val xImage = rootView.findViewById<ImageView>(R.id.x_image)
            with(xImage) {
                visibility = View.VISIBLE
                setImageDrawable(context.getDrawable(R.mipmap.x_image))
                x = (currentCoordinateX.toInt() - sizeImage / 2).toFloat()
                y = (currentCoordinateY.toInt() - sizeImage / 2).toFloat()
            }
            val reloadImage = rootView.findViewById<ImageButton>(R.id.reload)
            with(reloadImage) {
                visibility = View.VISIBLE
                x = (currentCoordinateX.toInt() + sizeImage/2).toFloat()
                y = (currentCoordinateY.toInt() - sizeImage/2).toFloat()
            }
            val chapeu = rootView.findViewById<ImageView>(R.id.image_hat)
            chapeu.visibility = View.INVISIBLE
        }
    }

    fun exibirLupa(id: Int, corLupa: String, isOffline: Boolean) {
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
                tag = "lupa"
                setImageDrawable(context.getDrawable(lupa.tipo.resIdLupa))
                layoutParams = paramsLupa
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener {
                    exibirDialog(lupa)
                }
            }
            if (!isOffline)
                mostrarImagem(lupa.resIdImage)
            rootView.addView(imageButton)

            val paramsTitulo = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            with(paramsTitulo) {
                gravity = START
                configuraLayoutTitulo()
                mPref.putPointX(leftMargin.toFloat())
                mPref.putPointY(topMargin.toFloat())
            }
            val tituloLupa = TextView(context)
            with(tituloLupa) {
                tag = "tituloLupa"
                text = lupa.title
                textSize = 22F
                layoutParams = paramsTitulo
                typeface = Typeface.createFromAsset(context.assets, "font/papyrus_let_bold.ttf")
//                typeface = Typeface.DEFAULT_BOLD
                setTextColor(ContextCompat.getColor(context, R.color.orange))
            }
            rootView.addView(tituloLupa)

            if (!isOffline)
                mPref.putTime(countTimer)
        }
    }

    private fun FrameLayout.LayoutParams.configuraLayoutTitulo() {
        if (mostrarAcima) {
            if (mostrarEsquerda) { // superior esquerdo
                leftMargin = currentCoordinateX.toInt() - 280
                topMargin = currentCoordinateY.toInt() - 100
                mostrarEsquerda = !mostrarEsquerda
            } else { //superior direito
                leftMargin = currentCoordinateX.toInt() + 20
                topMargin = currentCoordinateY.toInt() - 100
                mostrarEsquerda = !mostrarEsquerda
                mostrarAcima = !mostrarAcima
            }
        } else if (!mostrarAcima) {
            if (mostrarEsquerda) { // inferior esquerdo
                leftMargin = currentCoordinateX.toInt() - 280
                topMargin = currentCoordinateY.toInt() + 10
                mostrarEsquerda = !mostrarEsquerda
            } else { // inferior direito
                leftMargin = currentCoordinateX.toInt() - 40
                topMargin = currentCoordinateY.toInt() - 100
                mostrarEsquerda = !mostrarEsquerda
                mostrarAcima = !mostrarAcima
            }
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
        val readerX = BufferedReader(InputStreamReader(context.assets.open("coordinates/coord_x_2"), "UTF-8"))
        val readerY = BufferedReader(InputStreamReader(context.assets.open("coordinates/coord_y_2"), "UTF-8"))
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

    fun limparDesenho() {
        mostrarAcima = true
        mostrarEsquerda = true
        mPref.clear()
        canvas.clearCanvas()
        removeTodasAsLupas()
    }

}