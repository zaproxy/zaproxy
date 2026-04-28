package org.zaproxy.zap.extension.sensitive;

import java.util.Locale;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class SensitiveDataUtils {

    private static final Logger LOGGER = LogManager.getLogger(SensitiveDataUtils.class);

    private SensitiveDataUtils() {}

    public static String maskIfSensitive(
            String key, String value, OptionsParamSensitiveData sensitiveDataOptions) {

        if (sensitiveDataOptions == null
                || !sensitiveDataOptions.isMaskingEnabled()
                || key == null
                || key.isEmpty()) {
            return value;
        }

        String normalizedKey = key.toLowerCase(Locale.ROOT);

        for (String sensitiveKey : sensitiveDataOptions.getSensitiveKeys()) {
            if (sensitiveKey == null || sensitiveKey.isEmpty()) {
                continue;
            }

            if (normalizedKey.contains(sensitiveKey.toLowerCase(Locale.ROOT))) {
                String maskValue = sensitiveDataOptions.getMaskValue();
                String finalMask = (maskValue == null || maskValue.isEmpty()) ? "****" : maskValue;

                LOGGER.info("[SensitiveDataMask] key='{}' masked", key);

                return finalMask;
            }
        }

        return value;
    }

    public static String maskHeaderLineIfSensitive(
            String headerLine, OptionsParamSensitiveData sensitiveDataOptions) {

        if (headerLine == null || headerLine.isEmpty()) {
            return headerLine;
        }

        int separatorIndex = headerLine.indexOf(':');

        if (separatorIndex <= 0) {
            return headerLine;
        }

        String headerName = headerLine.substring(0, separatorIndex).trim();
        String headerValue = headerLine.substring(separatorIndex + 1).trim();

        String maskedValue = maskIfSensitive(headerName, headerValue, sensitiveDataOptions);

        if (maskedValue.equals(headerValue)) {
            return headerLine;
        }

        LOGGER.info("[SensitiveDataMask] Mask applied to header '{}'", headerName);

        return headerLine.substring(0, separatorIndex + 1) + " " + maskedValue;
    }

    public static String maskHeaderBlockIfSensitive(
            String headerBlock, OptionsParamSensitiveData sensitiveDataOptions) {

        if (headerBlock == null
                || headerBlock.isEmpty()
                || sensitiveDataOptions == null
                || !sensitiveDataOptions.isMaskingEnabled()) {
            return headerBlock;
        }

        String lineSeparator = detectLineSeparator(headerBlock);
        String[] lines = headerBlock.split("\\R", -1);
        StringBuilder maskedHeaderBlock = new StringBuilder(headerBlock.length());

        for (int i = 0; i < lines.length; i++) {
            maskedHeaderBlock.append(maskHeaderLineIfSensitive(lines[i], sensitiveDataOptions));

            if (i < lines.length - 1) {
                maskedHeaderBlock.append(lineSeparator);
            }
        }

        return maskedHeaderBlock.toString();
    }

    public static String maskBodyIfJson(String body, OptionsParamSensitiveData sensitiveDataOptions) {
        if (body == null
                || body.isEmpty()
                || sensitiveDataOptions == null
                || !sensitiveDataOptions.isMaskingEnabled()) {
            return body;
        }

        JSON json;
        try {
            json = JSONSerializer.toJSON(body);
        } catch (JSONException e) {
            return body;
        }

        if (json instanceof JSONObject obj) {
            maskJsonObject(obj, sensitiveDataOptions);
            return obj.toString();
        }

        if (json instanceof JSONArray arr) {
            maskJsonArray(arr, sensitiveDataOptions);
            return arr.toString();
        }

        return body;
    }

    private static void maskJsonObject(
            JSONObject jsonObject, OptionsParamSensitiveData sensitiveDataOptions) {
        for (Object k : jsonObject.keySet()) {
            if (!(k instanceof String key)) {
                continue;
            }

            Object value = jsonObject.get(key);
            if (value instanceof JSONObject childObj) {
                maskJsonObject(childObj, sensitiveDataOptions);
                continue;
            }

            if (value instanceof JSONArray childArr) {
                maskJsonArray(childArr, sensitiveDataOptions);
                continue;
            }

            if (value instanceof String stringValue) {
                String masked = maskIfSensitive(key, stringValue, sensitiveDataOptions);
                if (!masked.equals(stringValue)) {
                    jsonObject.put(key, masked);
                }
            } else {
                for (String sensitiveKey : sensitiveDataOptions.getSensitiveKeys()) {
                    if (sensitiveKey == null || sensitiveKey.isEmpty()) {
                        continue;
                    }
                    if (key.toLowerCase(Locale.ROOT).contains(sensitiveKey.toLowerCase(Locale.ROOT))) {
                        String masked =
                                maskIfSensitive(key, "", sensitiveDataOptions);
                        jsonObject.put(key, masked);
                        break;
                    }
                }
            }
        }
    }

    private static void maskJsonArray(JSONArray jsonArray, OptionsParamSensitiveData sensitiveDataOptions) {
        for (int i = 0; i < jsonArray.size(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONObject obj) {
                maskJsonObject(obj, sensitiveDataOptions);
            } else if (value instanceof JSONArray arr) {
                maskJsonArray(arr, sensitiveDataOptions);
            }
        }
    }

    private static String detectLineSeparator(String text) {
        if (text.contains("\r\n")) {
            return "\r\n";
        }
        if (text.contains("\n")) {
            return "\n";
        }
        if (text.contains("\r")) {
            return "\r";
        }
        return System.lineSeparator();
    }
}