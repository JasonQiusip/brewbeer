package com.ltbrew.brewbeer.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ltbrew.brewbeer.api.ConfigApi;
import com.ltbrew.brewbeer.api.longconnection.interfaces.FileSocketReadyCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by qiusiping on 16/5/8.
 */
public class LtPushService extends IntentService implements FileSocketReadyCallback {
    private int tryAgain = 3;
    private String tag = "ltpushservice";

    public LtPushService() {
        super("LtPushService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(tag, "======onStartCommand=====");
        startConnectionOnNewThread();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    private void startConnectionOnNewThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConfigApi.startLongConnection(LtPushService.this);
            }
        }).start();
    }

    @Override
    public void onGetOauthTokenFailed() {

    }

    @Override
    public void onOAuthFailed() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(tryAgain > 0){
                    tryAgain--;
                    ConfigApi.startLongConnection(LtPushService.this);
                }
            }
        }).start();

    }

    @Override
    public void onCmdSocketReady() {
        tryAgain = 3;
    }

    @Override
    public void onFileSocketReady() {

    }

    @Override
    public void onInitializeLongConnFailed() {

    }

    @Override
    public void onInitFileSocketFailed() {

    }

    @Override
    public void onFileSocketReconnect() {

    }

    @Override
    public void onCmdSocketReconnect() {

    }

    @Override
    public void onMaximumFileLength(int length) {

    }

    @Override
    public void onFileBegined() {

    }

    @Override
    public void onFileUploadSuccess() {

    }

    @Override
    public void onFileUploadFailed() {

    }

    @Override
    public void onFileUploadEnd() {

    }

    @Override
    public void onGetPidHeart(ArrayList<Integer> result, String r_hrr_endtime, String linkedIndex, String endindex) {

    }

    @Override
    public void onCmdHasPush(List<String> pushLists, String pok, long pushTime) {

    }

    @Override
    public void onGetPidHeartHistory(String endindex, HashMap<String, ArrayList<Integer>> maps) {

    }
}
