package com.example.kdar.rollcall2.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PreferenceHelper {
    private SharedPreferences sharedPreferences;
    private Context context;

    public PreferenceHelper(Context context, String name) {
        if (context == null) {
            return;
        }

        sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        this.context = context;
    }

    public String getClassList() {
        if (sharedPreferences == null) return "";

        return sharedPreferences.getString(GlobalHelper.classList, "");
    }

    public void setClassList(String newValue) {
        if (sharedPreferences == null) return;

        sharedPreferences.edit().putString(GlobalHelper.classList, newValue).apply();
    }

    public int getClassPosition() {
        if (sharedPreferences == null) return -1;

        return sharedPreferences.getInt(GlobalHelper.classPosition, -1);
    }

    public void setClassPosition(int newValue) {
        if (sharedPreferences == null) return;

        sharedPreferences.edit().putInt(GlobalHelper.classPosition, newValue).apply();
    }

    public String getDateRoll() {
        if (sharedPreferences == null) return "";
        return sharedPreferences.getString(GlobalHelper.dateRoll, "");
    }

    public void setDateRoll(String newValue) {
        if (sharedPreferences == null) return;

        sharedPreferences.edit().putString(GlobalHelper.dateRoll, newValue).apply();
    }

    public String getHistoryRoll() {
        if (sharedPreferences == null) return "";
        return sharedPreferences.getString(GlobalHelper.historyRoll, "");
    }

    public void setHistoryRoll(String newValue) {
        if (sharedPreferences == null) return;

        sharedPreferences.edit().putString(GlobalHelper.historyRoll, newValue).apply();
    }
}
