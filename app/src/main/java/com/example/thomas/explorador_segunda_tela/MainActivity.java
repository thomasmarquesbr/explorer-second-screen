package com.example.thomas.explorador_segunda_tela;

import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.thomas.explorador_segunda_tela.network.MulticastGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.LEFT;
import static android.view.Gravity.START;
import static android.view.Gravity.TOP;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {

    @BindView(R.id.root_view) protected FrameLayout root_view;
    @BindView(R.id.canvas_view) protected CanvasView canvasView;

    private String TAG = "MainActivity";
    ArrayList<Float> listCoordinateX = new ArrayList<>();
    ArrayList<Float> listCoordinateY = new ArrayList<>();
    private MulticastGroup multicastGroup;
    private String tag_multicast = "second_screen";
    private String ip_multicast = "230.192.0.10";
    private int port_multicast = 1027;

    private boolean shouldDraw = false;

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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

//    public void onClickButton(View v) {
//        canvasView.clearCanvas();
//    }



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
        shouldDraw = true;
        final int size = (listCoordinateX.size() <= listCoordinateY.size()) ? listCoordinateX.size() : listCoordinateY.size();
        canvasView.shootEventTouch(MotionEvent.ACTION_DOWN, listCoordinateX.get(0) - 1, listCoordinateY.get(0) - 1);
        CountDownTimer timer = new CountDownTimer(size * 50, 50) {
            private int i = 0;
            @Override
            public void onTick(long millisUntilFinished) {
                if (i % 50 == 0) {
                    putImageOnView(listCoordinateX.get(i), listCoordinateY.get(i));
                }
                if (shouldDraw) {
                    canvasView.shootEventTouch(MotionEvent.ACTION_MOVE, listCoordinateX.get(i), listCoordinateY.get(i));
                    i++;
                }
            }
            @Override
            public void onFinish() {
                canvasView.shootEventTouch(MotionEvent.ACTION_DOWN, listCoordinateX.get(listCoordinateX.size()-1) + 1, listCoordinateY.get(listCoordinateY.size()-1) + 1);
                shouldDraw = false;
            }
        };
        timer.start();
    }

    public void stopDraw() {
        shouldDraw = false;
    }

    public void putImageOnView(float x, float y) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(120, 120);
        layoutParams.gravity = TOP | START;
        layoutParams.leftMargin = (int) x - (layoutParams.width / 2);
        layoutParams.topMargin = (int) y - (layoutParams.height / 2);
        ImageButton imageButton = new ImageButton(this);
        imageButton.setImageDrawable(getDrawable(R.drawable.magnify_red));
        imageButton.setLayoutParams(layoutParams);
        imageButton.setBackgroundColor(Color.TRANSPARENT);
        imageButton.setOnClickListener(this);
        root_view.addView(imageButton);
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "TESTE");
    }
}
