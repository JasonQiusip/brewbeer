package com.ltbrew.brewbeer.api.common.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by 151117a on 2016/5/2.
 */
public class MiscUtil {

    public static final Pattern TOKEN_REGEX = Pattern
            .compile("oauth_token=([^&^\\n^\\r]+)");
    public static final Pattern SECRET_REGEX = Pattern
            .compile("oauth_token_secret=([^&^\\n^\\r]*)");
    public static final Pattern VERIFIER_REGEX = Pattern
            .compile("oauth_verifier=([^&^\\n^\\r]+)");
    public static final Pattern NONENUMBER_REGEX = Pattern.compile("[^0-9]");
    public static final Pattern ALPHA_REGEX = Pattern.compile("^[_a-z\\d]+$");
    public static final int ESTIMATED_PARAM_LENGTH = 20;
    private static final String AMPERSAND_SEPARATED_STRING = "%s&%s&%s";
    private static final String PARAM_SEPARATOR = ", ";
    private static final String PREAMBLE = "OAuth ";
    // hmacsha1 related
    private static final String EMPTY_STRING = "";
    private static final String CARRIAGE_RETURN = "\r\n";
    private static final String UTF8 = "UTF-8";
    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final Map<String, String> ENCODING_RULES;

    static {
        Map<String, String> rules = new HashMap<String, String>();
        rules.put("*", "%2A");
//		rules.put("+", "%20");
        rules.put("%7E", "~");
        ENCODING_RULES = Collections.unmodifiableMap(rules);
    }

    // OAuthEncoder
    private static String CHARSET = "UTF-8";
    public Pattern accessTokenPattern = Pattern
            .compile("\"access_token\":\\s*\"(\\S*?)\"");

    private MiscUtil() {
    }


    public static String asOauthBaseString(String urlEncodedString) {
        return encodeURL(urlEncodedString);
    }


    public static String extract(String response, Pattern p) {
        Matcher matcher = p.matcher(response);
        if (matcher.find() && matcher.groupCount() >= 1) {
            return encodeURL(matcher.group(1).replace(CARRIAGE_RETURN, EMPTY_STRING));
        } else {
            return null;
        }
    }

    // encode base64
    public static String encodeBase64(byte[] bytes) {
        try {
            return new String(Base64.encode(bytes, 0), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    ;

    public static byte[] decodeBase64(String base64String) {
        return Base64.decode(base64String.getBytes(), 0);
    }

    // HMACSHA1

    /**
     * {@inheritDoc}
     */
    public static String getSignature(String baseString, String apiSecret,
                                      String tokenSecret) {
        try {
            return doSign(baseString, encodeURL(apiSecret) + '&'
                    + encodeURL(tokenSecret));
        } catch (Exception e) {
            return null;
        }
    }

    private static String doSign(String toSign, String keyString) throws Exception {

        SecretKeySpec key = new SecretKeySpec((keyString).getBytes(UTF8),
                HMAC_SHA1);
        Mac mac = Mac.getInstance(HMAC_SHA1);
        mac.init(key);
        byte[] bytes = mac.doFinal(toSign.getBytes(UTF8));

        return bytesToBase64String(bytes)
                .replace(CARRIAGE_RETURN, EMPTY_STRING);
    }

    private static String bytesToBase64String(byte[] bytes) {
        try {
            return new String(Base64.encode(bytes, 0), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String encodeURL(String plain) {
        String encoded = "";
        try {
            encoded = URLEncoder.encode(plain, CHARSET);
        } catch (UnsupportedEncodingException uee) {

        }

        for (Map.Entry<String, String> rule : ENCODING_RULES.entrySet()) {
            encoded = applyRule(encoded, rule.getKey(), rule.getValue());
        }
        return encoded;
    }

    private static String applyRule(String encoded, String toReplace,
                                    String replacement) {
        return encoded.replaceAll(Pattern.quote(toReplace), replacement);
    }

    public static String decode(String encoded) {
        try {
            return URLDecoder.decode(encoded, CHARSET);
        } catch (UnsupportedEncodingException uee) {
            return null;
        }
    }

    public static String getNonce() {
        Long ts = getTs();
        return String.valueOf(Math.abs(ts + new Random().nextInt()));
    }

    public static String getTimestampInSeconds() {
        return String.valueOf(getTs());
    }

    private static Long getTs() {
        return System.currentTimeMillis() / 1000;
    }

    public static String joinString(Map<String, String> dict, boolean encode) {
        if (dict == null)
            return null;
        StringBuffer content = new StringBuffer();
        int i = 0;
        int size = dict.size();
        for (Map.Entry<String, String> entry : dict.entrySet()) {
            if (entry.getValue() == null)
                return null;
            if (encode)
                content.append(String.format("%s=%s", encodeURL(entry.getKey()),
                        encodeURL(entry.getValue())));
            else
                content.append(String.format("%s=%s", entry.getKey(),
                        entry.getValue()));
            i++;
            if (i == size)
                break;
            content.append("&");
        }
        return content.toString();
    }

    public static String getNumber(String srcStr) {
        Matcher m = NONENUMBER_REGEX.matcher(srcStr);
        return m.replaceAll("").trim();
    }

    public static boolean checkOperation(String opStr) {
        Matcher m = ALPHA_REGEX.matcher(opStr);

        return m.matches();
    }

    public static String convertIntToString(int value) {
        return String.valueOf(value);
    }
}
