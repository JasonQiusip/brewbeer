package com.ltbrew.brewbeer.presenter.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.ltbrew.brewbeer.BrewApp;

/**
 * Created by 151117a on 2016/5/5.
 */
public class DeviceUtil {

    public static final String CUR_DEV_ID = "curDevId";
    public static final String DEVICE_SP = "DEVICE_SP";

    public static void storeCurrentDevId(String devId){
        getSharedPreferences().edit().putString(CUR_DEV_ID, devId).commit();
    }

    public static String getCurrentDevId(){
        return getSharedPreferences().getString(CUR_DEV_ID, "");
    }


    public static void setDevNickName(String devId, String nickName){
        getSharedPreferences().edit().putString(devId, nickName).commit();

    }

    public static String getDevNickName(String devId){
        return getSharedPreferences().getString(devId, "");
    }

    public static void clearData(){
        storeCurrentDevId("");
    }

    private static SharedPreferences getSharedPreferences(){
        return BrewApp.getInstance().getSharedPreferences(DEVICE_SP, Context.MODE_PRIVATE);
    }

}
