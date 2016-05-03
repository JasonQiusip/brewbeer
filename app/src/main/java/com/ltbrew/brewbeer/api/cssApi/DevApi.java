package com.ltbrew.brewbeer.api.cssApi;

import com.ltbrew.brewbeer.api.common.ApiConstants;
import com.ltbrew.brewbeer.api.generalApi.GeneralApi;
import com.ltbrew.brewbeer.api.model.HttpReqParam;
import com.ltbrew.brewbeer.api.model.HttpResponse;

import java.util.HashMap;

/**
 * Created by 151117a on 2016/5/3.
 */
public class DevApi {

    public HttpResponse bindDev(String qrCode){
        HashMap body = new HashMap();
        body.put("code", qrCode);
        HttpReqParam httpReqParam = new HttpReqParam();
        httpReqParam.setBody(body);
        return GeneralApi.dev(httpReqParam, ApiConstants.BIND);
    }

    public HttpResponse setPhoneNo2Dev(String phoneNo, String qr){
        HashMap body = new HashMap();
        body.put("no", phoneNo);
        body.put("qr", qr);
        HttpReqParam httpReqParam = new HttpReqParam();
        httpReqParam.setBody(body);
        return GeneralApi.dev(httpReqParam, ApiConstants.PATCH_TID);
    }

    public HttpResponse unbindDev(String devId){
        HashMap body = new HashMap();
        body.put("id", devId);
        HttpReqParam httpReqParam = new HttpReqParam();
        httpReqParam.setBody(body);
        return GeneralApi.dev(httpReqParam, ApiConstants.UNBIND);
    }
}
