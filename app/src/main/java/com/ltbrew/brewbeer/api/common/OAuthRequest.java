package com.ltbrew.brewbeer.api.common;

import com.ltbrew.brewbeer.api.common.utils.MiscUtil;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.TreeMap;

public class OAuthRequest {
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final int REQUEST_TEMP_TOKEN = 0;
    public static final int AUTHORIZATION = 1;
    public static final int REQUEST_ACCESS_TOKEN = 2;
    public static final int PACK_ACCESS_TOKEN = 3;
    public static final String TIMESTAMP = "oauth_timestamp";
    public static final String SIGN_METHOD = "oauth_signature_method";
    public static final String SIGNATURE = "oauth_signature";
    public static final String CONSUMER_SECRET = "oauth_consumer_secret";
    public static final String CONSUMER_KEY = "oauth_consumer_key";
    public static final String CALLBACK = "oauth_callback";
    public static final String VERSION = "oauth_version";
    public static final String NONCE = "oauth_nonce";
    public static final String REALM = "realm";
    public static final String PARAM_PREFIX = "oauth_";
    public static final String TOKEN = "oauth_token";
    public static final String TOKEN_SECRET = "oauth_token_secret";
    public static final String OUT_OF_BAND = "oob";
    public static final String VERIFIER = "oauth_verifier";
    public static final String HEADER = "Authorization";
    public static final String SCOPE = "scope";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String CODE = "code";
    private static final String OAUTH_PREFIX = "oauth_";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final char QUERY_STRING_SEPARATOR = '?';
    private static final String PARAM_SEPARATOR = "&";
    private static final String PAIR_SEPARATOR = "=";
    private static final String EMPTY_STRING = "";
    public ArrayList<TreeMap<String, String>> bodyList = new ArrayList();
    public ArrayList<TreeMap<String, String>> queryStringList1 = new ArrayList();
    private TreeMap<String, String> oauthParameters;
    private String realm;
    private String url;
    private String verb;
    private String queryStr;
    private String tokenSecret;
    private TreeMap<String, String> headers;
    private String charset;
    private TreeMap<String, String> bodyMap;
    private TreeMap<String, String> queryStringMap;
    private TreeMap<String, String> queryDict;

    public OAuthRequest(String verb, String url) {
        this.verb = verb;
        this.url = url;
        this.headers = new TreeMap();
        this.oauthParameters = new TreeMap();
    }

    public String appendTo(String url) {
        String queryString = this.asFormUrlEncodedString();
        if (queryString.equals("")) {
            return url;
        } else {
            url = url + (url.indexOf(63) != -1 ? "&" : Character.valueOf('?'));
            url = url + queryString;
            return url;
        }
    }

    public String asUrlEncodedPair(String key, String value) {
        return MiscUtil.encodeURL(key).concat("=").concat(MiscUtil.encodeURL(value));
    }

    public String asFormUrlEncodedString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < this.oauthParameters.keySet().size(); ++i) {
            builder.append('&').append(this.asUrlEncodedPair(this.oauthParameters.keySet().toArray()[i].toString(), this.oauthParameters.values().toArray()[i].toString()));
        }

        return builder.toString().substring(1);
    }

    public void addOAuthParameter(String key, String value) {
        this.oauthParameters.put(this.checkKey(key), value);
    }

    private String checkKey(String key) {
        if (!key.startsWith("oauth_") && !key.equals("scope")) {
            throw new IllegalArgumentException(String.format("OAuth parameters must either be \'%s\' or start with \'%s\'", new Object[]{"scope", "oauth_"}));
        } else {
            return key;
        }
    }

    public TreeMap<String, String> getOauthParameters() {
        return this.oauthParameters;
    }

    public String getRealm() {
        return this.realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String toString() {
        return String.format("@OAuthRequest(%s, %s)", new Object[]{this.getVerb(), this.getUrl()});
    }

    public String getCompleteUrl() {
        return this.appendTo(this.url);
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void addBodyParameter(String key, String value) {
        this.bodyMap.put(key, value);
    }

    public void addQuerystringParameter(String key, String value) {
        this.queryStringMap.put(key, value);
    }

    public String getUrl() {
        return this.url;
    }

    public String getSanitizedUrl() {
        return !this.url.startsWith("http://") || !this.url.endsWith(":80") && !this.url.contains(":80/") ? (!this.url.startsWith("https://") || !this.url.endsWith(":443") && !this.url.contains(":443/") ? this.url.replaceAll("\\?.*", "") : this.url.replaceAll("\\?.*", "").replaceAll(":443", "")) : this.url.replaceAll("\\?.*", "").replaceAll(":80", "");
    }

    public String getVerb() {
        return this.verb;
    }

    public TreeMap<String, String> getHeaders() {
        return this.headers;
    }

    public String getCharset() {
        return this.charset == null ? Charset.defaultCharset().name() : this.charset;
    }

    public void setCharset(String charsetName) {
        this.charset = charsetName;
    }

    public TreeMap<String, String> getQueryData() {
        return this.queryDict;
    }

    public void setQueryData(TreeMap<String, String> dict) {
        this.queryDict = dict;
    }
}
