package com.ltbrew.brewbeer.uis.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ltbrew.brewbeer.BrewApp;

/**
 * Created by 151117a on 2016/1/25.
 */
public class NetworkConnectionUtil {
    public static boolean isNetworkAvailable(){
        ConnectivityManager connectMgr = (ConnectivityManager) BrewApp.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {// unconnect network
            return false;
        }else{
            return true;
        }
    }
}
