package com.example.thomas.explorador_segunda_tela;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.thomas.explorador_segunda_tela.helper.PreferencesHelper;
import com.example.thomas.explorador_segunda_tela.network.MulticastGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.Gravity.END;
import static android.view.Gravity.START;
import static android.view.Gravity.TOP;

enum ColorLink {
    RED("RED", R.drawable.ic_red_magnify),
    GREEN("GREEN", R.drawable.ic_green_magnify),
    YELLOW("YELLOW", R.drawable.ic_yellow_magnify),
    BLUE("BLUE", R.drawable.ic_blue_magnify);

    private String linkColor;
    private int resIdDrawable;
    ColorLink(String linkColor, int resIdDrawable) {
        this.linkColor = linkColor;
        this.resIdDrawable = resIdDrawable;
    }

    @Override
    public String toString() {
        return linkColor;
    }

    public int getResIdDrawable(){
        return resIdDrawable;
    }
}

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    @BindView(R.id.root_view) protected FrameLayout root_view;
    @BindView(R.id.canvas_view) protected CanvasView canvasView;

    private String TAG = "MainActivity";
    private PreferencesHelper preferencesHelper;
    ArrayList<Float> listCoordinateX = new ArrayList<>();
    ArrayList<Float> listCoordinateY = new ArrayList<>();
    private MulticastGroup multicastGroup;
    private String tag_multicast = "second_screen";
    private String ip_multicast = "230.192.0.10";
    private int port_multicast = 1027;

    private static int HEIGHT_BASE = 2048;
    private static int WIDTH_BASE = 1536;
    private int heightDevice;
    private int widthDevice;
    private float currentCoordinateX;
    private float currentCoordinateY;
    private int sizeImage;
    private boolean shouldDraw = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        preferencesHelper = new PreferencesHelper(this);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        heightDevice = metrics.heightPixels;
        widthDevice = metrics.widthPixels;
        sizeImage = (widthDevice > 1079) ? 120 : 60;
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
        currentCoordinateX = (widthDevice*listCoordinateX.get(0))/WIDTH_BASE;
        currentCoordinateY = (heightDevice*listCoordinateY.get(0))/HEIGHT_BASE;
        canvasView.shootEventTouch(MotionEvent.ACTION_DOWN,  currentCoordinateX - 1, currentCoordinateY - 1);
        CountDownTimer timer = new CountDownTimer(size * 50, 50) {
            private int i = 0;
            @Override
            public void onTick(long millisUntilFinished) {
                currentCoordinateX = (widthDevice*listCoordinateX.get(i))/WIDTH_BASE;
                currentCoordinateY = (heightDevice*listCoordinateY.get(i))/HEIGHT_BASE;
                if (i % 50 == 0) {
                    preferencesHelper.putPointX(currentCoordinateX);
                    preferencesHelper.putPointY(currentCoordinateY);
                    putImageOnView("Android custom dialog example!", "Lorem ipsum blandit est netus ultrices lacus vulputate pulvinar, arcu nulla tempus nulla quisque convallis lobortis, et cubilia dui accumsan varius sollicitudin at. tortor arcu tempor at in libero urna aliquam laoreet taciti quisque tempus, praesent ligula ante molestie auctor curabitur vehicula ultricies consectetur vivamus egestas, placerat ut massa dictum potenti semper ac magna odio conubia. libero inceptos netus justo litora fusce lectus ante, per eu placerat orci luctus gravida, quisque conubia quam eu vulputate tincidunt. per mauris nisl tristique id habitant ultricies, fames curae lacinia massa dictum ad, vitae cursus enim vel magna. Turpis aliquam massa ad porta enim fusce, aliquet eros eget commodo nam integer eu, vehicula feugiat tortor elit consectetur. diam nisl feugiat himenaeos erat conubia metus suspendisse fames consequat sodales quisque habitasse, inceptos nisl aptent proin facilisis iaculis eget aliquet nostra habitant sociosqu. aptent ut nostra morbi consectetur conubia donec duis cursus libero, habitasse curae sed tempus lectus porttitor sit facilisis, donec conubia praesent lacinia augue himenaeos pharetra malesuada. habitant himenaeos imperdiet gravida sociosqu felis lacinia eget consectetur congue, dolor nostra consequat ac mi et ante lacinia. ut lectus lobortis nisi hac iaculis interdum donec senectus, phasellus sociosqu himenaeos iaculis a tempor sollicitudin, nibh lobortis ac justo ut in non. " , R.mipmap.ic_launcher);
                }
                if (shouldDraw) {
                    moveHat();
                    canvasView.shootEventTouch(MotionEvent.ACTION_MOVE, currentCoordinateX, currentCoordinateY);
                    i++;
                }
            }
            @Override
            public void onFinish() {
                canvasView.shootEventTouch(MotionEvent.ACTION_DOWN, currentCoordinateX + 1, currentCoordinateY + 1);
                shouldDraw = false;
//                Log.e(TAG, "x: " + preferencesHelper.getListPoints(PreferencesHelper.KEY_LIST_X));
//                Log.e(TAG, "y: " + preferencesHelper.getListPoints(PreferencesHelper.KEY_LIST_Y));
            }
        };
        timer.start();
    }

    public void stopDraw() {
        shouldDraw = false;
    }

    public void moveHat() {
        ImageView image_hat = (ImageView) findViewById(R.id.image_hat);
        image_hat.setMinimumWidth(sizeImage);
        image_hat.setMinimumHeight(sizeImage);
        if (image_hat.getVisibility() == View.INVISIBLE)
            image_hat.setVisibility(View.VISIBLE);
        image_hat.setX(currentCoordinateX - (image_hat.getWidth() / 2));
        image_hat.setY(currentCoordinateY - (image_hat.getHeight() / 2));
    }

    public void putImageOnView(final String title, final String text, final int imageResId) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(120, 120);
        layoutParams.gravity = TOP | START;
        layoutParams.leftMargin = (int) currentCoordinateX - (layoutParams.width / 2);
        layoutParams.topMargin = (int) currentCoordinateY - (layoutParams.height / 2);
        ImageButton imageButton = new ImageButton(this);
        imageButton.setImageDrawable(getDrawable(ColorLink.valueOf("RED").getResIdDrawable()));
        imageButton.setLayoutParams(layoutParams);
        imageButton.setBackgroundColor(Color.TRANSPARENT);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayMetrics metrics = MainActivity.this.getResources().getDisplayMetrics();
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_dialog);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setGravity(Gravity.BOTTOM);
                dialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, height/3);
                TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_title);
                tvTitle.setTextSize((widthDevice > 1079) ? 24 : 16);
                tvTitle.setText(title);
                TextView tvText = (TextView) dialog.findViewById(R.id.tv_text);
                tvText.setText(text);
                ImageView image = (ImageView) dialog.findViewById(R.id.iv_image);
                image.setImageResource(imageResId);
                ImageButton ibClose = (ImageButton) dialog.findViewById(R.id.ib_close);
                ibClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        showImageInTop();
        root_view.addView(imageButton);

    }

    public void showImageInTop() {
        final ImageView imageView = (ImageView) findViewById(R.id.image_view);
        imageView.setImageDrawable(getDrawable(R.mipmap.ic_launcher));
        imageView.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(View.INVISIBLE);
            }
        }, 5000);
    }

}
