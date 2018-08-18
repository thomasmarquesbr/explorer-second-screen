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

import com.example.thomas.explorador_segunda_tela.network.MulticastGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    @BindView(R.id.canvas_view) protected CanvasView canvasView;

    private String TAG = "MainActivity";
    ArrayList<Float> listCoordinateX = new ArrayList<>();
    ArrayList<Float> listCoordinateY = new ArrayList<>();
    private MulticastGroup multicastGroup;
    private String tag_multicast = "second_screen";
    private String ip_multicast = "230.192.0.10";
    private int port_multicast = 1027;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        canvasView.setOnTouchListener(this);
        startDrawing();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (multicastGroup == null)
            multicastGroup = new MulticastGroup(this, tag_multicast, ip_multicast, port_multicast);
        multicastGroup.startMessageReceiver();
    }

    @Override
    protected void onStop() {
//        Log.e("x", listCoordinateX+"");
//        Log.e("y",listCoordinateY+"");
//        for (Float coordinateX : listCoordinateX) {
//            Log.e("x","" + coordinateX);
//        }
//        Log.e("separate", "----------------");
//        for (Float coordinateY : listCoordinateY) {
//            Log.e("y","" + coordinateY);
//        }

        if (multicastGroup != null)
            multicastGroup.stopMessageReceiver();
        super.onStop();
    }

    public void onClickButton(View v) {
        canvasView.clearCanvas();
    }

    public void shootEventTouch(int motionEventType, float coordinateX, float coordinateY) {
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
                motionEventType,
                x,
                y,
                metaState
        );
        // Dispatch touch event to view
//        canvasView.dispatchTouchEvent(motionEvent);
        canvasView.onTouchEvent(motionEvent);
    }

    public void readFilesCoordinates() {
        BufferedReader readerX = null;
        BufferedReader readerY = null;
        try {
            readerX = new BufferedReader(new InputStreamReader(getAssets().open("coordinates/coord_x"), "UTF-8"));
            readerY = new BufferedReader(new InputStreamReader(getAssets().open("coordinates/coord_y"), "UTF-8"));
            // do reading, usually loop until end of file reading
            String mLineX;
            while ((mLineX = readerX.readLine()) != null) {
                listCoordinateX.add(Float.parseFloat(mLineX));
            }
            String mLineY;
            while ((mLineY = readerY.readLine()) != null) {
                listCoordinateY.add(Float.parseFloat(mLineY));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (readerX != null) {
                try {
                    readerX.close();
                } catch (IOException e) { e.printStackTrace(); }
            }
            if (readerY != null) {
                try {
                    readerY.close();
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    public void startDrawing() {
        readFilesCoordinates();
        final int size = (listCoordinateX.size() <= listCoordinateY.size()) ? listCoordinateX.size() : listCoordinateY.size();
        shootEventTouch(MotionEvent.ACTION_DOWN, listCoordinateX.get(0) - 1, listCoordinateY.get(0) - 1);
        CountDownTimer timer = new CountDownTimer(size * 200, 200) {
            private int i = 0;
            @Override
            public void onTick(long millisUntilFinished) {
                shootEventTouch(MotionEvent.ACTION_MOVE, listCoordinateX.get(i), listCoordinateY.get(i));
                i++;
            }
            @Override
            public void onFinish() {
                shootEventTouch(MotionEvent.ACTION_DOWN, listCoordinateX.get(listCoordinateX.size()-1) + 1, listCoordinateY.get(listCoordinateY.size()-1) + 1);
            }
        };
        timer.start();
    }

    public void stopDrawing() {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }
}
