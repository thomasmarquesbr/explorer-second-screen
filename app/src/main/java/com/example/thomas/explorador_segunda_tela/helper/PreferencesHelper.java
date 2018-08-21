package com.example.thomas.explorador_segunda_tela.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PreferencesHelper {

    private static String TAG = "PreferencesHelper";
    private static final String PREF_FILE_NAME = "app_pref_file";
    public static final String KEY_LIST_X = "key_list_x";
    public static final String KEY_LIST_Y = "key_list_y";
    private static SharedPreferences mPref;

    public PreferencesHelper(Context context){
        mPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void clear() {
        mPref.edit().clear().apply();
    }

    public void saveListPoints(ArrayList<String> list, String key){
        Gson gson = new Gson();
        String json = gson.toJson(list);
        mPref.edit().putString(key, json).apply();
    }

    public ArrayList<String> getListPoints(String key){
        Gson gson = new Gson();
        String json = mPref.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void putPointX(float x) {
        ArrayList<String> listPointsX = getListPoints(KEY_LIST_X);
        if (listPointsX == null)
            listPointsX = new ArrayList<>();
        listPointsX.add(String.valueOf(x));
        saveListPoints(listPointsX, KEY_LIST_X);
    }

    public void putPointY(float y) {
        ArrayList<String> listPointsX = getListPoints(KEY_LIST_Y);
        if (listPointsX == null)
            listPointsX = new ArrayList<>();
        listPointsX.add(String.valueOf(y));
        saveListPoints(listPointsX, KEY_LIST_Y);
    }

}
