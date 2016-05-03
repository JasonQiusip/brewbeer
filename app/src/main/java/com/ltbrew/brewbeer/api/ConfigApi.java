package com.ltbrew.brewbeer.api;

import android.content.Context;

import com.ltbrew.brewbeer.api.common.TokenDispatcher;

/**
 * Created by 151117a on 2016/5/2.
 */
public class ConfigApi {

    public static String api_key;
    public static String api_secret;

    public static void init(Context context){
        TokenDispatcher.getInstance().init(context);
    }
}
