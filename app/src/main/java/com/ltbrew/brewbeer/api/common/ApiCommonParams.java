package com.ltbrew.brewbeer.api.common;

public class ApiCommonParams {

    public static final String api_key = "aea8b32e091a11e681ee408d5c5a48ca";
    public static final String encryption_key = "e91647cf091a11e69e11408d5c5a48caf4393b8f091a11e6bb44408d5c5a48ca";
    //    192.168.2.168
    public static final String IP_DOMAIN = "218.5.96.6";
    public static final String AUTHORIZE_URL = "http://" + IP_DOMAIN + ":8302/";
//    public static final String API_URL = "http://" + IP_DOMAIN + ":8100";
    public static final String DOWN_UP_URL = "http://" + IP_DOMAIN + ":8304";
    public static final String COMMON_API_URL = "http://" + IP_DOMAIN + ":8300";

    public static final String API_URL = "http://" + IP_DOMAIN + ":8300";  //正式网络ip地址
    public static String api_secret;
    public static String token;
}
