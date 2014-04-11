/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;

public class ApiResponseConversionUtils {

    private static Logger logger = Logger.getLogger(ApiResponseConversionUtils.class);

    private ApiResponseConversionUtils() {
    }

    public static ApiResponseSet httpMessageToSet(int historyId, HttpMessage msg) {
        Map<String, String> map = new HashMap<>();
        map.put("id", String.valueOf(historyId));
        map.put("cookieParams", msg.getCookieParamsAsString());
        map.put("note", msg.getNote());
        map.put("requestHeader", msg.getRequestHeader().toString());
        map.put("requestBody", msg.getRequestBody().toString());
        map.put("responseHeader", msg.getResponseHeader().toString());

        if (HttpHeader.GZIP.equals(msg.getResponseHeader().getHeader(HttpHeader.CONTENT_ENCODING))) {
            // Uncompress gziped content
            try (ByteArrayInputStream bais = new ByteArrayInputStream(msg.getResponseBody().getBytes());
                 GZIPInputStream gis = new GZIPInputStream(bais);
                 InputStreamReader isr = new InputStreamReader(gis);
                 BufferedReader br = new BufferedReader(isr);) {
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                map.put("responseBody", sb.toString());
            } catch (IOException e) {
                logger.error("Unable to uncompress gzip content: " + e.getMessage(), e);
                map.put("responseBody", msg.getResponseBody().toString());
            }
        } else {
            map.put("responseBody", msg.getResponseBody().toString());
        }

        return new ApiResponseSet("message", map);
    }

}
