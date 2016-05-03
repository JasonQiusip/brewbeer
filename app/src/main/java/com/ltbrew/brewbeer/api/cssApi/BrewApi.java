package com.ltbrew.brewbeer.api.cssApi;

import com.ltbrew.brewbeer.api.common.ApiConstants;
import com.ltbrew.brewbeer.api.generalApi.GeneralApi;
import com.ltbrew.brewbeer.api.model.HttpReqParam;
import com.ltbrew.brewbeer.api.model.HttpResponse;

import java.util.HashMap;

/**
 * Created by 151117a on 2016/5/2.
 */
public class BrewApi {


    public static HttpResponse getBrewRecipes(String devId, String formula_id){
        HashMap body = new HashMap();
        body.put("formula_id", formula_id);
        HttpReqParam httpReqParam = new HttpReqParam();
        httpReqParam.setBody(body);
        return GeneralApi.res(httpReqParam, devId, ApiConstants.BREW_LS_FORMULA);
    }

    public static HttpResponse beginBrew(String devId, String pack_id, String is_open){
        HashMap body = new HashMap();
        body.put("pack_id", pack_id);
        body.put("is_open", is_open);
        HttpReqParam httpReqParam = new HttpReqParam();
        httpReqParam.setBody(body);
        return GeneralApi.res(httpReqParam, devId, ApiConstants.BREW_BEGIN);
    }

    public static HttpResponse getBrewHistory(String devId, String begin_date, String end_date, String state){
        HashMap body = new HashMap();
        body.put("begin_date", begin_date);
        body.put("end_date", end_date);
        body.put("state", state);
        HttpReqParam httpReqParam = new HttpReqParam();
        httpReqParam.setBody(body);
        return GeneralApi.res(httpReqParam, devId, ApiConstants.BREW_HISTORY);
    }
}
