package com.example.thomas.explorador_segunda_tela;

import android.os.Build;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    @BindView(R.id.canvas_view) protected CanvasView canvasView;

    private String TAG = "MainActivity";
    ArrayList<Float> listCoordinateX = new ArrayList<>();
    ArrayList<Float> listCoordinateY = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        canvasView.setOnTouchListener(this);

    }

    @Override
    protected void onStop() {
        Log.e("x", listCoordinateX+"");
        Log.e("y",listCoordinateY+"");
        super.onStop();
    }

    public void onClickButton(View v) {
        canvasView.clearCanvas();
        startDrawing();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float coordinateX = event.getX();
        float coordinateY = event.getY();
        listCoordinateX.add(coordinateX);
        listCoordinateY.add(coordinateY);
        return false;
    }

    public void shootEventTouch(float coordinateX, float coordinateY) {
        // Obtain MotionEvent object
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = coordinateX;
        float y = coordinateY;
        // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_MOVE,
                x,
                y,
                metaState
        );
        // Dispatch touch event to view
        canvasView.dispatchTouchEvent(motionEvent);
//        canvasView.onTouchEvent(motionEvent);
    }

    public void startDrawing() {
        CountDownTimer timer = new CountDownTimer(50000, 500) {

            public void onTick(long millisUntilFinished) {
                Log.e(TAG, String.valueOf((millisUntilFinished/500)));
                shootEventTouch((millisUntilFinished/500),(millisUntilFinished/500));
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                shootEventTouch(1024f,1024f);
            }
        };
//        timer.start();
    }

}
