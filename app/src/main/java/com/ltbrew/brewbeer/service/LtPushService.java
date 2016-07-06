package com.ltbrew.brewbeer.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.api.ConfigApi;
import com.ltbrew.brewbeer.api.longconnection.SOCKETSTATE;
import com.ltbrew.brewbeer.api.longconnection.TransmitCmdService;
import com.ltbrew.brewbeer.api.longconnection.interfaces.FileSocketReadyCallback;
import com.ltbrew.brewbeer.api.longconnection.process.ParsePackKits;
import com.ltbrew.brewbeer.presenter.util.DeviceUtil;
import com.ltbrew.brewbeer.uis.activity.BrewHomeActivity;
import com.ltbrew.brewbeer.uis.utils.BrewSessionUtils;
import com.ltbrew.brewbeer.uis.utils.NetworkConnectionUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by qiusiping on 16/5/8.
 */
public class LtPushService extends Service implements FileSocketReadyCallback {
    private static final String TAG = "LtPushService";
    public static final String CMN_PRGS_PUSH_ACTION = "CMN_PRGS_PUSH";
    public static final String CMN_MSG_PUSH_ACTION = "CMN_MSG_PUSH";
    public static final String CMN_PRGS_CHECK_ACTION = "CMN_PRGS_CHECK_ACTION";
    public static final String BREW_SESSION_PUSH_ACTION = "BREW_SESSION_PUSH_ACTION";
    public static final String PUSH_MSG_EXTRA = "pushMsg";
    public static final String CMD_RPT_ACTION = "cmd_rpt_action";
    public static final String UNBIND_ACTION = "unbind_action";
    public static final String FILE_SOCKET_IS_READY_ACTION = "fileSocketIsReadyAction";
    public static final String PUSH_PLD_EXTRA = "pld";
    public static final String REQUEST_BREW_SESSION_FAILED = "reqBrewSessionFailed";
    public static final String ANDROID_NET_CONN_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String ANDROID_INTENT_ACTION_USER_PRESENT = "android.intent.action.USER_PRESENT";
    private static final int SYSTEM_ALERT = 20;
    public static final String SOCKET_IS_KICKED_OUT = "socket_is_kicked";
    public static final String SOCKET_INIT_FAILED = "socket_init_failed";
    private int tryAgain = 3;
    private TransmitCmdService transmitCmdService;
    private final static int GRAY_SERVICE_ID = 2016;
//    ServiceBinder serviceBinder = new ServiceBinder();
    private int state;
    private int starting = 0;
    private int started = 1;
    private Thread startLongConnectionThread;
    private Thread reconnectThread;
    private NotificationCompat.Builder notificationBuilder;
    private Handler handler = new Handler();

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(ANDROID_NET_CONN_CONNECTIVITY_CHANGE.equals(action)){
                if (NetworkConnectionUtil.isNetworkAvailable()) {// unconn
                    if(transmitCmdService != null && transmitCmdService.isSocketClosed() != SOCKETSTATE.socket_alive){
                        transmitCmdService.closeSocket();
                        startLongConnectionThread = null;
                    }
                    startConnectionOnNewThread();
                }else{
                    startLongConnectionThread = null;
                }

            }else if(ANDROID_INTENT_ACTION_USER_PRESENT.equals(action)){
                if(transmitCmdService != null && transmitCmdService.isSocketClosed() != SOCKETSTATE.socket_alive){
                    transmitCmdService.closeSocket();
                    startLongConnectionThread = null;
                    startConnectionOnNewThread();
                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    ILtPushServiceAidlInterface.Stub serviceBinder = new ILtPushServiceAidlInterface.Stub(){

        @Override
        public void sendBrewSessionCmd(long pack_id) throws RemoteException {
            sendBrewSession(pack_id);
        }

        @Override
        public void startLongConn() throws RemoteException {
            startConnectionOnNewThread();
        }

        @Override
        public void sendCmdToCheckTemp(long pack_id) throws RemoteException {
            Log.e(TAG, "======sendCmdToCheckTemp=====");

            checkCmnMSgLast(DeviceUtil.getCurrentDevId(), "pack:"+pack_id);
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "======onStartCommand=====");
        startConnectionOnNewThread();
        Intent brewHomeIntent = new Intent(this, BrewHomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, brewHomeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder = new NotificationCompat.Builder(this).setContentTitle("精酿大师")
                .setContentText("服务正在运行").setTicker("").setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis()).setContentIntent(pendingIntent);

        startForeground(GRAY_SERVICE_ID, notificationBuilder.build());//API < 18 ，此方法能有效隐藏Notification上的图标

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ANDROID_NET_CONN_CONNECTIVITY_CHANGE);
        intentFilter.addAction(ANDROID_INTENT_ACTION_USER_PRESENT);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("", "===onDestroy====");
        startLongConnectionThread = null;
        transmitCmdService.closeSocket();
        unregisterReceiver(broadcastReceiver);
    }

    private void startConnectionOnNewThread() {
        Log.e("", "===startConnectionOnNewThread===="+startLongConnectionThread);
        if(transmitCmdService != null && transmitCmdService.isSocketClosed() != SOCKETSTATE.socket_alive){
            transmitCmdService.closeSocket();
            startLongConnectionThread = null;
        }

        if(startLongConnectionThread != null)
            return;
        startLongConnectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                transmitCmdService = ConfigApi.startLongConnection(LtPushService.this);
                state = starting;

            }
        });
        startLongConnectionThread.start();
    }


    public void sendBrewSession(Long package_id) {
        Log.e("", "===###################sendBrewSessionCmd####################====");

        if (transmitCmdService != null)
            transmitCmdService.sendBrewSessionCmd(package_id);
    }

    public void checkCmnMSgLast(String pid, String token){
        if (transmitCmdService != null)
            transmitCmdService.checkCmnMSgLast(pid, token);
    }

    @Override
    public void onGetOauthTokenFailed() {

    }

    @Override
    public void onOAuthFailed() {
        Log.e("", "======================onOAuthFailed=========================");

        reconnectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (tryAgain > 0) {
                    tryAgain--;
                    transmitCmdService = ConfigApi.startLongConnection(LtPushService.this);
                    state = starting;
                }
            }
        });
        reconnectThread.start();

    }

    @Override
    public void onCmdSocketReady() {
        Log.e("", "======================onCmdSocketReady=========================");
        tryAgain = 3;

    }

    int trySendBrewSession = 3;

    @Override
    public void onFileSocketReady() {
        Log.e("", "======================onFileSocketReady=========================");
        if (state == starting && trySendBrewSession > 0) {
            Intent intent = new Intent();
            intent.setAction(FILE_SOCKET_IS_READY_ACTION);
            sendBroadcast(intent);
            trySendBrewSession--;
        }
        state = started;
    }

    @Override
    public void onInitializeLongConnFailed() {
        Log.e("", "======================onInitializeLongConnFailed=========================");
        Intent intent = new Intent(this, BrewHomeActivity.class);
        showNotification(this, "连接服务初始化失败，请点击重连", intent);
        sendBroadcast(new Intent(SOCKET_INIT_FAILED));

        startLongConnectionThread = null;
        if(transmitCmdService != null)
            transmitCmdService.closeSocket();

    }

    @Override
    public void onInitFileSocketFailed() {
        Log.e("", "======================onInitFileSocketFailed=========================");

    }

    @Override
    public void onFileSocketReconnect() {
        Log.e("", "======================onFileSocketReconnect=========================");
        state = starting;

    }

    @Override
    public void onCmdSocketReconnect() {
        Log.e("", "======================onCmdSocketReconnect=========================");

    }

    @Override
    public void onMaximumFileLength(int length) {
        Log.e("", "======================onMaximumFileLength=========================");

    }

    @Override
    public void onFileBegined() {
        Log.e("", "======================onFileBegined=========================");

    }

    @Override
    public void onFileUploadSuccess() {
        Log.e("", "======================onFileUploadSuccess=========================");

    }

    @Override
    public void onFileUploadFailed() {
        Log.e("", "======================onFileUploadFailed=========================");

    }

    @Override
    public void onFileUploadEnd() {
        Log.e("", "======================onFileUploadEnd=========================");

    }

    @Override
    public void onGetPidHeart(ArrayList<Integer> result, String r_hrr_endtime, String linkedIndex, String endindex) {

    }

    @Override
    public void onCmdHasPush(List<String> pushLists, String pok, long pushTime) {
        Log.e(TAG, "" + pushLists);
        int size = pushLists.size();
        String pushMsg = pushLists.get(0);
        JSONObject jsonObject = JSON.parseObject(pushMsg);
        PushMsg pushMessage = parsePushMsg(jsonObject);
        String cb = pushMessage.cb;
        if (cb.contains(":")) {
            String[] splittedStr = cb.split(":");
            cb = splittedStr[1];
        }
        System.out.println("CMD:  "+cb);
        Object obj = PushCommand.lookup.get(cb);
        if (obj == null) {
            System.out.println("未识别的指令");
            return;
        }
        if(isBackground(this) && "cmn_msg".equals(cb)){
            Intent intent = new Intent(this, BrewHomeActivity.class);
            showNotification(this, pushMessage.des, intent);
        }
        JSONObject pld = jsonObject.getJSONObject("_pld");


        Intent intent = new Intent();
        switch (PushCommand.valueOf(cb)) {
            case follow:
                intent.setAction(BrewHomeActivity.ADD_DEV_SUCCESS_ACTION);
                sendBroadcast(intent);
                break;
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


                if (pld != null || !pld.equals("null")) {
                    String body = pld.getString("body");
                    String des = pld.getString("des");
                    Integer si = pld.getInteger("si");
                    Integer ratio = pld.getInteger("ratio");
                    String st = pld.getString("st");
                    PldForCmnPrgs pldForCmnPrgs = new PldForCmnPrgs();
                    pldForCmnPrgs.body = body;
                    pldForCmnPrgs.si = si;
                    pldForCmnPrgs.ratio = ratio;
                    pldForCmnPrgs.st = st;
                    pushMessage.des = getStartTSOfSteps(st, pushMessage.des);

                    Log.e("pushmsg -> cmn_prgs", pldForCmnPrgs.toString());
                    intent.setAction(CMN_PRGS_PUSH_ACTION);
                    intent.putExtra(PUSH_MSG_EXTRA, pushMessage);
                    intent.putExtra(PUSH_PLD_EXTRA, pldForCmnPrgs);
                    sendBroadcast(intent);
                }
                break;
            case cmn_msg:
                if(pld != null || !pld.equals("null")){
                    PldForCmnMsg pldForCmnMsg = new PldForCmnMsg();
                    int ms = pld.getInteger("ms");
                    String tk = pld.getString("tk");
                    pldForCmnMsg.ms = ms;
                    pldForCmnMsg.tk = tk;
                    Log.e("pushmsg -> cmn_prgs", pldForCmnMsg.toString());

                    intent.setAction(CMN_MSG_PUSH_ACTION);
                    intent.putExtra(PUSH_MSG_EXTRA, pushMessage);
                    intent.putExtra(PUSH_PLD_EXTRA, pldForCmnMsg);
                    sendBroadcast(intent);
                }

                break;
            case brew_session:
                if(pld != null || !pld.equals("null")){
                    PldForBrewSession pldForBrewSession = new PldForBrewSession();
                    String pack_id = pld.getString("pack_id");
                    String formula_id = pld.getString("formula_id");
                    int state = pld.getInteger("state");
                    pldForBrewSession.state = state;
                    intent.setAction(BREW_SESSION_PUSH_ACTION);
                    intent.putExtra(PUSH_MSG_EXTRA, pushMessage);
                    intent.putExtra(PUSH_PLD_EXTRA, pldForBrewSession);
                    sendBroadcast(intent);
                }
                break;
        }

    }

    private String getStartTSOfSteps(String st, String des) {
        if(st == null)
            return des;
        String[] sts = st.split(":");
        if(sts.length < 2)
            return des;
        String pack_id = sts[1];
        String timeStamp = null;
        if (des.contains("糖化中 ")) {
            //获取糖化开始时间
            timeStamp = des.replace("糖化中 ", "");
            des = "糖化中";

        }

        if (des.contains("煮沸中 ")){
            timeStamp = des.replace("煮沸中 ", "");
            des = "煮沸中";
        }

        if(timeStamp != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date date = simpleDateFormat.parse(timeStamp);
                BrewSessionUtils.storeStepStartTimeStamp(pack_id, date.getTime());

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return des;
    }

    private PushMsg parsePushMsg(JSONObject jsonObject) {
        PushMsg pushMessage = new PushMsg();
        String cb = jsonObject.getString("cb");
        String description = jsonObject.getString("description");
        String id = jsonObject.getString("id");
        Integer f = jsonObject.getInteger("f");

        pushMessage.cb = cb;
        pushMessage.des = description;
        pushMessage.id = id;
        pushMessage.f = f;
        return pushMessage;
    }

    @Override
    public void onGetPidHeartHistory(String endindex, HashMap<String, ArrayList<Integer>> maps) {

    }

    String tk;

    @Override
    public void onGeBrewSessionResp(String tk, String state) {
        this.tk = tk;
        Log.e(TAG, tk);
    }

    @Override
    public void onWritingHb() {

    }

    @Override
    public void onGetCmnMsg(String des) {
        Log.e(TAG,  "onGetCmnMsg "+des);
        JSONObject jsonObject = JSON.parseObject(des);
        PushMsg pushMessage = parsePushMsg(jsonObject);
        JSONObject pld = jsonObject.getJSONObject("_pld");
        if(pld != null || !pld.equals("null")){
            PldForCmnMsg pldForCmnMsg = new PldForCmnMsg();
            int ms = pld.getInteger("ms");
            String tk = pld.getString("tk");
            pldForCmnMsg.ms = ms;
            pldForCmnMsg.tk = tk;
            Log.e("pushmsg -> cmn_prgs", pldForCmnMsg.toString());
            Intent intent = new Intent();
            intent.setAction(CMN_MSG_PUSH_ACTION);
            intent.putExtra(PUSH_MSG_EXTRA, pushMessage);
            intent.putExtra(PUSH_PLD_EXTRA, pldForCmnMsg);
            sendBroadcast(intent);
        }

    }

    @Override
    public void onGetCmdPrgs(String percent, String seq_index, String body) {
        PushMsg pushMessage = new PushMsg();
        pushMessage.ratio = Integer.valueOf(percent);
        pushMessage.si = Integer.valueOf(seq_index);
        pushMessage.st = tk;
        byte[] bytes = ParsePackKits.charToByteArray(body.toCharArray()); //utf8中文串通过转字符串再转String会出现乱码， 所以在次将String转会byte数组， 再用系统的方法转为中文
        pushMessage.des = new String(bytes);
        Log.e(TAG,  "onGetCmdPrgs "+pushMessage.des);

        pushMessage.des = getStartTSOfSteps(pushMessage.st, pushMessage.des);
        Intent intent = new Intent();
        intent.setAction(CMN_PRGS_CHECK_ACTION);
        intent.putExtra(PUSH_MSG_EXTRA, pushMessage);
        sendBroadcast(intent);
    }


    public void showNotification(Context context, String txt, Intent intent) {
        Log.e(TAG+" showNotification", txt);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 下面需兼容Android 2.x版本是的处理方式
        NotificationCompat.Builder nb = new NotificationCompat.Builder(context).setContentTitle("精酿过程")
                .setContentText(txt).setTicker(txt).setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis()).setContentIntent(pendingIntent);
        Notification notify1 = null;
        notify1 = nb.build();

        notify1.flags |= Notification.DEFAULT_ALL; // FLAG_AUTO_CANCEL表明当通知被用户点击时，通知将被清除。
        // 通过通知管理器来发起通知。如果id不同，则每click，在statu那里增加一个提示
        manager.notify(GRAY_SERVICE_ID, notify1);
    }


    @Override
    public void onServerRespError(String cmd) {
        if("brew_session".equals(cmd) || "cmn_prgs".equals(cmd)){
            sendBroadcast(new Intent(REQUEST_BREW_SESSION_FAILED));
        }
    }

    @Override
    public void onLongConnectionKickedOut() {
        Intent intent = new Intent(this, BrewHomeActivity.class);
        showNotification(this, "服务未连上，请点击重连", intent);
        sendBroadcast(new Intent(SOCKET_IS_KICKED_OUT));
        startLongConnectionThread = null;
        if(transmitCmdService != null)
            transmitCmdService.closeSocket();
    }

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                /*
                BACKGROUND=400 EMPTY=500 FOREGROUND=100
                GONE=1000 PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
                 */
                Log.i(context.getPackageName(), "此appimportace ="
                        + appProcess.importance
                        + ",context.getClass().getName()="
                        + context.getClass().getName());
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Log.i(context.getPackageName(), "处于后台"
                            + appProcess.processName);
                    return true;
                } else {
                    Log.i(context.getPackageName(), "处于前台"
                            + appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }


}
