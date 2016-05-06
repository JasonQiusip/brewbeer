package com.ltbrew.brewbeer.uis.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.ltbrew.brewbeer.BrewApp;

/**
 * Created by DELL on 2016/5/6.
 */
public class AccUtils {
    public static final String CUR_ACC = "curAcc";
    public static final String ACC_SP = "acc_sp";
    public static final String PWD = "pwd";

    public static void storeAcc(String devId){
        getSharedPreferences().edit().putString(CUR_ACC, devId).commit();
    }

    public static String getAcc(){
        return getSharedPreferences().getString(CUR_ACC, "");
    }

    public static void storeCurPwd(String pwd){
        getSharedPreferences().edit().putString(PWD, pwd).commit();
    }

    public static String getCurPwd(){
        return getSharedPreferences().getString(PWD, "");
    }

    private static SharedPreferences getSharedPreferences(){
        return BrewApp.getInstance().getSharedPreferences(ACC_SP, Context.MODE_PRIVATE);
    }

    public static boolean isAccEmpty(){
        return TextUtils.isEmpty(getAcc());
    }

    public static boolean isPwdEmpty(){
        return TextUtils.isEmpty(getCurPwd());
    }
}
