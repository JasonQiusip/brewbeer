package com.ltbrew.brewbeer.api.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.ltbrew.brewbeer.BrewApp;

/**
 * Created by qiusiping on 16/4/1.
 */
public class SharePrefUtil {

    private static final String BREWBEER = "BrewBeer";
    private static SharePrefUtil sharePrefUtil;
    private static SharedPreferences sharedPreferences;
    private SharePrefUtil(){}

    public static SharePrefUtil getInstance(Context context){
        if(sharePrefUtil == null){
            sharePrefUtil = new SharePrefUtil();
        }
        if(sharedPreferences == null){
            sharedPreferences = context.getSharedPreferences(BREWBEER, Context.MODE_PRIVATE);
        }
        return sharePrefUtil;
    }

    public void storeAcc(String acc){
        sharedPreferences.edit().putString("acc", acc).commit();
    }

    public String getAcc(){
        return sharedPreferences.getString("acc", "");
    }

    public void storePwd(String pwd){
        byte[] encodePwd = Base64.encode(pwd.getBytes(), 0);
        sharedPreferences.edit().putString("pwd", new String(encodePwd)).commit();
    }

    public String getPwd(){

        String pwd = sharedPreferences.getString("pwd", "");
        return new String(Base64.decode(pwd, 0));
    }

    public void storeToken(String token){
        sharedPreferences.edit().putString("tk", token).commit();
    }

    public String getToken(){
        return sharedPreferences.getString("tk", "");
    }

}
