package com.ltbrew.brewbeer.api.common;

import android.content.Context;
import android.text.TextUtils;

import com.ltbrew.brewbeer.api.common.utils.SharePrefUtil;
import com.ltbrew.brewbeer.api.model.HttpMethodType;
import com.ltbrew.brewbeer.api.model.HttpReqParam;
import com.ltbrew.brewbeer.api.model.HttpResponse;
import com.ltbrew.brewbeer.api.ssoApi.LoginApi;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class TokenDispatcher {

    public static final String ACC = "acc";
    public static final String SEC = "sec";
    public static final String EXP = "exp";
    private static final boolean STOP_TRYING = false;
    private static final Pattern tokenPattern = Pattern.compile("([^|]+:[^|]+){1,3}");
    private static Context context;

    public void init(Context context){//need to be init once application is started
        this.context = context;
    }

    private static class LazyHolder {
        private static TokenDispatcher INSTANCE = new TokenDispatcher();
    }
    private TokenDispatcher (){}

    public static final TokenDispatcher getInstance() {
        if(LazyHolder.INSTANCE == null){
            LazyHolder.INSTANCE = new TokenDispatcher();
        }
        return LazyHolder.INSTANCE;
    }

    public void setUserName(String acc){
        SharePrefUtil.getInstance(context).storeAcc(acc);
    }

    public void resetToken(){
        SharePrefUtil.getInstance(context).storeToken(null);
    }

    public String getToken(){
        return SharePrefUtil.getInstance(context).getToken();
    }

    public void setPwd(String pwd){
        SharePrefUtil.getInstance(context).storePwd(pwd);
    }

    public void setToken(String token){
        SharePrefUtil.getInstance(context).storeToken(token);
    }

    public String getPwd(){
        return SharePrefUtil.getInstance(context).getPwd();
    }

    public String getUsername(){
        return SharePrefUtil.getInstance(context).getAcc();
    }

    public synchronized static HttpResponse delegateHttpReqWithToken(HttpReqParam request) {
        return delegateHttpWithToken(request, true);
    }

    private synchronized static HttpResponse delegateHttpWithToken(HttpReqParam request, boolean tryAgain) {

        String token = SharePrefUtil.getInstance(context).getToken(); //获取本地token
        if(TextUtils.isEmpty(token)){
            return getTokenFromServer(request);
        }
        HttpResponse httpResponse = new HttpResponse();
        String url = request.getUrlPath();
        TreeMap<String, String> header = request.getHeader();
        HashMap<String, String> body = request.getBody();
        if (body == null) {
            body = new HashMap<>(); //避免空指针异常
        }
        //when retry _tk and _sign are in the body, we need to remove it in order to calculate new sign
        if(body.containsKey("_tk")){
            body.remove("_tk");
        }
        if(body.containsKey("_sign")){
            body.remove("_sign");
        }
        if (request.getType() == HttpMethodType.Get) {
            String sign = OAuthUtil.getSignature(url, body, "GET", ApiCommonParams.api_secret);
            body.put("_tk", token);
            body.put("_sign", sign);
            httpResponse = HttpMethods.httpGet(url, body, header);
        } else {
            String sign = OAuthUtil.getSignature(url, body, "POST", ApiCommonParams.api_secret);
            body.put("_tk", token);
            body.put("_sign", sign);
            httpResponse = HttpMethods.httpPost(url, body, header);
        }

        if (httpResponse.isAuthorizeFailed() && tryAgain) {
            return getTokenFromServer(request);

        } else {
            return httpResponse;
        }
    }

    private static HttpResponse getTokenFromServer(HttpReqParam request) {
        String pwd = SharePrefUtil.getInstance(context).getPwd();
        String username = SharePrefUtil.getInstance(context).getAcc();
        HttpResponse loginResp = LoginAgain(pwd, username);
        if (loginResp.isSuccess() ) {
            SharePrefUtil.getInstance(context).storeToken(loginResp.getContent());
            return delegateHttpWithToken(request, STOP_TRYING);
        }else {
            return loginResp;
        }
    }

    private static HttpResponse LoginAgain(String pwd, String username) {
        return LoginApi.loginAsync(username, pwd);
    }


}
