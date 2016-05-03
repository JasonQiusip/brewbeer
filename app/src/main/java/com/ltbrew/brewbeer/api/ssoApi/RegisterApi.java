package com.ltbrew.brewbeer.api.ssoApi;

import com.ltbrew.brewbeer.api.common.ApiCommonParams;
import com.ltbrew.brewbeer.api.common.ApiConstants;
import com.ltbrew.brewbeer.api.common.HttpMethods;
import com.ltbrew.brewbeer.api.common.RegisterInteface;
import com.ltbrew.brewbeer.api.model.HttpResponse;

import java.util.HashMap;

/**
 * Created by 151117a on 2016/5/2.
 */
public class RegisterApi {

    public static HttpResponse reqRegCode(String username){
        HashMap dict = new HashMap();
        dict.put("account", username + ApiConstants.ACC_SUFFIX);
        return HttpMethods.httpPost(ApiCommonParams.AUTHORIZE_URL + ApiConstants.SMS_REQ_REG_VAL_CODE, dict, null);
    }

    public static HttpResponse register(String username, String pwd, String code){
        String registerStr = RegisterInteface.register(ApiCommonParams.api_key, ApiCommonParams.api_secret, username, pwd, code);
        return HttpMethods.httpPost(ApiCommonParams.AUTHORIZE_URL + ApiConstants.ACTIVE_SMS, RegisterInteface.charToByteArray(registerStr.toCharArray()));
    }

    public static HttpResponse reqPwdLost(String username){
        HashMap dict = new HashMap();
        dict.put("account", username + ApiConstants.ACC_SUFFIX);
        return HttpMethods.httpPost(ApiCommonParams.AUTHORIZE_URL + ApiConstants.SMS_PWD_LOSE, dict, null);
    }

    public static HttpResponse setNewPwd(String username, String pwd, String code){
        HashMap dict = new HashMap();
        dict.put("account", username + ApiConstants.ACC_SUFFIX);
        dict.put("pwd", pwd);
        dict.put("val", code);
        return HttpMethods.httpPost(ApiCommonParams.AUTHORIZE_URL + ApiConstants.SMS_PWD_NEW, dict, null);
    }
}
