package com.ltbrew.brewbeer.api.common;

import android.app.Dialog;
import com.ltbrew.brewbeer.api.common.utils.MiscUtil;
import com.ltbrew.brewbeer.api.model.HttpResponse;

import java.util.HashMap;
import java.util.TreeMap;

public class OAuthUtil
{
  private String apiKey;
  private String apiSecret;
  private final String callback = "http://www.linktopcss.com/ready";
  private String token_Secret = "";
  private String verifier = "";
  private String callbackUrl;
  private Object locker = new Object();
  private Dialog dialog;
  private String username;
  private String password;
  
  protected OAuthUtil(String key, String secret, String username, String password)
  {
    this.apiKey = key;
    this.apiSecret = secret;
    this.username = username;
    this.password = password;
  }
  

  public static String getSignature(String requestUrl, HashMap<String, String> dict, String verb, String apiSecret)
  {
    OAuthRequest request = new OAuthRequest(verb, requestUrl);
    TreeMap<String, String> map = new TreeMap();
    if (dict != null) {
      map.putAll(dict);
    }
    request.setQueryData(map);
    return calSignature(request, apiSecret);
  }
  
  private static String calSignature(OAuthRequest request, String tokenSecret)
  {
    TreeMap<String, String> queryData = request.getQueryData();
    StringBuilder strBuilder = new StringBuilder();
    for (String key : request.getQueryData().keySet()) {
      strBuilder.append(key + "=" + (String)queryData.get(key));
    }
    String baseString = request.getVerb() + request.getUrl() + strBuilder.toString() + tokenSecret;
    baseString = MiscUtil.encodeURL(baseString);
    String signature = RsyncUtils.cal_MD5(baseString.getBytes());
    return signature;
  }
}
