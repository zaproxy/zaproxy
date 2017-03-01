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
package org.zaproxy.zap.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.encoder.Base64;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.zaproxy.zap.network.HttpRequestBody;

import edu.umass.cs.benchlab.har.HarCache;
import edu.umass.cs.benchlab.har.HarContent;
import edu.umass.cs.benchlab.har.HarCookie;
import edu.umass.cs.benchlab.har.HarCookies;
import edu.umass.cs.benchlab.har.HarCreator;
import edu.umass.cs.benchlab.har.HarEntry;
import edu.umass.cs.benchlab.har.HarEntryTimings;
import edu.umass.cs.benchlab.har.HarHeader;
import edu.umass.cs.benchlab.har.HarHeaders;
import edu.umass.cs.benchlab.har.HarLog;
import edu.umass.cs.benchlab.har.HarPostData;
import edu.umass.cs.benchlab.har.HarPostDataParam;
import edu.umass.cs.benchlab.har.HarPostDataParams;
import edu.umass.cs.benchlab.har.HarQueryParam;
import edu.umass.cs.benchlab.har.HarQueryString;
import edu.umass.cs.benchlab.har.HarRequest;
import edu.umass.cs.benchlab.har.HarResponse;
import edu.umass.cs.benchlab.har.tools.HarFileWriter;

/**
 * Utility class to parse/create HTTP Archives (HAR) and do conversions between HAR Java classes and {@code HttpMessage}s
 * (request and response).
 *
 * @see <a href="http://www.softwareishard.com/blog/har-12-spec/">HTTP Archive 1.2</a>
 * @since 2.3.0
 * @see HttpMessage
 */
public final class HarUtils {

    private static final Logger LOGGER = Logger.getLogger(HarUtils.class);

    private HarUtils() {
    }

    public static HarLog createZapHarLog() {
        return new HarLog(new HarCreator(Constant.PROGRAM_NAME, Constant.PROGRAM_VERSION));
    }

    public static byte[] harLogToByteArray(HarLog harLog) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);

        HarFileWriter w = new HarFileWriter();
        w.writeHarFile(harLog, baos);

        return baos.toByteArray();
    }

    public static HttpMessage createHttpMessage(String jsonHarRequest) throws IOException {
        return createHttpMessage(createHarRequest(jsonHarRequest));
    }

    public static HarRequest createHarRequest(String jsonHarRequest) throws IOException {
        HarRequest harRequest;
        try (JsonParser jp = new JsonFactory().createJsonParser(jsonHarRequest)) {
            jp.nextToken();
            jp.nextToken();
            harRequest = new HarRequest(jp, null);
        }
        return harRequest;
    }

    public static HttpMessage createHttpMessage(HarRequest harRequest) throws HttpMalformedHeaderException {
        StringBuilder strBuilderReqHeader = new StringBuilder();

        strBuilderReqHeader.append(harRequest.getMethod())
                .append(' ')
                .append(harRequest.getUrl())
                .append(' ')
                .append(harRequest.getHttpVersion())
                .append("\r\n");

        for (HarHeader harHeader : harRequest.getHeaders().getHeaders()) {
            strBuilderReqHeader.append(harHeader.getName()).append(": ").append(harHeader.getValue()).append("\r\n");
        }
        strBuilderReqHeader.append("\r\n");

        StringBuilder strBuilderReqBody = new StringBuilder();
        final HarPostData harPostData = harRequest.getPostData();
        if (harPostData != null) {
            final String text = harPostData.getText();
            if (text != null && !text.isEmpty()) {
                strBuilderReqBody.append(harRequest.getPostData().getText());
            } else if (harPostData.getParams() != null && !harPostData.getParams().getPostDataParams().isEmpty()) {
                for (HarPostDataParam param : harRequest.getPostData().getParams().getPostDataParams()) {
                    if (strBuilderReqBody.length() > 0) {
                        strBuilderReqBody.append('&');
                    }
                    strBuilderReqBody.append(param.getName()).append('=').append(param.getValue());
                }
            }
        }

        return new HttpMessage(new HttpRequestHeader(strBuilderReqHeader.toString()), new HttpRequestBody(
                strBuilderReqBody.toString()));
    }

    public static HarEntry createHarEntry(HttpMessage httpMessage) {
        HarEntryTimings timings = new HarEntryTimings(0, 0, httpMessage.getTimeElapsedMillis());

        return new HarEntry(
                new Date(httpMessage.getTimeSentMillis()),
                httpMessage.getTimeElapsedMillis(),
                createHarRequest(httpMessage),
                createHarResponse(httpMessage),
                new HarCache(),
                timings);
    }

    public static HarRequest createHarRequest(HttpMessage httpMessage) {
        HttpRequestHeader requestHeader = httpMessage.getRequestHeader();

        HarCookies harCookies = new HarCookies();
        try {
            for (HttpCookie cookie : requestHeader.getHttpCookies()) {
                harCookies.addCookie(new HarCookie(cookie.getName(), cookie.getValue()));
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Ignoring cookies for HAR (\"request\") \"cookies\" list. Request contains invalid cookie: "
                    + e.getMessage());
        }

        HarQueryString harQueryString = new HarQueryString();
        for (HtmlParameter param : httpMessage.getUrlParams()) {
            harQueryString.addQueryParam(new HarQueryParam(param.getName(), param.getValue()));
        }

        HarPostData harPostData = null;
        HttpRequestBody requestBody = httpMessage.getRequestBody();
        if (requestBody.length() >= 0) {
            HarPostDataParams params = new HarPostDataParams();
            String text = "";

            String contentType = requestHeader.getHeader(HttpHeader.CONTENT_TYPE);
            if (contentType == null) {
                contentType = "";
                text = requestBody.toString();
            } else {
                if (StringUtils.startsWithIgnoreCase(contentType.trim(), HttpHeader.FORM_URLENCODED_CONTENT_TYPE)) {
                    for (HtmlParameter param : httpMessage.getFormParams()) {
                        params.addPostDataParam(new HarPostDataParam(param.getName(), param.getValue()));
                    }
                } else {
                    text = requestBody.toString();
                }
            }
            harPostData = new HarPostData(contentType, params, text, null);
        }

        return new HarRequest(
                requestHeader.getMethod(),
                requestHeader.getURI().toString(),
                requestHeader.getVersion(),
                harCookies,
                createHarHeaders(requestHeader),
                harQueryString,
                harPostData,
                requestHeader.toString().length(),
                httpMessage.getRequestBody().length(),
                null);
    }

    public static HarResponse createHarResponse(HttpMessage httpMessage) {
        HttpResponseHeader responseHeader = httpMessage.getResponseHeader();
        HarCookies harCookies = new HarCookies();

        long whenCreated = System.currentTimeMillis();
        for (HttpCookie cookie : responseHeader.getHttpCookies(httpMessage.getRequestHeader().getHostName())) {
            Date expires;
            if (cookie.getVersion() == 0) {
                expires = new Date(whenCreated + (cookie.getMaxAge() * 1000));
            } else {
                expires = new Date(httpMessage.getTimeSentMillis() + httpMessage.getTimeElapsedMillis()
                        + (cookie.getMaxAge() * 1000));
            }

            harCookies.addCookie(new HarCookie(
                    cookie.getName(),
                    cookie.getValue(),
                    cookie.getPath(),
                    cookie.getDomain(),
                    expires,
                    cookie.isHttpOnly(),
                    cookie.getSecure(),
                    null));
        }

        String text = null;
        String encoding = null;
        String contentType = responseHeader.getHeader(HttpHeader.CONTENT_TYPE);
        if (contentType == null) {
            contentType = "";
        } else if (!contentType.isEmpty()) {
            String lcContentType = contentType.toLowerCase(Locale.ROOT);
            final int pos = lcContentType.indexOf(';');
            if (pos != -1) {
                lcContentType = lcContentType.substring(0, pos).trim();
            }

            if (!lcContentType.startsWith("text")) {
                encoding = "base64";
                text = Base64.encodeBytes(httpMessage.getResponseBody().getBytes());
            } else {
                text = httpMessage.getResponseBody().toString();
            }
        }

        HarContent harContent = new HarContent(httpMessage.getResponseBody().length(), 0, contentType, text, encoding, null);

        String redirectUrl = responseHeader.getHeader(HttpHeader.LOCATION);

        return new HarResponse(
                responseHeader.getStatusCode(),
                responseHeader.getReasonPhrase(),
                responseHeader.getVersion(),
                harCookies,
                createHarHeaders(responseHeader),
                harContent,
                redirectUrl == null ? "" : redirectUrl,
                responseHeader.toString().length(),
                httpMessage.getResponseBody().length(),
                null);
    }

    public static HarHeaders createHarHeaders(HttpHeader httpHeader) {
        HarHeaders harHeaders = new HarHeaders();
        List<HttpHeaderField> headers = httpHeader.getHeaders();
        for (HttpHeaderField headerField : headers) {
            harHeaders.addHeader(new HarHeader(headerField.getName(), headerField.getValue()));
        }
        return harHeaders;
    }
}
