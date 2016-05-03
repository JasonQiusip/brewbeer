package com.ltbrew.brewbeer.api.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

/**
 * Created by qiusiping on 16/4/1.
 */
public class SharePrefUtil {

    private static final String KINZOL_SDK = "kinzol_sdk";
    private static SharePrefUtil sharePrefUtil;
    private static SharedPreferences sharedPreferences;
    private SharePrefUtil(){}

    public static SharePrefUtil getInstance(Context context){
        if(sharePrefUtil == null){
            sharePrefUtil = new SharePrefUtil();
        }
        if(sharedPreferences == null){
            sharedPreferences = context.getSharedPreferences(KINZOL_SDK, Context.MODE_PRIVATE);
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
