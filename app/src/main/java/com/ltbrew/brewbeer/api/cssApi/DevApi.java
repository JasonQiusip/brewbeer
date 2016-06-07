package com.ltbrew.brewbeer.api.cssApi;

import android.text.TextUtils;

import com.ltbrew.brewbeer.api.common.ApiConstants;
import com.ltbrew.brewbeer.api.generalApi.GeneralApi;
import com.ltbrew.brewbeer.api.model.HttpReqParam;
import com.ltbrew.brewbeer.api.model.HttpResponse;

import java.util.HashMap;

/**
 * Created by 151117a on 2016/5/3.
 */
public class DevApi {

    public static HttpResponse getDevs(){
        return GeneralApi.dev(null, ApiConstants.LIST);
    }

    public static HttpResponse bindDev(String qrCode){
        HashMap body = new HashMap();
        body.put("code", qrCode);
        HttpReqParam httpReqParam = new HttpReqParam();
        httpReqParam.setBody(body);
        return GeneralApi.dev(httpReqParam, ApiConstants.BIND);
    }

    public static HttpResponse followDev(String qrCode){
        HashMap body = new HashMap();
        body.put("code", qrCode);
        HttpReqParam httpReqParam = new HttpReqParam();
        httpReqParam.setBody(body);
        return GeneralApi.dev(httpReqParam, ApiConstants.FOLLOW_REQ);
    }
//    id	pid
//    account	主帐号
//    val	验证码
    public static HttpResponse checkFollow(String pid, String acc, String val){
        HashMap body = new HashMap();
        body.put("id", pid);
        body.put("account", acc);
        body.put("val", val);
        HttpReqParam httpReqParam = new HttpReqParam();
        httpReqParam.setBody(body);
        return GeneralApi.dev(httpReqParam, ApiConstants.CHECK_FOLLOW_VAL_CODE);
    }

    public static HttpResponse verifyIotByQr(String qrCode){
        HashMap body = new HashMap();
        body.put("qr", qrCode);
        HttpReqParam httpReqParam = new HttpReqParam();
        httpReqParam.setBody(body);
        return GeneralApi.dev(httpReqParam, ApiConstants.VERIFY_IOT);
    }

    public static HttpResponse setPhoneNo2Dev( String qr, String phoneNo){
        HashMap body = new HashMap();
        body.put("qr", qr);
        body.put("no", phoneNo);
        HttpReqParam httpReqParam = new HttpReqParam();
        httpReqParam.setBody(body);
        return GeneralApi.dev(httpReqParam, ApiConstants.PATCH_TID);
    }

    public static HttpResponse unbindDev(String devId){
        HashMap body = new HashMap();
        body.put("id", devId);
        HttpReqParam httpReqParam = new HttpReqParam();
        httpReqParam.setBody(body);
        return GeneralApi.dev(httpReqParam, ApiConstants.UNBIND);
    }

    public static HttpResponse getDevInfo(String devId){
        return GeneralApi.res(null, devId, ApiConstants.INFO);
    }
}
