package com.ltbrew.brewbeer.api.common;

public class ApiCommonParams {

    public static final String api_key = "840ebe7c2bfe4d529181063433ece0ef";
    public static String api_secret = "426e26e82c704e5984b4a30071cc3775";

    //    192.168.2.168
    public static final String IP_DOMAIN = "218.5.96.6";
    public static final String AUTHORIZE_URL = "http://" + IP_DOMAIN + ":8302/";
//    public static final String API_URL = "http://" + IP_DOMAIN + ":8100";
    public static final String DOWN_UP_URL = "http://" + IP_DOMAIN + ":8304";
    public static final String COMMON_API_URL = "http://" + IP_DOMAIN + ":8300";

    public static final String API_URL = "http://" + IP_DOMAIN + ":8300";  //正式网络ip地址
    public static String token;
}
