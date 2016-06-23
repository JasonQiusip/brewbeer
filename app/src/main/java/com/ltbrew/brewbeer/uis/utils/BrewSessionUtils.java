package com.ltbrew.brewbeer.uis.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.ltbrew.brewbeer.BrewApp;

/**
 * Created by 151117a on 2016/6/7.
 */
public class BrewSessionUtils {

    public static final String START_TIME_STAMP = "boilStart";
    public static final String BREW_SESSION_SP = "brew_session_sp";
    public static final String FERMENTING_TS = "fermenting_ts";

    public static void storeStepStartTimeStamp(long timeStamp){
        getSharedPreferences().edit().putLong(START_TIME_STAMP, timeStamp).commit();
    }

    public static long getStepStartTimeStamp(){
        return getSharedPreferences().getLong(START_TIME_STAMP, 0);
    }

    public static void storeFermentingStartTimeStamp(Long package_id, long timeStamp){
        getSharedPreferences().edit().putLong(package_id+FERMENTING_TS, timeStamp).commit();
    }

    public static long getFermentingStartTimeStamp(Long package_id){
        return getSharedPreferences().getLong(package_id+FERMENTING_TS, 0);
    }

    private static SharedPreferences getSharedPreferences(){
        return BrewApp.getInstance().getSharedPreferences(BREW_SESSION_SP, Context.MODE_PRIVATE);
    }
}
