package com.ltbrew.brewbeer.api.common;

import com.ltbrew.brewbeer.api.common.utils.MiscUtil;
import com.ltbrew.brewbeer.api.common.utils.RC4;

import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class RegisterInteface {

	private static String CHARSET = "UTF-8";
	// hmacsha1 related
	private static final String EMPTY_STRING = "";
	private static final String CARRIAGE_RETURN = "\r\n";
	private static final String HMAC_SHA1 = "HmacSHA1";

	/**
	 * 
	 * @param key
	 * @param acc
	 * @param pwd
	 */
	public static String register(String key, String secret, String acc,String pwd,  String code) {
		TreeMap<String, String> dict1 = new TreeMap<String, String>();
		dict1.put("acc", acc);
		if (code != null)
			dict1.put("code", code);
		if (pwd != null)
			dict1.put("pwd", pwd);

		String paramString = MiscUtil.joinString(dict1, false);
		String a = RsyncUtils.toHexStr(new RC4(charToByteArray(secret.toCharArray())).encrypt(charToByteArray(paramString.toCharArray())));
		String signature = getSignature(paramString, secret, "");

		TreeMap<String, String> dict = new TreeMap<String, String>();
		dict.put("k", key);
		dict.put("a", a);
		dict.put("h", signature);
		dict.put("v", "1");

		return MiscUtil.joinString(dict, false);
	}

	static StringBuilder byteArrayToStr(byte[] byteArray) {
		StringBuilder sb = new StringBuilder();
		for (byte b : byteArray) {
			char ch = (char) (b & 0xff);
			sb.append(ch);
		}
		return sb;
	}

	public static byte[] charToByteArray(char[] charArray) {

		byte[] input = new byte[charArray.length];
		int i = 0;
		for (char ch : charArray) {
			input[i] = (byte) ch;
			i++;
		}
		return input;
	}
	// HMACSHA1
	/**
	 * {@inheritDoc}
	 */
	public static String getSignature(String baseString, String apiSecret, String tokenSecret) {
		try {
			return doSign(baseString, apiSecret);
		} catch (Exception e) {
			return null;
		}
	}

	private static String doSign(String toSign, String keyString) throws Exception {

		SecretKeySpec key = new SecretKeySpec((keyString).getBytes(CHARSET), HMAC_SHA1);
		Mac mac = Mac.getInstance(HMAC_SHA1);
		mac.init(key);
		byte[] bytes = mac.doFinal(toSign.getBytes(CHARSET));

		return RsyncUtils.toHexStr(bytes).replace(CARRIAGE_RETURN, EMPTY_STRING);
	}
}