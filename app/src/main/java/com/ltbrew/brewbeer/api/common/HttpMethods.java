package com.ltbrew.brewbeer.api.common;

import android.util.Log;

import com.ltbrew.brewbeer.api.common.utils.MiscUtil;
import com.ltbrew.brewbeer.api.model.HttpResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.OkUrlFactory;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpMethods {

    protected static final String CONTROL_SUCCESS = "success";
    protected static final String CONTROL_FAIL = "fail";
    private static final String TAG = "HttpMethods";

    /**
     *
     * @param url
     * @param dict    a=123&b=456&c=4343
     * @param treeMap
     * @return
     */
    public static HttpResponse httpGet(String url, HashMap<String,String> dict, TreeMap<String, String> treeMap) {
        System.out.println(""+url+"    "+ dict+"");

        HttpResponse httpResp = new HttpResponse();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20000, TimeUnit.MILLISECONDS)
                .readTimeout(20000, TimeUnit.MILLISECONDS)
                .writeTimeout(20000, TimeUnit.MILLISECONDS).build();

        String queryParam = MiscUtil.joinString(dict, true);
        if(queryParam != null && !queryParam.equals(""))
        {
            url = url+"?"+queryParam;
        }
        Request.Builder reqBuilder = new Request.Builder();
        reqBuilder.header("content-type", "text/html");
        if(treeMap != null){
            for(String key : treeMap.keySet())
            {
                reqBuilder.header(key, treeMap.get(key));
            }
        }

        Request req = reqBuilder.url(url).build();
        try {
            Response response = client.newCall(req).execute();
            httpResp.setCode(response.code());
            if (response.code() == 200) {
                String body = response.body().string();
                httpResp.setContent(body);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            httpResp.setContent("");
        }
        return httpResp;
    }

    /**
     *
     * @param url
     * @param dict    a=123&b=456&c=4343
     * @param headerDict
     * @return
     */
    public static HttpResponse httpPost(String url, HashMap<String,String> dict, TreeMap<String, String> headerDict)
    {
//        System.out.println(url+"    "+dict);
        Log.e(TAG, url+" "+dict);
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setCode(-1);
        httpResponse.setContent(CONTROL_FAIL);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20000, TimeUnit.MILLISECONDS)
                .readTimeout(20000, TimeUnit.MILLISECONDS)
                .writeTimeout(20000, TimeUnit.MILLISECONDS).build();

        RequestBody formBody = okHttpPostBody(dict);
        Request.Builder reqBuilder = new Request.Builder();
        if(headerDict != null){
            for(String key : headerDict.keySet())
            {
                reqBuilder.header(key, headerDict.get(key));
            }
        }
        Request request = reqBuilder.url(url)
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            httpResponse.setCode(response.code());
            if (response.code() == 200) {
                String body = response.body().string();
                httpResponse.setContent(body);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return httpResponse;
    }

    public static HttpResponse httpGet(String url)
    {
        System.out.println(url);
        HttpResponse httpResponse = new HttpResponse();
        URL urlPath;
        try {
            urlPath = new URL(url);
            OkHttpClient httpClient = new OkHttpClient();
            OkUrlFactory okUrlFactory = new OkUrlFactory(httpClient);
            HttpURLConnection urlConnection = okUrlFactory.open(urlPath);;
            urlConnection.setConnectTimeout(20000);
            urlConnection.setReadTimeout(20000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            httpResponse.setCode(responseCode);
            if(responseCode != 200)
                return httpResponse;
            int len = 0;

            InputStream inputStream = urlConnection.getInputStream();

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while( (len=inputStream.read(buffer)) != -1){
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            inputStream.close();
            urlConnection.disconnect();
            httpResponse.setFile(outStream.toByteArray());
            return httpResponse;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return httpResponse;
    }

    public static HttpResponse httpPost(String url, byte[] file)
    {
        System.out.println(url);
        HttpResponse httpResponse = new HttpResponse();
        try {
            URL urlPath = new URL(url);
            OkHttpClient httpClient = new OkHttpClient();
            OkUrlFactory okUrlFactory = new OkUrlFactory(httpClient);
            HttpURLConnection urlConnection = okUrlFactory.open(urlPath);
            urlConnection.setConnectTimeout(30000);
            urlConnection.setReadTimeout(30000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Length", file.length+"");
            urlConnection.setRequestProperty("content-type", "text/html");
            urlConnection.connect();
            OutputStream out = urlConnection.getOutputStream();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(file);
            byte[] bytes=new byte[1024];
            int numReadByte=0;
            while((numReadByte=byteArrayInputStream.read(bytes,0,1024))>0){
                out.write(bytes,0,numReadByte);
            }
            out.flush();
            byteArrayInputStream.close();
            out.close();

            int responseCode = urlConnection.getResponseCode();
            httpResponse.setCode(responseCode);
            if(responseCode != 200) {
                return httpResponse;
            }
            InputStream inputStream = urlConnection.getInputStream();

            System.out.print(responseCode+"");

            BufferedReader reader = null;
//			Log.e("responseCode ", responseCode+"  responseCode");
            if (responseCode == 200) {
                reader = new BufferedReader(new InputStreamReader(inputStream));
            } else {
                reader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
            }
            StringBuffer result = new StringBuffer();
            String s = "";
            while((s = reader.readLine()) != null)
                result.append(s);

            httpResponse.setContent(result.toString());
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return httpResponse;
    }

    private static RequestBody okHttpPostBody(HashMap<String, String> queryParam) {
        FormBody.Builder formEncodingBuilder = new FormBody.Builder();
        //upload data
        if (queryParam != null) {
            for (Entry<String, String> entry : queryParam.entrySet()) {
                formEncodingBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        return formEncodingBuilder.build();
    }


}
