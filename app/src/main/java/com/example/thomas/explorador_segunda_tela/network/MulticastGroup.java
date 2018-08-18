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
            Log.e("second", tag + " > " + message);
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
