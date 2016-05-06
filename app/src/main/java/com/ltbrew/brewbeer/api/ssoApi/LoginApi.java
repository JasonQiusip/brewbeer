package com.ltbrew.brewbeer.api.ssoApi;

import com.ltbrew.brewbeer.api.common.ApiCommonParams;
import com.ltbrew.brewbeer.api.common.ApiConstants;
import com.ltbrew.brewbeer.api.common.HttpMethods;
import com.ltbrew.brewbeer.api.common.TokenDispatcher;
import com.ltbrew.brewbeer.api.model.HttpMethodType;
import com.ltbrew.brewbeer.api.model.HttpReqParam;
import com.ltbrew.brewbeer.api.model.HttpResponse;

import java.util.HashMap;

/**
 * Created by 151117a on 2016/5/2.
 */
public class LoginApi {

    public static HttpResponse checkAccount(String username){
        HashMap dict = new HashMap();
        dict.put("account", username + ApiConstants.ACC_SUFFIX);
        return HttpMethods.httpPost(ApiCommonParams.AUTHORIZE_URL + ApiConstants.SMS_VALID_ACC_NEW, dict, null);
    }

    public static HttpResponse loginAsync(final String username, final String pwd) {
        HashMap<String, String> dict = new HashMap<String, String>();
        dict.put("api_key", ApiCommonParams.api_key);
        dict.put("username", username + ApiConstants.ACC_SUFFIX);
        dict.put("pwd", pwd);
        HttpResponse login = HttpMethods.httpGet(ApiCommonParams.AUTHORIZE_URL + ApiConstants.GEN_TK, dict, null);
        if(login.isSuccess()){
            TokenDispatcher.getInstance().setUserName(username);
            TokenDispatcher.getInstance().setPwd(pwd);
            TokenDispatcher.getInstance().setToken(login.getContent());
        }
        return login;
    }

    
}
