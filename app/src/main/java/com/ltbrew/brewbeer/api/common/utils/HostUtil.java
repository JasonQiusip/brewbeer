package com.ltbrew.brewbeer.api.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.ltbrew.brewbeer.api.common.CSSLog;
import com.ltbrew.brewbeer.api.common.ServerHostHanlder;
import com.ltbrew.brewbeer.api.longconnection.process.ManageLongConnIp;
import com.ltbrew.brewbeer.api.model.HostPorts;

import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by 151117a on 2016/5/2.
 */
public class HostUtil {
    private static int expireDuration;
    private static long lastReqTimeSec;
    private static int hostIndex;
    private static int portsIndex;
    private static int portSize;
    private static ArrayList<HostPorts> hostPort;
    private static HostPorts currentHostPorts;
    private static String HOST = "117.28.254.73:8000";
    private static String DOWNHOST = "117.28.254.73:8004";
    private static String GET_TOKEN_HOST = "117.28.254.73:8002";
//    private static String HOST = "218.5.96.6:8300";
//    private static String DOWNHOST = "218.5.96.6:8304";
//    private static String GET_TOKEN_HOST = "218.5.96.6:8302";
    private static String GEN_TK = "http://" + GET_TOKEN_HOST + "/gen_tk";

    public static final String resUrl = "http://%s/res/%s/%s/%s";
    public static final String ctrlUrl = "http://%s/ctrl/%s/%s/%s";
    public static final String upUrl = "http://%s/up";
    public static final String downUrl = "http://%s/down";
    private String apiKey;
    private String apiSecret;


    public static boolean checkHostports(Context context)
    {
        initHostPortIndex();
        long currentTime = System.currentTimeMillis() / 1000L;
        SharedPreferences oauthSp = context.getApplicationContext().getSharedPreferences("oauth", 0);

        SharedPreferences spHost = context.getApplicationContext().getSharedPreferences("HostPorts", 0);

        expireDuration = spHost.getInt("expireDuration", 0);
        lastReqTimeSec = oauthSp.getLong("lastReqTimeSec", 0L);
        if (currentTime - lastReqTimeSec > expireDuration)
        {
            boolean bHost = getHostsFromRoutes(context);
            if (bHost)
            {
                ManageLongConnIp.getInstance().initDirect_push(context, currentHostPorts.getDirect_push());
                return true;
            }
            return false;
        }
        hostIndex = oauthSp.getInt("hostIndex", 0);
        portsIndex = oauthSp.getInt("portsIndex", 0);
        String hostJson = spHost.getString(hostIndex + "", "");
        HostPorts hostPortsFromLocal = getHostFromLocalSrc(hostJson);
        ManageLongConnIp.getInstance().initDirect_push(context, hostPortsFromLocal.getDirect_push());
        return true;
    }

    private static void initHostPortIndex()
    {
        hostIndex = 0;
        portsIndex = 0;
        expireDuration = 0;
        lastReqTimeSec = 0L;
        portSize = 0;
    }

    public static boolean getHostsFromRoutes(Context context)
    {
        ArrayList<String> ipAddressFromServer = null;
        try
        {
            ipAddressFromServer = ServerHostHanlder.getIpAddressFromServer();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            return true;
        }
        if (ipAddressFromServer == null) {
            return false;
        }
        try
        {
            hostPort = ServerHostHanlder.getHostPort(ipAddressFromServer);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (hostPort.size() == 0) {
            return false;
        }
        lastReqTimeSec = System.currentTimeMillis() / 1000L;

        storeDataToLocalSP(context);

        intRoutesHost();
        return true;
    }

    private static void storeDataToLocalSP(Context context)
    {
        SharedPreferences spHost = context.getApplicationContext().getSharedPreferences("HostPorts", 0);


        for (int i = 0; i < hostPort.size(); i++)
        {
            String toJson = JSON.toJSONString(hostPort.get(i));
            CSSLog.showLog("storeDataToLocalSp", toJson);
            spHost.edit().putString(i + "", toJson).commit();
            spHost.edit().putInt("expireDuration", ((HostPorts)hostPort.get(i)).getApi().getTtl()).commit();
        }
    }

    private static void intRoutesHost()
    {
        currentHostPorts = (HostPorts)hostPort.get(hostIndex);
        setHosts(currentHostPorts);
    }

    public static HostPorts getHostFromLocalSrc(String hostJson)
    {
        currentHostPorts = JSON.parseObject(hostJson, HostPorts.class);
        setHosts(currentHostPorts);
        return currentHostPorts;
    }

    private static synchronized void setHosts(HostPorts hostPorts)
    {
        String host = hostPorts.getApi().getHost();
        portSize = hostPorts.getApi().getPorts().size();
        if (portsIndex >= portSize) {
            portsIndex = portSize - 1;
        }
        HOST = host + ":" + hostPorts.getApi().getPorts().get(portsIndex);
        GET_TOKEN_HOST = host + ":" + hostPorts.getSso().getPorts().get(portsIndex);
        DOWNHOST = host + ":" + hostPorts.getFile().getPorts().get(portsIndex);
        initAddress();
        portSize = hostPorts.getApi().getPorts().size();
        CSSLog.showLog("", "HOST:----" + HOST + "  GET_TOKEN_HOST  " + GET_TOKEN_HOST);
    }
    private static void initAddress()
    {
        GEN_TK = "http://" + GET_TOKEN_HOST + "/gen_tk";
    }

    public static String getGetTokenHost()
    {
        return GET_TOKEN_HOST;
    }

    public static String getApiHost()
    {
        return HOST;
    }

    public static String getLoginUrl(){
        return GEN_TK;
    }

    public static String getDownloadHost(){
        return DOWNHOST;
    }

}
