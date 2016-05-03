package com.ltbrew.brewbeer.api.generalApi;

import com.ltbrew.brewbeer.api.common.HttpMethods;
import com.ltbrew.brewbeer.api.common.TokenDispatcher;
import com.ltbrew.brewbeer.api.common.utils.HostUtil;
import com.ltbrew.brewbeer.api.model.HttpMethodType;
import com.ltbrew.brewbeer.api.model.HttpReqParam;
import com.ltbrew.brewbeer.api.model.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by 151117a on 2016/5/2.
 */
public class GeneralApi {
    public static final String device_type = "pt30";

    public static HttpResponse control(HttpReqParam httpReqParam, String devId, String operation){
        String url = String.format(HostUtil.ctrlUrl, new Object[] {HostUtil.getApiHost(), device_type, devId, operation });
        httpReqParam.setUrlPath(url);
        httpReqParam.setType(HttpMethodType.Post);
        return TokenDispatcher.delegateHttpReqWithToken(httpReqParam);
    }

    public static HttpResponse res(HttpReqParam httpReqParam, String devId, String operation){
        String url = String.format(HostUtil.resUrl, new Object[] {HostUtil.getApiHost(), device_type, devId, operation });
        httpReqParam.setUrlPath(url);
        httpReqParam.setType(HttpMethodType.Get);
        return TokenDispatcher.delegateHttpReqWithToken(httpReqParam);
    }

    public static HttpResponse dev(HttpReqParam httpReqParam, String operation){
        String url = "http://" + HostUtil.getApiHost() + "/dev/" + operation;
        httpReqParam.setUrlPath(url);
        httpReqParam.setType(HttpMethodType.Get);
        return TokenDispatcher.delegateHttpReqWithToken(httpReqParam);
    }

    public static HttpResponse downloadFile(String device_type, String deviceid, String receipt, String fn)
    {
        HashMap<String, String> dict = new HashMap();
        dict.put("fn", fn);
        dict.put("share", ""+1);
        dict.put("m", "1");
        dict.put("r", receipt);
        String fileToken = getFileToken(device_type, deviceid, dict);
        if (fileToken == null) {
            return null;
        }
        try
        {
            JSONObject jsonObject = new JSONObject(fileToken);
            fileToken = jsonObject.getString("tk");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (fileToken != null)
        {
            String path = String.format(HostUtil.downUrl, new Object[] { HostUtil.getDownloadHost() });
            path = path + "?tk=" + fileToken + "&r=" + receipt;
            HttpResponse httpResponse = HttpMethods.httpGet(path);
            return httpResponse;
        }
        return null;
    }

    private static String getFileToken(String device_type, String deviceid, HashMap<String, String> dict)
    {
        HttpReqParam httpReqParam = new HttpReqParam();
        httpReqParam.setBody(dict);
        HttpResponse resource = res(httpReqParam, deviceid, "file_tk");
        if (resource.isSuccess()) {
            return resource.getContent();
        }
        return null;
    }

}
