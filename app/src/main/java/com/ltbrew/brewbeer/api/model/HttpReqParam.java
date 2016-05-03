package com.ltbrew.brewbeer.api.model;

import java.util.HashMap;
import java.util.TreeMap;

public class HttpReqParam {
    private HttpMethodType type;
    private String urlPath;
    private HashMap<String, String> body;
    private TreeMap<String, String> header;

    public HttpReqParam(){}

    public HttpReqParam(HttpMethodType type, String urlPath) {
        this.type = type;
        this.urlPath = urlPath;
    }

    public HttpMethodType getType() {
        return type;
    }

    public void setType(HttpMethodType type) {
        this.type = type;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public HashMap<String, String> getBody() {
        return body;
    }

    public void setBody(HashMap<String, String> body) {
        this.body = body;
    }

    public TreeMap<String, String> getHeader() {
        return header;
    }

    public void setHeader(TreeMap<String, String> header) {
        this.header = header;
    }


}
