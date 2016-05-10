package com.ltbrew.brewbeer.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.api.ConfigApi;
import com.ltbrew.brewbeer.api.longconnection.TransmitCmdService;
import com.ltbrew.brewbeer.api.longconnection.interfaces.FileSocketReadyCallback;
import com.ltbrew.brewbeer.api.longconnection.process.CmdsConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by qiusiping on 16/5/8.
 */
public class LtPushService extends IntentService implements FileSocketReadyCallback {
    private static final String TAG = "LtPushService";
    public static final String CMN_PRGS_PUSH_ACTION = "CMN_PRGS_PUSH";
    public static final String PUSH_MSG_EXTRA = "pushMsg";
    public static final String CMD_RPT_ACTION = "cmd_rpt_action";
    public static final String UNBIND_ACTION = "unbind_action";
    public static final String CMD_SOCKET_IS_READY_ACTION = "cmdSocketIsReadyAction";
    private int tryAgain = 3;
    private TransmitCmdService transmitCmdService;
    private final static int GRAY_SERVICE_ID = 1001;
    ServiceBinder serviceBinder = new ServiceBinder();
    private int state;
    private int starting = 0;
    private int started = 1;

    public LtPushService() {
        super("LtPushService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    public class ServiceBinder extends Binder {
        public LtPushService getService() {
            return LtPushService.this;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "======onStartCommand=====");
        startConnectionOnNewThread();
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(GRAY_SERVICE_ID, new Notification());//API < 18 ，此方法能有效隐藏Notification上的图标
        } else {
            Intent innerIntent = new Intent(this, GrayInnerService.class);
            startService(innerIntent);
            startForeground(GRAY_SERVICE_ID, new Notification());
        }

        return super.onStartCommand(intent, flags, startId);
    }
    /**
     * 给 API >= 18 的平台上用的灰色保活手段
     */
    public static class GrayInnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    private void startConnectionOnNewThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                transmitCmdService = ConfigApi.startLongConnection(LtPushService.this);
                state = starting;

            }
        }).start();
    }


    public void sendBrewSessionCmd(Long package_id) {
        if(transmitCmdService != null)
            transmitCmdService.sendBrewSessionCmd(package_id);
    }

    public void sendCmnPrgsCmd(String token){
        if(transmitCmdService != null)
            transmitCmdService.sendCmnPrgsCmd(token);
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
                    transmitCmdService = ConfigApi.startLongConnection(LtPushService.this);
                    state = starting;
                }
            }
        }).start();

    }

    @Override
    public void onCmdSocketReady() {
        tryAgain = 3;
        if(state == starting) {
            Intent intent = new Intent();
            intent.setAction(CMD_SOCKET_IS_READY_ACTION);
            sendBroadcast(intent);
        }
        state = started;

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
        Log.e(TAG, ""+pushLists);
        int size = pushLists.size();
        String pushMsg = pushLists.get(0);
        PushMsg pushMessageObj = parsePushMsg(pushMsg);
        String cb = pushMessageObj.cb;
        if(cb.contains(":")){
            String[] splittedStr = cb.split(":");
            cb = splittedStr[1];
        }
        Object obj = CmdsConstant.CMDSTR.lookup.get(cb);
        if(obj == null) {
            System.out.println("未识别的指令");
            return;
        }
        Intent intent = new Intent();
        switch (PushCommand.valueOf(cb)){
            case bind:
                break;
            case unbind:
                intent.setAction(UNBIND_ACTION);
                sendBroadcast(intent);
                break;
            case cmd_report:
                intent.setAction(CMD_RPT_ACTION);
                sendBroadcast(intent);
                break;
            case cmn_prgs:
                intent.setAction(CMN_PRGS_PUSH_ACTION);
                intent.putExtra(PUSH_MSG_EXTRA, pushMessageObj);
                sendBroadcast(intent);
                break;
            case brew_session:

                break;
        }

    }

    private PushMsg parsePushMsg(String pushMsg) {
        PushMsg pushMessage = new PushMsg();
        JSONObject jsonObject = JSON.parseObject(pushMsg);
        String cb = jsonObject.getString("cb");
        String description = jsonObject.getString("description");
        String id = jsonObject.getString("id");
        Integer f = jsonObject.getInteger("f");
        pushMessage.cb = cb;
        pushMessage.des = description;
        pushMessage.id = id;
        pushMessage.f = f;
        JSONObject pld = jsonObject.getJSONObject("_pld");
        if(pld != null || !pld.equals("null")) {
            String body = pld.getString("body");
            Integer si = pld.getInteger("si");
            Integer ratio = pld.getInteger("ratio");
            String st = pld.getString("st");

            pushMessage.body = body;
            pushMessage.si = si;
            pushMessage.ratio = ratio;
            pushMessage.st = st;
        }
        return pushMessage;
    }

    @Override
    public void onGetPidHeartHistory(String endindex, HashMap<String, ArrayList<Integer>> maps) {

    }

    @Override
    public void onGetCmdPrgs(String percent, String seq_index, String body) {
        PushMsg pushMessage = new PushMsg();
        pushMessage.ratio = Integer.valueOf(percent);
        pushMessage.si = Integer.valueOf(seq_index);
        pushMessage.body = body;
        Intent intent = new Intent();
        intent.setAction(CMN_PRGS_PUSH_ACTION);
        intent.putExtra(PUSH_MSG_EXTRA, pushMessage);
        sendBroadcast(intent);
    }

    @Override
    public void onServerRespError() {

    }
}
