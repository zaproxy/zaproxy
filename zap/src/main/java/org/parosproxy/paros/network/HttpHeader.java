/*
 * Created on Jun 14, 2004
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
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
// ZAP: 2012/01/12 Changed the method parse to use only CRLF as line separator.
// ZAP: 2012/03/15 Removed an unnecessary try catch block and unnecessary casting.
// Reworked the method getCharset.
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2012/10/04 Changed to initialise the instance variable mVersion with a
// valid version (HttpHeader.HTTP10).
// ZAP: 2012/11/01 Issue 410: charset wrapped in quotation marks
// ZAP: 2013/04/08 Issue 605: Force intercepts via header
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2013/09/02 Resolved header value setting on setHeader() which manage wrongly the "-" char
// ZAP: 2013/11/16 Issue 867: HttpMessage#getFormParams should return an empty TreeSet if
// the request body is not "x-www-form-urlencoded"
// ZAP: 2015/03/26 Issue 1573: Add option to inject plugin ID in header for all ascan requests
// ZAP: 2016/06/17 Be lenient when parsing charset and accept single quote chars around the value
// ZAP: 2016/06/17 Remove redundant initialisations of instance variables
// ZAP: 2017/02/08 Change isEmpty to check start line instead of headers (if it has the
// status/request line it's not empty).
// ZAP: 2017/03/02 Issue 3226: Added API Key and Nonce headers
// ZAP: 2018/02/06 Make the lower/upper case changes locale independent (Issue 4327).
// ZAP: 2018/04/24 Add JSON Content-Type.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/12/09 Added getHeaderValues(String) method (returning List) and deprecated
// getHeaders(String) method (returning Vector).
// ZAP: 2022/03/11 Added headers: Content-Location, Link, Refresh
// ZAP: 2022/09/12 Allow only major HTTP version.
// ZAP: 2022/11/17 Add HTTP/2 constant.
// ZAP: 2022/11/22 Lower case the HTTP field names for compatibility with HTTP/2.
package org.parosproxy.paros.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class HttpHeader implements java.io.Serializable {

    private static final long serialVersionUID = 7922279497679304778L;
    public static final String CRLF = "\r\n";
    public static final String LF = "\n";
    public static final String CONTENT_LENGTH = "content-length";
    public static final String TRANSFER_ENCODING = "transfer-encoding";
    public static final String CONTENT_ENCODING = "content-encoding";
    public static final String CONTENT_LOCATION = "content-location";
    public static final String CONTENT_TYPE = "content-type";
    public static final String PROXY_CONNECTION = "proxy-connection";
    public static final String PROXY_AUTHENTICATE = "proxy-authenticate";
    public static final String CONNECTION = "connection";
    public static final String AUTHORIZATION = "authorization";
    public static final String WWW_AUTHENTICATE = "www-authenticate";
    public static final String LOCATION = "location";
    public static final String IF_MODIFIED_SINCE = "if-modified-since";
    public static final String IF_NONE_MATCH = "if-none-match";
    public static final String USER_AGENT = "user-agent";
    public static final String ACCEPT_ENCODING = "accept-encoding";
    public static final String CACHE_CONTROL = "cache-control";
    public static final String PRAGMA = "pragma";
    public static final String REFERER = "referer";
    public static final String X_ZAP_REQUESTID = "x-zap-requestid";
    public static final String X_SECURITY_PROXY = "x-security-proxy";
    public static final String COOKIE = "cookie";
    public static final String SET_COOKIE = "set-cookie";
    public static final String SET_COOKIE2 = "set-cookie2";
    public static final String X_XSS_PROTECTION = "x-xss-protection";
    public static final String X_FRAME_OPTION = "x-frame-options";
    public static final String X_CONTENT_TYPE_OPTIONS = "x-content-type-options";
    public static final String HTTP09 = "HTTP/0.9";
    public static final String HTTP10 = "HTTP/1.0";
    public static final String HTTP11 = "HTTP/1.1";
    public static final String HTTP2 = "HTTP/2";
    public static final String _CLOSE = "Close";
    public static final String _KEEP_ALIVE = "Keep-Alive";
    public static final String _CHUNKED = "Chunked";
    public static final String FORM_URLENCODED_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String SCHEME_HTTP = "http://";
    public static final String SCHEME_HTTPS = "https://";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String DEFLATE = "deflate";
    public static final String GZIP = "gzip";
    public static final String IDENTITY = "identity";
    public static final String SEC_PROXY_INTERCEPT = "intercept";
    public static final String SEC_PROXY_RECORD = "record";
    public static final String LINK = "link";
    public static final String REFRESH = "refresh";
    public static final Pattern patternCRLF = Pattern.compile("\\r\\n", Pattern.MULTILINE);
    public static final Pattern patternLF = Pattern.compile("\\n", Pattern.MULTILINE);
    // ZAP: Issue 410: charset wrapped in quotation marks
    private static final Pattern patternCharset =
            Pattern.compile(
                    "charset *= *(?:(?:'([^';\\s]+))|(?:\"?([^\";\\s]+)\"?))",
                    Pattern.CASE_INSENSITIVE);
    protected static final String p_TEXT = "[^\\x00-\\x1f\\r\\n]*";
    protected static final String p_METHOD = "(\\w+)";
    protected static final String p_SP = " +";
    // protected static final String p_URI			= "(\\S+)";
    // allow space in URI for encoding to %20
    protected static final String p_URI = "([^\\r\\n]+)";
    protected static final String p_VERSION = "(HTTP/\\d+(?:\\.\\d+)?)";
    protected static final String p_STATUS_CODE = "(\\d{3})";
    protected static final String p_REASON_PHRASE = "(" + p_TEXT + ")";
    protected String mStartLine;
    protected String mMsgHeader;
    protected boolean mMalformedHeader;
    protected Hashtable<String, Vector<String>> mHeaderFields;
    protected int mContentLength;
    protected String mLineDelimiter;
    protected String mVersion;
    // ZAP: added CORS headers
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "access-control-allow-origin";
    public static final String ACCESS_CONTROL_ALLOW_HEADERS = "access-control-allow-headers";
    public static final String ACCESS_CONTROL_ALLOW_METHODS = "access-control-allow-methods";
    public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "access-control-expose-headers";
    // ZAP: added "Allow" and "Public" Headers, for response to "OPTIONS" method
    public static final String METHODS_ALLOW = "Allow";
    public static final String METHODS_PUBLIC = "Public"; // IIS specific?
    public static final String X_ZAP_SCAN_ID = "x-zap-scan-id";
    public static final String X_ZAP_API_KEY = "x-zap-api-key";
    public static final String X_ZAP_API_NONCE = "x-zap-api-nonce";
    // ZAP: additional standard/defacto headers
    public static final String PROXY_AUTHORIZATION = "proxy-authorization";
    public static final String X_CSRF_TOKEN = "x-csrf-token";
    public static final String X_CSRFTOKEN = "x-csrftoken";
    public static final String X_XSRF_TOKEN = "x-xsrf-token";

    public HttpHeader() {
        init();
    }

    /**
     * Construct a HttpHeader from a given String.
     *
     * @param data
     * @throws HttpMalformedHeaderException
     */
    public HttpHeader(String data) throws HttpMalformedHeaderException {
        setMessage(data);
    }

    /** Inititialization. */
    private void init() {
        mHeaderFields = new Hashtable<>();
        mStartLine = "";
        mMsgHeader = "";
        mMalformedHeader = false;
        mContentLength = -1;
        mLineDelimiter = CRLF;
        mVersion = HttpHeader.HTTP10;
    }

    /**
     * Set and parse this HTTP header with the string given.
     *
     * @param data
     * @throws HttpMalformedHeaderException
     */
    public void setMessage(String data) throws HttpMalformedHeaderException {
        clear();
        try {
            if (!this.parse(data)) {
                mMalformedHeader = true;
            }
        } catch (Exception e) {
            mMalformedHeader = true;
        }

        if (mMalformedHeader) {
            throw new HttpMalformedHeaderException();
        }
    }

    public void clear() {
        init();
    }

    /**
     * Get the first header value using the name given. If there are multiple occurrence, only the
     * first one will be returned as String.
     *
     * @param name
     * @return the header value. null if not found.
     */
    public String getHeader(String name) {
        List<String> headers = getHeaderValues(name);
        if (headers.isEmpty()) {
            return null;
        }

        return headers.get(0);
    }

    /**
     * Get headers with the name. Multiple value can be returned.
     *
     * @param name
     * @return a vector holding the value as string.
     * @deprecated since 2.9.0. See {@link #getHeaderValues(String)} instead
     */
    @Deprecated
    public Vector<String> getHeaders(String name) {
        return mHeaderFields.get(normalisedHeaderName(name));
    }

    /**
     * Get header(s) with the name. Multiple values can be returned.
     *
     * @param name the name of the header(s) to return.
     * @return a {@code List} holding the value(s) as String(s).
     * @since 2.9.0
     */
    public List<String> getHeaderValues(String name) {
        List<String> values = mHeaderFields.get(normalisedHeaderName(name));
        return values == null ? Collections.emptyList() : Collections.unmodifiableList(values);
    }

    public List<HttpHeaderField> getHeaders() {
        List<HttpHeaderField> headerFields = new ArrayList<>();
        String[] headers = mMsgHeader.split(Pattern.quote(mLineDelimiter));

        for (int i = 0; i < headers.length; ++i) {
            String[] headerField = headers[i].split(":", 2);
            if (headerField.length == 2) {
                headerFields.add(new HttpHeaderField(headerField[0].trim(), headerField[1].trim()));
            }
        }
        return headerFields;
    }

    /**
     * Add a header with the name and value given. It will be appended to the header string.
     *
     * @param name
     * @param val
     */
    public void addHeader(String name, String val) {
        mMsgHeader = mMsgHeader + name + ": " + val + mLineDelimiter;
        addInternalHeaderFields(name, val);
    }

    /**
     * Set a header name and value. If the name is not found, it will be added. If the value is
     * null, the header will be removed.
     *
     * @param name
     * @param value
     */
    public void setHeader(String name, String value) {
        //		int pos = 0;
        //		int crlfpos = 0;
        Pattern pattern = null;

        if (getHeaderValues(name).isEmpty() && value != null) {
            // header value not found, append to end
            addHeader(name, value);
        } else {
            pattern = getHeaderRegex(name);
            Matcher matcher = pattern.matcher(mMsgHeader);
            if (value == null) {
                // delete header
                mMsgHeader = matcher.replaceAll("");
            } else {
                // replace header
                String newString = name + ": " + value + mLineDelimiter;
                mMsgHeader = matcher.replaceAll(Matcher.quoteReplacement(newString));
            }

            // set into hashtable
            replaceInternalHeaderFields(name, value);
        }
    }

    private Pattern getHeaderRegex(String name) throws PatternSyntaxException {
        // Added character quoting to avoid troubles with "-" char or similar
        return Pattern.compile(
                "^ *\\Q" + name + "\\E *: *[^\\r\\n]*" + mLineDelimiter,
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    }

    /**
     * Return the HTTP version (e.g. HTTP/1.0, HTTP/1.1)
     *
     * @return
     */
    public String getVersion() {
        return mVersion;
    }

    /**
     * Set the HTTP version of this header.
     *
     * @param version
     */
    public abstract void setVersion(String version);

    /**
     * Get the content length of this header.
     *
     * @return content length. -1 means content length not set.
     */
    public int getContentLength() {
        return mContentLength;
    }

    /**
     * Set the content length of this header.
     *
     * @param len
     */
    public void setContentLength(int len) {
        if (mContentLength != len) {
            setHeader(CONTENT_LENGTH, Integer.toString(len));
            mContentLength = len;
        }
    }

    /**
     * Check if this header expect connection to be closed. HTTP/1.0 default to close. HTTP/1.1
     * default to keep-alive.
     *
     * @return
     */
    public boolean isConnectionClose() {
        boolean result = true;
        if (mMalformedHeader) {
            return true;
        }

        if (isHttp10()) {
            // HTTP 1.0 default to close unless keep alive.
            result = true;
            try {
                if (getHeader(CONNECTION).equalsIgnoreCase(_KEEP_ALIVE)
                        || getHeader(PROXY_CONNECTION).equalsIgnoreCase(_KEEP_ALIVE)) {
                    return false;
                }
            } catch (NullPointerException e) {
            }

        } else if (isHttp11()) {
            // HTTP 1.1 default to keep alive unless close.
            result = false;
            try {
                if (getHeader(CONNECTION).equalsIgnoreCase(_CLOSE)) {
                    return true;
                } else if (getHeader(PROXY_CONNECTION).equalsIgnoreCase(_CLOSE)) {
                    return true;
                }
            } catch (NullPointerException e) {
            }
        }
        return result;
    }

    /**
     * Check if this header is HTTP 1.0.
     *
     * @return true if HTTP 1.0.
     */
    public boolean isHttp10() {
        if (mVersion.equalsIgnoreCase(HTTP10)) {
            return true;
        }
        return false;
    }

    /**
     * Check if this header is HTTP 1.1.
     *
     * @return true if HTTP 1.0.
     */
    public boolean isHttp11() {
        if (mVersion.equalsIgnoreCase(HTTP11)) {
            return true;
        }
        return false;
    }

    /**
     * Check if Transfer Encoding Chunked is set in this header.
     *
     * @return true if transfer encoding chunked is set.
     */
    public boolean isTransferEncodingChunked() {
        String transferEncoding = getHeader(TRANSFER_ENCODING);
        if (transferEncoding != null && transferEncoding.equalsIgnoreCase(_CHUNKED)) {
            return true;
        }
        return false;
    }

    /**
     * Parse this Http header using the String given.
     *
     * @param data String to be parsed to form this header.
     * @return
     * @throws Exception
     */
    protected boolean parse(String data) throws Exception {
        if (data == null || data.isEmpty()) {
            return true;
        }

        // ZAP: Replace all "\n" with "\r\n" to parse correctly
        String newData = data.replaceAll("(?<!\r)\n", CRLF);
        // ZAP: always use CRLF to comply with HTTP specification
        // even if the data it's not directly used.
        mLineDelimiter = CRLF;

        String[] split = patternCRLF.split(newData);
        mStartLine = split[0];

        String token = null, name = null, value = null;
        int pos = 0;

        StringBuilder sb = new StringBuilder(2048);
        for (int i = 1; i < split.length; i++) {
            token = split[i];
            if (token.equals("")) {
                continue;
            }

            if ((pos = token.indexOf(":")) < 0) {
                mMalformedHeader = true;
                return false;
            }
            name = token.substring(0, pos).trim();
            value = token.substring(pos + 1).trim();

            if (name.equalsIgnoreCase(CONTENT_LENGTH)) {
                try {
                    mContentLength = Integer.parseInt(value);
                } catch (NumberFormatException nfe) {
                }
            }

            /*
            if (name.equalsIgnoreCase(PROXY_CONNECTION)) {
            sb.append(name + ": " + _CLOSE + mLineDelimiter);
            } else if (name.equalsIgnoreCase(CONNECTION)) {
            sb.append(name + ": " + _CLOSE + mLineDelimiter);
            } else {
            */
            sb.append(name + ": " + value + mLineDelimiter);
            // }

            addInternalHeaderFields(name, value);
        }

        mMsgHeader = sb.toString();
        return true;
    }

    /**
     * Replace the header stored in internal hashtable
     *
     * @param name
     * @param value
     */
    private void replaceInternalHeaderFields(String name, String value) {
        String key = normalisedHeaderName(name);
        Vector<String> v = getHeaders(key);
        if (v == null) {
            v = new Vector<>();
            mHeaderFields.put(key, v);
        }

        if (value != null) {
            v.clear();
            v.add(value);
        } else {
            mHeaderFields.remove(key);
        }
    }

    /**
     * Add the header stored in internal hashtable
     *
     * @param name
     * @param value
     */
    private void addInternalHeaderFields(String name, String value) {
        String key = normalisedHeaderName(name);
        Vector<String> v = getHeaders(key);
        if (v == null) {
            v = new Vector<>();
            mHeaderFields.put(key, v);
        }

        if (value != null) {
            v.add(value);
        } else {
            mHeaderFields.remove(key);
        }
    }

    /**
     * Gets the header name normalised, to obtain the value(s) from {@link #mHeaderFields}.
     *
     * <p>The normalisation is done by changing all characters to upper case.
     *
     * @param name the name of the header to normalise.
     * @return the normalised header name.
     */
    private static String normalisedHeaderName(String name) {
        return name.toUpperCase(Locale.ROOT);
    }

    /**
     * Get if this is a malformed header.
     *
     * @return
     */
    public boolean isMalformedHeader() {
        return mMalformedHeader;
    }

    /** Get a string representation of this header. */
    @Override
    public String toString() {
        return getPrimeHeader() + mLineDelimiter + mMsgHeader + mLineDelimiter;
    }

    /**
     * Get the prime header.
     *
     * @return startline for request, statusline for response.
     */
    public abstract String getPrimeHeader();

    /**
     * Get if this is a image header.
     *
     * @return true if image.
     */
    public boolean isImage() {
        return false;
    }

    /**
     * Get if this is a text header.
     *
     * @return true if text.
     */
    public boolean isText() {
        return true;
    }

    /**
     * Tells whether or not the HTTP header contains any of the given {@code Content-Type} values.
     *
     * <p>The values are expected to be in lower case.
     *
     * @param contentTypes the values to check.
     * @return {@code true} if any of the given values is contained in the (first) {@code
     *     Content-Type} header, {@code false} otherwise.
     * @since 2.8.0
     * @see #getNormalisedContentTypeValue()
     */
    public boolean hasContentType(String... contentTypes) {
        if (contentTypes == null || contentTypes.length == 0) {
            return true;
        }

        String normalisedContentType = getNormalisedContentTypeValue();
        if (normalisedContentType == null) {
            return false;
        }

        for (String contentType : contentTypes) {
            if (normalisedContentType.contains(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the normalised value of the (first) {@code Content-Type} header.
     *
     * <p>The normalisation is done by changing all characters to lower case.
     *
     * @return the value normalised, might be {@code null}.
     * @since 2.8.0
     * @see #hasContentType(String...)
     */
    public String getNormalisedContentTypeValue() {
        String contentType = getHeader(CONTENT_TYPE);
        if (contentType != null) {
            return contentType.toLowerCase(Locale.ROOT);
        }
        return null;
    }

    /**
     * Get the line delimiter of this header.
     *
     * @return
     */
    public String getLineDelimiter() {
        return mLineDelimiter;
    }

    /**
     * Get the headers as string. All the headers name value pair is concatenated and delimited.
     *
     * @return Eg "Host: www.example.com\r\nUser-agent: some agent\r\n"
     */
    public String getHeadersAsString() {
        return mMsgHeader;
    }

    /**
     * Tells whether or not the header is empty.
     *
     * <p>A header is empty if it has no content (for example, no start line nor headers).
     *
     * @return {@code true} if the header is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        if (mStartLine == null || mStartLine.isEmpty()) {
            return true;
        }

        return false;
    }

    public String getCharset() {
        String contentType = getHeader(CONTENT_TYPE);
        if (contentType == null) {
            return null;
        }

        Matcher matcher = patternCharset.matcher(contentType);
        if (matcher.find()) {
            String charset = matcher.group(2);
            if (charset == null) {
                return matcher.group(1);
            }
            return charset;
        }
        return null;
    }
}
