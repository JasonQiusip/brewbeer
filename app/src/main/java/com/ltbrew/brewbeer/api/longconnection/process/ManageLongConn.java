package com.ltbrew.brewbeer.api.longconnection.process;

import android.content.Context;
import android.content.SharedPreferences;

import com.ltbrew.brewbeer.BrewApp;
import com.ltbrew.brewbeer.api.model.Direct_push;

/**
 * Created by Jason on 2015/6/23.
 */
public class ManageLongConn {

    private SharedPreferences longConnSp;
    private static final String LONGCONN_SP_TITLE = "LongConnHost";
    private static final String ST_SP = "ST_STORAGE";
    private static final String PORT_INDEX_KEY = "portIndex";
    public Context context;
    public String ipHost;
    public int port;
    Direct_push direct_push;
    static ManageLongConn manageLongConnIp;
    int portIndex = 0;

    private ManageLongConn() {
    }

    public static ManageLongConn getInstance() {
        if (manageLongConnIp == null)
            manageLongConnIp = new ManageLongConn();
        return manageLongConnIp;

    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void initDirect_push(Context context, Direct_push direct_push) {
        this.direct_push = direct_push;
        longConnSp = context.getApplicationContext().getSharedPreferences(LONGCONN_SP_TITLE,
                Context.MODE_PRIVATE);
        setPort();
    }

    public void setPort() {
        if (!getLongConnIPFromLocal() && direct_push != null) {
            port = this.direct_push.getPorts().get(portIndex);
        }
    }

    public boolean getLongConnIPFromLocal() {
        if (direct_push == null)
            return false;
        ipHost = this.direct_push.getHost();
        port = longConnSp.getInt(PORT_INDEX_KEY, 0);
        if (port == 0)
            return false;
        return true;
    }

    public void switchPort() {
        if (direct_push == null)
            return;
        if (direct_push.getPorts().size() > portIndex) {
            portIndex++;
            port = direct_push.getPorts().get(portIndex);

        } else {
            portIndex = 0;
            port = direct_push.getPorts().get(portIndex);
        }
    }


    public void storeFdAndStToken(String fd, String stToken){
        getStSharePreference().edit().putString("fd", fd).commit();
        getStSharePreference().edit().putString("stToken", stToken).commit();
    }

    public String getFd(){
        return getStSharePreference().getString("fd", null);
    }

    public String getStToken(){
        return getStSharePreference().getString("stToken", null);
    }

    public void clearStSp(){
        getStSharePreference().edit().clear().commit();
    }

    public SharedPreferences getStSharePreference(){
        if(context == null)
            context = BrewApp.getInstance();

        return context.getApplicationContext().getSharedPreferences(ST_SP,
                Context.MODE_PRIVATE);
    }
}
