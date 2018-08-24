package com.example.thomas.explorador_segunda_tela.network;

import android.util.Log;

import com.example.thomas.explorador_segunda_tela.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MulticastGroup extends MulticastManager {

    private final String TAG = this.getClass().getSimpleName();
    private MainActivity mActivity;

    private static final String START = "START";
    private static final String STOP = "STOP";
    private static final String RESUME = "RESUME";
    private static final String PAUSE = "PAUSE";

    public MulticastGroup(MainActivity mainActivity, String tag, String multicastIp, int multicastPort) {
        super(mainActivity, tag, multicastIp, multicastPort);
        this.mActivity = mainActivity;
    }

    @Override
    protected Runnable getIncomingMessageAnalyseRunnable() {

        String tag = incomingMessage.getTag();
        String message = incomingMessage.getMessage();

        try {
            JSONObject jsonObject = new JSONObject(URLDecoder.decode(message, "UTF-8"));
            Log.e("second", tag + " > " + jsonObject.toString());
            executeAction(jsonObject);
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void executeAction(JSONObject json) throws JSONException {
        if (json.has(START)) {
            String action = json.getString(START);
            if (action.equals("drawing")) {
                mActivity.startDrawing(json.getInt("duration"));
            } else if (action.equals("RED") || action.equals("GREEN") || action.equals("YELLOW") || action.equals("BLUE")) {
                mActivity.startLink(action, json.getInt("id"));
            }
        } else if (json.has(RESUME)) {
            String action = json.getString(RESUME);
            switch (action) {
                case "drawing":

                    break;
                default: break;
            }
        } else if (json.has(STOP)) {
            String action = json.getString(STOP);
            switch (action) {
                case "drawing":
                    mActivity.stopDraw();
                    break;
                default: break;
            }
        } else if (json.has(PAUSE)) {
            String action = json.getString(PAUSE);
            switch (action) {
                case "drawing":

                    break;
                default: break;
            }
        }
    }

}
