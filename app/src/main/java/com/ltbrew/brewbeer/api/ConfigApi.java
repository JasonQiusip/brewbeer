package com.ltbrew.brewbeer.api;

import android.content.Context;
import android.text.TextUtils;

import com.ltbrew.brewbeer.api.common.TokenDispatcher;
import com.ltbrew.brewbeer.api.common.utils.SharePrefUtil;
import com.ltbrew.brewbeer.api.longconnection.TransmitCmdService;
import com.ltbrew.brewbeer.api.longconnection.interfaces.FileSocketReadyCallback;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.api.ssoApi.LoginApi;

/**
 * Created by 151117a on 2016/5/2.
 */
public class ConfigApi {

    public static String api_key;
    public static String api_secret;

    public static void init(Context context){
        TokenDispatcher.getInstance().init(context);
    }

    public static void startLongConnection(FileSocketReadyCallback cb){
        String token = TokenDispatcher.getInstance().getToken();
        if(TextUtils.isEmpty(token)){
            String pwd = TokenDispatcher.getInstance().getPwd();
            String username = TokenDispatcher.getInstance().getUsername();
            HttpResponse loginResp = LoginApi.loginAsync(username, pwd);
            if(loginResp.isSuccess()) {
                token = loginResp.getContent();
            }else {
                cb.onGetOauthTokenFailed();
            }
        }
       TransmitCmdService.newInstance(token, cb).initializeCmdLongConn();
    }
}
