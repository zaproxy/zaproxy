/*
 * Created on 22 Jun 2004.
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2006 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/10/29 Fixed cookie parsing
// ZAP: 2012/03/15 Changed to use the classes HttpRequestBody and HttpResponseBody.
// Changed to use the byte[] body. Changed to use the class StringBuilder instead
// of StringBuffer. Reworked some methods.
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2012/06/11 Added method boolean isWebSocketUpgrade()
// ZAP: 2012/07/02 Implement Message interface for more flexibility.
// ZAP: 2012/06/24 Added method to add Cookies of type java.net.HttpCookie to request header
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/08/09 Added HttpSession field
// ZAP: 2012/10/08 Issue 391: Performance improvements
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/04/08 Issue 605: Force intercepts via header
// ZAP: 2013/07/25 Added support for sending the message from the perspective of a User
// ZAP: 2013/09/26 Issue 716: ZAP flags its own HTTP responses
// ZAP: 2013/11/16 Issue 867: HttpMessage#getFormParams should return an empty TreeSet if
// the request body is not "x-www-form-urlencoded"
// ZAP: 2014/01/06 Issue 965: Support 'single page' apps and 'non standard' parameter separators
// ZAP: 2014/03/23 Tidy up, do not allow to set null request/response headers/bodies.
// ZAP: 2014/03/28 Issue 1127: 	Allow scripts to generate breaks
// ZAP: 2014/06/16 Issue 1217: Table format does not display information when charset is
// present in Content-Type header
// ZAP: 2015/02/09 Fix NullPointerException in equals(Object) when comparing with empty messages
// ZAP: 2015/08/07 Issue 1768: Update to use a more recent default user agent
// ZAP: 2015/08/19 Deprecate/change methods with unused parameters
// ZAP: 2016/05/31 Implement hashCode()
// ZAP: 2017/02/01 Set whether or not the charset should be determined when setting a (String)
// response.
// ZAP: 2017/08/23 queryEquals correct comparison and add JavaDoc. equalType update JavaDoc.
// ZAP: 2018/03/13 Added toEventData()
// ZAP: 2018/04/04 Add a copy constructor.
// ZAP: 2018/08/10 Use non-deprecated HttpRequestHeader constructor (Issue 4846).
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/12/09 Address deprecation of getHeaders(String) Vector method.
// ZAP: 2020/07/31 Tidy up parameter methods
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2020/12/09 Handle content encodings in request/response bodies.
// ZAP: 2021/04/01 Detect WebSocket upgrade messages having multiple Connection directives
// ZAP: 2021/05/11 Fixed conversion of Request Method to/from CONNECT
// ZAP: 2021/05/14 Add missing override annotation.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
// ZAP: 2023/01/10 Tidy up logger.
package org.parosproxy.paros.network;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httpsessions.HttpSession;
import org.zaproxy.zap.model.NameValuePair;
import org.zaproxy.zap.network.HttpEncoding;
import org.zaproxy.zap.network.HttpEncodingDeflate;
import org.zaproxy.zap.network.HttpEncodingGzip;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.network.HttpResponseBody;
import org.zaproxy.zap.users.User;

/**
 * Representation of a HTTP message request (header and body) and response (header and body) pair.
 */
public class HttpMessage implements Message {

    public static final String EVENT_DATA_URI = "uri";
    public static final String EVENT_DATA_REQUEST_HEADER = "requestHeader";
    public static final String EVENT_DATA_REQUEST_BODY = "requestBody";
    public static final String EVENT_DATA_RESPONSE_HEADER = "responseHeader";
    public static final String EVENT_DATA_RESPONSE_BODY = "responseBody";

    public static final String MESSAGE_TYPE = "HTTP";

    private HttpRequestHeader mReqHeader = new HttpRequestHeader();
    private HttpRequestBody mReqBody = new HttpRequestBody();
    private HttpResponseHeader mResHeader = new HttpResponseHeader();
    private HttpResponseBody mResBody = new HttpResponseBody();
    private Object userObject = null;
    private int timeElapsed = 0;
    private long timeSent = 0;
    // private String tag = "";
    // ZAP: Added note to HttpMessage
    private String note = "";
    // ZAP: Added historyRef
    private HistoryReference historyRef = null;
    // ZAP: Added logger
    private static final Logger LOGGER = LogManager.getLogger(HttpMessage.class);
    // ZAP: Added HttpSession
    private HttpSession httpSession = null;
    // ZAP: Added support for requesting the message to be sent as a particular User
    private User requestUser;
    // Can be set by scripts to force a break
    private boolean forceIntercept = false;

    /**
     * Flag that indicates if the response has been received or not from the target host.
     *
     * <p>Default is {@code false}.
     */
    private boolean responseFromTargetHost = false;

    public HistoryReference getHistoryRef() {
        return historyRef;
    }

    public void setHistoryRef(HistoryReference historyRef) {
        this.historyRef = historyRef;
    }

    /**
     * Gets the http session associated with this message.
     *
     * @return the http session
     */
    public HttpSession getHttpSession() {
        return this.httpSession;
    }

    /**
     * Sets the http session associated with this message.
     *
     * @param session the new http session
     */
    public void setHttpSession(HttpSession session) {
        this.httpSession = session;
    }

    /** Constructor for a empty HTTP message. */
    public HttpMessage() {}

    /**
     * Constructs a {@code HttpMessage} with a HTTP/1.1 GET request to the given URI.
     *
     * <p>The following headers are automatically added:
     *
     * <ul>
     *   <li>{@code Host}, with the domain and port from the given URI.
     *   <li>{@code User-Agent}, using the {@link HttpRequestHeader#getDefaultUserAgent()}.
     *   <li>{@code Pragma: no-cache}
     *   <li>{@code Cache-Control: no-cache}, if version is HTTP/1.1
     *   <li>{@code Content-Type: application/x-www-form-urlencoded}, if the method is POST or PUT.
     * </ul>
     *
     * @param uri the request target.
     * @throws HttpMalformedHeaderException if the resulting HTTP header is malformed.
     */
    public HttpMessage(URI uri) throws HttpMalformedHeaderException {
        setRequestHeader(new HttpRequestHeader(HttpRequestHeader.GET, uri, HttpHeader.HTTP11));
    }

    /**
     * Constructs a {@code HttpMessage} with a HTTP/1.1 GET request to the given URI.
     *
     * <p>The following headers are automatically added:
     *
     * <ul>
     *   <li>{@code Host}, with the domain and port from the given URI.
     *   <li>{@code User-Agent}, using the {@link HttpRequestHeader#getDefaultUserAgent()}.
     *   <li>{@code Pragma: no-cache}
     *   <li>{@code Cache-Control: no-cache}, if version is HTTP/1.1
     *   <li>{@code Content-Type: application/x-www-form-urlencoded}, if the method is POST or PUT.
     * </ul>
     *
     * @param uri the request target.
     * @param params unused.
     * @throws HttpMalformedHeaderException if the resulting HTTP header is malformed.
     * @deprecated (2.8.0) Use {@link #HttpMessage(URI)} instead.
     * @since 2.4.2
     */
    @Deprecated
    public HttpMessage(URI uri, ConnectionParam params) throws HttpMalformedHeaderException {
        this(uri);
    }

    /**
     * Constructs an HTTP message with the given request header.
     *
     * @param reqHeader the request header
     * @throws IllegalArgumentException if the parameter {@code reqHeader} is {@code null}.
     */
    public HttpMessage(HttpRequestHeader reqHeader) {
        setRequestHeader(reqHeader);
    }

    /**
     * Constructs an HTTP message with the given request header and request body.
     *
     * @param reqHeader the request header
     * @param reqBody the request body
     * @throws IllegalArgumentException if the parameter {@code reqHeader} or {@code reqBody} are
     *     {@code null}.
     */
    public HttpMessage(HttpRequestHeader reqHeader, HttpRequestBody reqBody) {
        setRequestHeader(reqHeader);
        setRequestBody(reqBody);
    }

    /**
     * Constructor for a HTTP message with given request and response pair.
     *
     * @param reqHeader the request header
     * @param reqBody the request body
     * @param resHeader the response header
     * @param resBody the response body
     * @throws IllegalArgumentException if one of the parameters is {@code null}.
     */
    public HttpMessage(
            HttpRequestHeader reqHeader,
            HttpRequestBody reqBody,
            HttpResponseHeader resHeader,
            HttpResponseBody resBody) {
        setRequestHeader(reqHeader);
        setRequestBody(reqBody);
        setResponseHeader(resHeader);
        setResponseBody(resBody);
    }

    public HttpMessage(String reqHeader, byte[] reqBody, String resHeader, byte[] resBody)
            throws HttpMalformedHeaderException {
        setRequestHeader(reqHeader);
        setRequestBody(reqBody);
        if (resHeader != null && !resHeader.equals("")) {
            setResponseHeader(resHeader);
            setResponseBody(resBody);
        }
    }

    /**
     * Constructs a {@code HttpMessage} from the given message.
     *
     * <p>All the {@code HttpMessage} state is copied, except for the following which are the same:
     *
     * <ul>
     *   <li>{@link #getUserObject()}
     *   <li>{@link #getHistoryRef()}
     *   <li>{@link #getHttpSession()}
     *   <li>{@link #getRequestingUser()}
     * </ul>
     *
     * @param message the message to copy.
     * @since 2.8.0
     */
    public HttpMessage(HttpMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("The parameter message must not be null.");
        }

        message.copyRequestInto(this);
        message.copyResponseInto(this);

        setUserObject(message.getUserObject());
        setTimeSentMillis(message.getTimeSentMillis());
        setTimeElapsedMillis(message.getTimeElapsedMillis());
        setNote(message.getNote());
        setHistoryRef(message.getHistoryRef());
        setHttpSession(message.getHttpSession());
        setRequestingUser(message.getRequestingUser());
        setForceIntercept(message.isForceIntercept());
        setResponseFromTargetHost(message.isResponseFromTargetHost());
    }

    /**
     * Gets the request header of this message.
     *
     * @return the request header, never {@code null}
     */
    public HttpRequestHeader getRequestHeader() {
        return mReqHeader;
    }

    /**
     * Sets the request header of this message.
     *
     * @param reqHeader the new request header
     * @throws IllegalArgumentException if parameter {@code reqHeader} is {@code null}.
     */
    public void setRequestHeader(HttpRequestHeader reqHeader) {
        if (reqHeader == null) {
            throw new IllegalArgumentException("The parameter reqHeader must not be null.");
        }
        mReqHeader = reqHeader;
    }

    /**
     * Gets the response header of this message.
     *
     * <p>To know if a response has been set call the method {@code HttpResponseHeader#isEmpty()} on
     * the returned response header. The response header is initially empty.
     *
     * @return the response header, never {@code null}
     * @see HttpResponseHeader#isEmpty()
     */
    public HttpResponseHeader getResponseHeader() {
        return mResHeader;
    }

    /**
     * Sets the response header of this message.
     *
     * @param resHeader the new response header
     * @throws IllegalArgumentException if parameter {@code resHeader} is {@code null}.
     */
    public void setResponseHeader(HttpResponseHeader resHeader) {
        if (resHeader == null) {
            throw new IllegalArgumentException("The parameter resHeader must not be null.");
        }
        mResHeader = resHeader;
    }

    /**
     * Gets the request body of this message.
     *
     * @return the request body, never {@code null}
     */
    public HttpRequestBody getRequestBody() {
        return mReqBody;
    }

    /**
     * Sets the request body of this message.
     *
     * <p><strong>Note:</strong> No encodings are set to the request body to match the header.
     *
     * @param reqBody the new request body
     * @throws IllegalArgumentException if parameter {@code reqBody} is {@code null}.
     */
    public void setRequestBody(HttpRequestBody reqBody) {
        if (reqBody == null) {
            throw new IllegalArgumentException("The parameter reqBody must not be null.");
        }
        mReqBody = reqBody;
    }

    /**
     * Gets the response body of this message.
     *
     * @return the response body, never {@code null}
     */
    public HttpResponseBody getResponseBody() {
        return mResBody;
    }

    /**
     * Sets the response body of this message.
     *
     * <p><strong>Note:</strong> No encodings are set to the response body to match the header.
     *
     * @param resBody the new response body
     * @throws IllegalArgumentException if parameter {@code resBody} is {@code null}.
     */
    public void setResponseBody(HttpResponseBody resBody) {
        if (resBody == null) {
            throw new IllegalArgumentException("The parameter resBody must not be null.");
        }
        mResBody = resBody;
        getResponseBody().setCharset(getResponseHeader().getCharset());
    }

    /**
     * Sets the given string as the request header.
     *
     * <p><strong>Note:</strong> No encodings are set to the request body to match the header.
     *
     * @param reqHeader the new request header.
     * @throws HttpMalformedHeaderException if the given header is malformed.
     * @see #setContentEncodings(HttpHeader, HttpBody)
     */
    public void setRequestHeader(String reqHeader) throws HttpMalformedHeaderException {
        HttpRequestHeader newHeader = new HttpRequestHeader(reqHeader);
        setRequestHeader(newHeader);
    }

    /**
     * Sets the given string as the response header.
     *
     * <p><strong>Note:</strong> No encodings are set to the response body to match the header.
     *
     * @param resHeader the new response header.
     * @throws HttpMalformedHeaderException if the given header is malformed.
     * @see #setContentEncodings(HttpHeader, HttpBody)
     */
    public void setResponseHeader(String resHeader) throws HttpMalformedHeaderException {
        HttpResponseHeader newHeader = new HttpResponseHeader(resHeader);
        setResponseHeader(newHeader);
    }

    /**
     * Sets the content encodings defined in the header into the body.
     *
     * <p><strong>Note:</strong> Supports only {@code gzip} and {@code deflate}.
     *
     * @param header the header.
     * @param body the body.
     */
    public static void setContentEncodings(HttpHeader header, HttpBody body) {
        String encoding = header.getHeader(HttpHeader.CONTENT_ENCODING);
        if (encoding == null || encoding.isEmpty()) {
            body.setContentEncodings(Collections.emptyList());
            return;
        }

        List<HttpEncoding> encodings = new ArrayList<>(1);
        if (encoding.contains(HttpHeader.DEFLATE)) {
            encodings.add(HttpEncodingDeflate.getSingleton());
        } else if (encoding.contains(HttpHeader.GZIP)) {
            encodings.add(HttpEncodingGzip.getSingleton());
        }

        body.setContentEncodings(encodings);
    }

    /**
     * Sets the given string body as the request body.
     *
     * <p>The defined request header content encodings are set to the body, the string body will be
     * encoded accordingly.
     *
     * @param body the new body.
     * @see HttpBody#setContentEncodings(List)
     */
    public void setRequestBody(String body) {
        setContentEncodings(getRequestHeader(), getRequestBody());

        getRequestBody().setCharset(getRequestHeader().getCharset());
        getRequestBody().setBody(body);
    }

    /**
     * Sets the given byte body as the request body.
     *
     * <p>The defined request header content encodings are set to the body, the byte body is assumed
     * to be properly encoded.
     *
     * @param body the new body.
     * @see HttpBody#setContentEncodings(List)
     */
    public void setRequestBody(byte[] body) {
        getRequestBody().setBody(body);
        getRequestBody().setCharset(getRequestHeader().getCharset());

        setContentEncodings(getRequestHeader(), getRequestBody());
    }

    /**
     * Sets the given string body as the response body.
     *
     * <p>The defined response header content encodings are set to the body, the string body will be
     * encoded accordingly.
     *
     * @param body the new body.
     * @see HttpBody#setContentEncodings(List)
     */
    public void setResponseBody(String body) {
        setContentEncodings(getResponseHeader(), getResponseBody());

        getResponseBody().setCharset(getResponseHeader().getCharset());
        getResponseBody().setDetermineCharset(getResponseHeader().isText());
        getResponseBody().setBody(body);
    }

    /**
     * Sets the given byte body as the response body.
     *
     * <p>The defined response header content encodings are set to the body, the byte body is
     * assumed to be properly encoded.
     *
     * @param body the new body.
     * @see HttpBody#setContentEncodings(List)
     */
    public void setResponseBody(byte[] body) {
        getResponseBody().setBody(body);
        getResponseBody().setCharset(getResponseHeader().getCharset());

        setContentEncodings(getResponseHeader(), getResponseBody());
    }

    /**
     * Compare if 2 message is the same. 2 messages are the same if: Host, port, path and query
     * param and VALUEs are the same. For POST request, the body must be the same.
     *
     * @param object
     * @return
     */
    @Override
    public boolean equals(Object object) {

        if (!(object instanceof HttpMessage)) {
            return false;
        }

        HttpMessage msg = (HttpMessage) object;
        boolean result = false;

        // compare method
        if (!this.getRequestHeader()
                .getMethod()
                .equalsIgnoreCase(msg.getRequestHeader().getMethod())) {
            return false;
        }

        // compare host, port and URI
        URI uri1 = this.getRequestHeader().getURI();
        URI uri2 = msg.getRequestHeader().getURI();

        if (uri1 == null) {
            if (uri2 != null) {
                return false;
            }
            return true;
        } else if (uri2 == null) {
            return false;
        }

        try {
            if (uri1.getHost() == null
                    || uri2.getHost() == null
                    || !uri1.getHost().equalsIgnoreCase(uri2.getHost())) {
                return false;
            }

            if (uri1.getPort() != uri2.getPort()) {
                return false;
            }

            String pathQuery1 = uri1.getPathQuery();
            String pathQuery2 = uri2.getPathQuery();

            if (pathQuery1 == null && pathQuery2 == null) {
                return true;
            } else if (pathQuery1 != null && pathQuery2 != null) {
                return pathQuery1.equalsIgnoreCase(pathQuery2);
            } else if (pathQuery1 == null || pathQuery2 == null) {
                return false;
            }

            if (this.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
                return this.getRequestBody().equals(msg.getRequestBody());
            }

            result = true;

        } catch (URIException e) {
            try {
                result =
                        this.getRequestHeader()
                                .getURI()
                                .toString()
                                .equalsIgnoreCase(msg.getRequestHeader().getURI().toString());
            } catch (Exception e1) {
                // ZAP: log error
                LOGGER.error(e.getMessage(), e);
            }
        }

        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result + getRequestHeader().getMethod().toLowerCase(Locale.ROOT).hashCode();
        URI uri = getRequestHeader().getURI();
        if (uri != null) {
            result = prime * result + uri.getPort();
            try {
                result =
                        prime * result
                                + (uri.getRawHost() == null
                                        ? 0
                                        : uri.getHost().toLowerCase(Locale.ROOT).hashCode());
            } catch (URIException e) {
                LOGGER.error("Failed to obtain the host for hashCode calculation: {}", uri, e);
            }
            result =
                    prime * result
                            + ((uri.getRawPathQuery() == null)
                                    ? 0
                                    : uri.getEscapedPathQuery()
                                            .toLowerCase(Locale.ROOT)
                                            .hashCode());
        }

        if (getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
            result = prime * result + getRequestBody().hashCode();
        }

        return result;
    }

    /**
     * Compares this {@code HttpMessage} against another. Messages are equal type if the host, port,
     * path and parameter names are equal. Even though the query values may differ. For POST this
     * assumes x-www-form-urlencoded, for other types (such as JSON) this means that parameter names
     * and values (the full request body) could be included.
     *
     * @param msg the message against which this {@code HttpMessage} is being compared.
     * @return {@code true} if the messages are considered equal, {@code false} otherwise
     */
    public boolean equalType(HttpMessage msg) {
        boolean result = false;

        // compare method
        if (!this.getRequestHeader()
                .getMethod()
                .equalsIgnoreCase(msg.getRequestHeader().getMethod())) {
            return false;
        }

        // compare host, port and URI
        URI uri1 = this.getRequestHeader().getURI();
        URI uri2 = msg.getRequestHeader().getURI();

        try {
            if (uri1.getHost() == null
                    || uri2.getHost() == null
                    || !uri1.getHost().equalsIgnoreCase(uri2.getHost())) {
                return false;
            }

            if (uri1.getPort() != uri2.getPort()) {
                return false;
            }

            String path1 = uri1.getPath();
            String path2 = uri2.getPath();

            if (path1 == null && path2 == null) {
                return true;
            }

            if (path1 != null && path2 != null && !path1.equalsIgnoreCase(path2)) {
                return false;
            } else {
                if (path1 == null || path2 == null) {
                    return false;
                }
            }

            if (!queryEquals(msg)) {
                return false;
            }

            result = true;

        } catch (URIException e) {
            // ZAP: log error
            LOGGER.error(e.getMessage(), e);
        }

        return result;
    }

    /**
     * Compares the parameter names used in GET and POST messages. For POST this assumes
     * x-www-form-urlencoded, for other types (such as JSON) this means that parameter names and
     * values (the full request body) could be included.
     *
     * @param msg the message against which this {@code HttpMessage} parameter names are being
     *     compared.
     * @return {@code true} if the set of parameter names are considered equal, {@code false}
     *     otherwise
     */
    private boolean queryEquals(HttpMessage msg) {
        boolean result = false;

        SortedSet<String> set1 = null;
        SortedSet<String> set2 = null;

        set1 = getParamNameSet(HtmlParameter.Type.url);
        set2 = msg.getParamNameSet(HtmlParameter.Type.url);

        if (!set1.equals(set2)) {
            return false;
        }

        // compare here if this is a POST
        // the POST body part must also be the same set
        if (getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {

            set1 = getParamNameSet(HtmlParameter.Type.form);
            set2 = msg.getParamNameSet(HtmlParameter.Type.form);

            if (!set1.equals(set2)) {
                return false;
            }
        }

        result = true;

        return result;
    }

    /**
     * Returns the names of the parameters of the given {@code type}. As a Set is used no names will
     * be duplicated.
     *
     * @param type the type of the parameters that will be extracted from the message
     * @return a {@code TreeSet} with the names of the parameters of the given {@code type}, never
     *     {@code null}
     * @since 2.4.2
     */
    public TreeSet<String> getParamNameSet(HtmlParameter.Type type) {
        TreeSet<String> set = new TreeSet<>();
        List<NameValuePair> paramList = Model.getSingleton().getSession().getParameters(this, type);

        for (NameValuePair nvp : paramList) {
            set.add(nvp.getName());
        }
        return set;
    }

    /**
     * Returns the names of the parameters of the given {@code type} in a List. The List can return
     * duplicated names.
     *
     * @param type the type of the parameters that will be extracted from the message
     * @return a {@code List} with the names of the parameters of the given {@code type}, never
     *     {@code null}
     * @since 2.10.0
     */
    public List<String> getParameterNames(HtmlParameter.Type type) {
        List<String> list = new ArrayList<>();
        Model.getSingleton()
                .getSession()
                .getParameters(this, type)
                .forEach((nvp) -> list.add(nvp.getName()));
        return list;
    }

    private TreeSet<HtmlParameter> getParamsSet(HtmlParameter.Type type) {
        TreeSet<HtmlParameter> set = new TreeSet<>();
        List<NameValuePair> paramList = Model.getSingleton().getSession().getParameters(this, type);

        for (NameValuePair nvp : paramList) {
            set.add(new HtmlParameter(type, nvp.getName(), nvp.getValue()));
        }
        return set;
    }

    /**
     * Returns the parameters of the given {@code type} in a List. The List can return duplicated
     * parameter names.
     *
     * @param type the type of the parameters that will be extracted from the message
     * @return a {@code List} with the parameters of the given {@code type}, never {@code null}
     * @since 2.10.0
     */
    public List<HtmlParameter> getParameters(HtmlParameter.Type type) {
        List<HtmlParameter> list = new ArrayList<>();
        Model.getSingleton()
                .getSession()
                .getParameters(this, type)
                .forEach((nvp) -> list.add(new HtmlParameter(type, nvp.getName(), nvp.getValue())));
        return list;
    }

    // ZAP: Added getParamNames
    public String[] getParamNames() {
        Vector<String> v = new Vector<>();
        // Get the params names from the query
        SortedSet<String> pns = this.getParamNameSet(HtmlParameter.Type.url);
        Iterator<String> iterator = pns.iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            if (name != null) {
                v.add(name);
            }
        }
        if (getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
            // Get the param names from the POST
            pns = this.getParamNameSet(HtmlParameter.Type.form);
            iterator = pns.iterator();
            while (iterator.hasNext()) {
                String name = iterator.next();
                if (name != null) {
                    v.add(name);
                }
            }
        }
        String[] a = new String[v.size()];
        v.toArray(a);
        return a;
    }

    // ZAP: Added getUrlParams
    public TreeSet<HtmlParameter> getUrlParams() {
        return this.getParamsSet(HtmlParameter.Type.url);
    }

    // ZAP: Added getFormParams
    public TreeSet<HtmlParameter> getFormParams() {
        final String contentType = mReqHeader.getHeader(HttpRequestHeader.CONTENT_TYPE);
        if (contentType == null
                || !StringUtils.startsWithIgnoreCase(
                        contentType.trim(), HttpHeader.FORM_URLENCODED_CONTENT_TYPE)) {
            return new TreeSet<>();
        }
        return this.getParamsSet(HtmlParameter.Type.form);
    }

    public void setCookieParamsAsString(String data) {
        this.getRequestHeader().setHeader(HttpHeader.COOKIE, data);
    }

    public String getCookieParamsAsString() {
        List<String> cookies = new LinkedList<>();
        if (!this.getRequestHeader().isEmpty()) {
            cookies.addAll(this.getRequestHeader().getHeaderValues(HttpHeader.COOKIE));
        }
        if (!this.getResponseHeader().isEmpty()) {
            cookies.addAll(this.getResponseHeader().getHeaderValues(HttpHeader.SET_COOKIE));
            cookies.addAll(this.getResponseHeader().getHeaderValues(HttpHeader.SET_COOKIE2));
        }

        // Fix error requesting cookies, but there are none
        if (cookies.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String header : cookies) {
            sb.append(header);
        }
        return sb.toString();
    }

    // ZAP: Added getCookieParams
    public TreeSet<HtmlParameter> getCookieParams() {
        TreeSet<HtmlParameter> set = new TreeSet<>();

        if (!this.getRequestHeader().isEmpty()) {
            set.addAll(this.getRequestHeader().getCookieParams());
        }
        if (!this.getResponseHeader().isEmpty()) {
            set.addAll(this.getResponseHeader().getCookieParams());
        }

        return set;
    }

    /** @return Returns the userObject. */
    public Object getUserObject() {
        return userObject;
    }
    /** @param userObject The userObject to set. */
    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }

    /**
     * Clones this message.
     *
     * <p>It returns a new {@code HttpMessage} with a copy of the request/response headers and
     * bodies, no other state is copied.
     *
     * @return a new {@code HttpMessage} with the same (contents) request/response headers and
     *     bodies as this one.
     * @see #HttpMessage(HttpMessage)
     */
    public HttpMessage cloneAll() {
        HttpMessage newMsg = cloneRequest();
        copyResponseInto(newMsg);
        return newMsg;
    }

    private void copyResponseInto(HttpMessage newMsg) {
        if (!this.getResponseHeader().isEmpty()) {
            try {
                newMsg.getResponseHeader().setMessage(this.getResponseHeader().toString());
            } catch (HttpMalformedHeaderException e) {
            }
            newMsg.setResponseBody(this.getResponseBody().getBytes());
        }
    }

    /**
     * Clones the request of this message.
     *
     * <p>It returns a new {@code HttpMessage} with a copy of the request header and body, no other
     * state is copied.
     *
     * @return a new {@code HttpMessage} with the same (contents) request header and body as this
     *     one.
     * @see #HttpMessage(HttpMessage)
     */
    public HttpMessage cloneRequest() {
        HttpMessage newMsg = new HttpMessage();
        copyRequestInto(newMsg);
        return newMsg;
    }

    private void copyRequestInto(HttpMessage newMsg) {
        if (!this.getRequestHeader().isEmpty()) {
            try {
                newMsg.getRequestHeader().setMessage(this.getRequestHeader().toString());
            } catch (HttpMalformedHeaderException e) {
                LOGGER.error(e.getMessage(), e);
            }
            newMsg.setRequestBody(this.getRequestBody().getBytes());
        }
    }
    /**
     * @return Get the elapsed time (time difference) between the request is sent and all response
     *     is received. In millis. The value is zero if the response is not received.
     */
    public int getTimeElapsedMillis() {
        return timeElapsed;
    }

    /**
     * Set the elapsed time (time difference) between the request is sent and all response is
     * received. In millis.
     *
     * @param timeElapsed
     */
    public void setTimeElapsedMillis(int timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    /**
     * Get the starting time when the request is going to be sent. This is the
     * System.currentTimeMillis before the message is sent. The value is zero if the request is not
     * sent yet.
     */
    public long getTimeSentMillis() {
        return timeSent;
    }
    /**
     * Set the time when the request is sent.
     *
     * @param timeSent The timeSent to set.
     */
    public void setTimeSentMillis(long timeSent) {
        this.timeSent = timeSent;
    }

    /** @return Returns the note. */
    public String getNote() {
        return note;
    }

    /** @param note The note to set. */
    public void setNote(String note) {
        this.note = note;
    }

    public void mutateHttpMethod(String method) {
        try {
            URI uri = getRequestHeader().getURI();
            String body = getRequestBody().toString();
            String prevMethod = getRequestHeader().getMethod();

            if (prevMethod.equalsIgnoreCase(method)) {
                return;
            }
            if (prevMethod.equals(HttpRequestHeader.POST)) {
                // Was POST, move all params onto the URL
                if (body != null && body.length() > 0) {
                    StringBuilder sb = new StringBuilder();
                    if (uri.getQuery() != null) {
                        sb.append(uri.getQuery());
                    }

                    String[] params = body.split("&");
                    for (String param : params) {
                        if (sb.length() > 0) {
                            sb.append('&');
                        }
                        String[] nv = param.split("=");
                        if (nv.length == 1) {
                            // This effectively strips out the equals if theres
                            // no value
                            sb.append(nv[0]);
                        } else {
                            sb.append(param);
                        }
                    }
                    uri.setQuery(sb.toString());
                }
                // Clear the body
                body = "";
                // Remove Content-Type if present
                getRequestHeader().setHeader(HttpRequestHeader.CONTENT_TYPE, null);

            } else if (method.equals(HttpRequestHeader.POST)) {
                // To be a port, move all URL query params into the body
                String query = uri.getQuery();
                if (query != null) {
                    StringBuilder sb = new StringBuilder();
                    String[] params = query.split("&");
                    for (String param : params) {
                        if (sb.length() > 0) {
                            sb.append('&');
                        }
                        sb.append(param);
                        String[] nv = param.split("=");
                        if (nv.length == 1) {
                            // Cope with URL params with no values e.g.
                            // http://www.example.com/test?key
                            sb.append('=');
                        }
                    }
                    getRequestHeader()
                            .setHeader(
                                    HttpRequestHeader.CONTENT_TYPE,
                                    HttpRequestHeader.FORM_URLENCODED_CONTENT_TYPE);
                    body = sb.toString();
                    uri.setQuery(null);
                }
            }
            if (prevMethod.equalsIgnoreCase(HttpRequestHeader.CONNECT)) {
                String scheme;
                if (getRequestHeader().getHostPort() == 443) {
                    scheme = "https://";
                } else {
                    scheme = "http://";
                }
                uri = new URI(scheme + uri, true);
            } else if (method.equals(HttpRequestHeader.CONNECT)) {
                uri = URI.fromAuthority(uri.getAuthority());
            }
            getRequestHeader()
                    .setMessage(
                            method
                                    + " "
                                    + uri
                                    + " "
                                    + getRequestHeader().getVersion()
                                    + "\r\n"
                                    + getRequestHeader().getHeadersAsString());
            getRequestBody().setBody(body);
        } catch (HttpMalformedHeaderException e) {
            // Ignore?
            LOGGER.error(e.getMessage(), e);
        } catch (URIException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    // Construct new POST Body from parameter in the postParams argument
    // in the Request Body
    public void setFormParams(TreeSet<HtmlParameter> postParams) {
        // TODO: Maybe update content length, etc.?
        mReqBody.setFormParams(postParams);
    }

    // Construct new URL from get Request, based on the getParams argument
    // in the Request Header
    public void setGetParams(TreeSet<HtmlParameter> getParams) {
        mReqHeader.setGetParams(getParams);
    }

    // Rewrite cookie line in the Request Header,
    // based on values in cookieParams
    public void setCookieParams(TreeSet<HtmlParameter> cookieParams) {
        mReqHeader.setCookieParams(cookieParams);
    }

    /**
     * ZAP: New method checking for connection upgrade.
     *
     * @return True if this connection should be upgraded to WebSockets.
     */
    public boolean isWebSocketUpgrade() {
        HttpHeader messageHeader = getResponseHeader();
        if (messageHeader.isEmpty()) {
            messageHeader = getRequestHeader();
        }

        if (!messageHeader.isEmpty()) {
            String connectionHeader = messageHeader.getHeader("connection");
            String upgradeHeader = messageHeader.getHeader("upgrade");

            if (upgradeHeader != null && upgradeHeader.equalsIgnoreCase("websocket")) {
                if (connectionHeader != null) {
                    for (String directive : connectionHeader.split(",")) {
                        if (directive.trim().equalsIgnoreCase("upgrade")) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    // Rewrite cookie line in the Request Header,
    // based on values in cookies
    public void setCookies(List<HttpCookie> cookies) {
        mReqHeader.setCookies(cookies);
    }

    @Override
    public boolean isInScope() {
        return Model.getSingleton()
                .getSession()
                .isInScope(this.getRequestHeader().getURI().toString());
    }

    /**
     * ZAP: New method checking if connection is a Server-Sent Events stream.
     *
     * @return
     */
    public boolean isEventStream() {
        boolean isEventStream = false;
        if (!getResponseHeader().isEmpty()) {
            isEventStream = getResponseHeader().hasContentType("text/event-stream");
        } else {
            // response not available
            // is request for event-stream?
            String acceptHeader = getRequestHeader().getHeader("Accept");
            if (acceptHeader != null && acceptHeader.equals("text/event-stream")) {
                // request is for an SSE stream
                isEventStream = true;
            }
        }

        return isEventStream;
    }

    @Override
    public boolean isForceIntercept() {
        String vals = this.getRequestHeader().getHeader(HttpHeader.X_SECURITY_PROXY);
        if (vals != null) {
            for (String val : vals.split(",")) {
                if (HttpHeader.SEC_PROXY_INTERCEPT.equalsIgnoreCase(val.trim())) {
                    // The browser told us to do it Your Honour
                    return true;
                }
            }
        }
        return forceIntercept;
    }

    public void setForceIntercept(boolean force) {
        this.forceIntercept = force;
    }

    /**
     * Gets the request user.
     *
     * @return the request user
     */
    public User getRequestingUser() {
        return requestUser;
    }

    /**
     * Sets the requesting user. When sending the message, if a requesting user has been set, the
     * message will be modified so that it will be sent as from the point of view of this particular
     * user.
     *
     * @param requestUser the new request user
     */
    public void setRequestingUser(User requestUser) {
        this.requestUser = requestUser;
    }

    /**
     * Tells whether or not the response has been received from the target host.
     *
     * <p><strong>Note:</strong> No distinction is done between responses from intermediate proxy
     * servers (if any) and the target host.
     *
     * @return {@code true} if the response has been received from the target host, {@code false}
     *     otherwise.
     */
    public boolean isResponseFromTargetHost() {
        return this.responseFromTargetHost;
    }

    /**
     * Sets if the response has been received or not from the target host.
     *
     * @param responseFromTargetHost {@code true} if the response has been received from the target
     *     host, {@code false} otherwise.
     */
    public void setResponseFromTargetHost(final boolean responseFromTargetHost) {
        this.responseFromTargetHost = responseFromTargetHost;
    }

    /**
     * Returns a map of data suitable for including in an {@link Event}
     *
     * @since 2.8.0
     */
    @Override
    public Map<String, String> toEventData() {
        Map<String, String> map = new HashMap<>();
        map.put(EVENT_DATA_URI, getRequestHeader().getURI().toString());
        map.put(EVENT_DATA_REQUEST_HEADER, getRequestHeader().toString());
        map.put(EVENT_DATA_REQUEST_BODY, getRequestBody().toString());
        if (!getResponseHeader().isEmpty()) {
            map.put(EVENT_DATA_RESPONSE_HEADER, getResponseHeader().toString());
            map.put(EVENT_DATA_RESPONSE_BODY, getResponseBody().toString());
        }
        return map;
    }

    /**
     * Returns "HTTP"
     *
     * @since 2.8.0
     */
    @Override
    public String getType() {
        return MESSAGE_TYPE;
    }
}
