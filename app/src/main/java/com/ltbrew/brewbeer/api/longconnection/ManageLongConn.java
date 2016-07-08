package com.ltbrew.brewbeer.api.longconnection;

import android.content.Context;
import android.content.SharedPreferences;

import com.ltbrew.brewbeer.BrewApp;
import com.ltbrew.brewbeer.api.common.HostUtil;
import com.ltbrew.brewbeer.api.model.Direct_push;
import com.ltbrew.brewbeer.api.model.Lt_stream;

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
    private Lt_stream ltStream;

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
        ipHost = direct_push.getHost();
        longConnSp = context.getApplicationContext().getSharedPreferences(LONGCONN_SP_TITLE,
                Context.MODE_PRIVATE);
    }

    public Direct_push getDirectPush(){
        if(direct_push == null){
            HostUtil.checkHostports(BrewApp.getInstance());
        }
        return this.direct_push;
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

    public void setLtStream(Lt_stream ltStream){
        this.ltStream = ltStream;

    }

    public Lt_stream getLtStream(){
        if(ltStream == null){
            HostUtil.checkHostports(BrewApp.getInstance());
        }
        return this.ltStream;
    }
}
