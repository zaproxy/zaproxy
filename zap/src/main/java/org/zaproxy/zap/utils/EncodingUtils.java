package org.zaproxy.zap.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

public class EncodingUtils {
	
	public static String mapToString(Map<String, String> map) {
		StringBuilder stringBuilder = new StringBuilder();

		for (String key : map.keySet()) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append("&");
			}
			String value = map.get(key);
			stringBuilder.append(key != null ? Base64.encodeBase64String(key.getBytes()) : "");
			stringBuilder.append(":");
			stringBuilder.append(value != null ? Base64.encodeBase64String(value.getBytes()) : "");
		}

		return stringBuilder.toString();
	}

	public static Map<String, String> stringToMap(String input) {
		Map<String, String> map = new HashMap<String, String>();

		String[] nameValuePairs = input.split("&");
		for (String nameValuePair : nameValuePairs) {
			String[] nameValue = nameValuePair.split(":");
			map.put(new String(Base64.decodeBase64(nameValue[0])),
					nameValue.length > 1 ? new String(Base64.decodeBase64(nameValue[1])) : "");
		}

		return map;
	}

}
