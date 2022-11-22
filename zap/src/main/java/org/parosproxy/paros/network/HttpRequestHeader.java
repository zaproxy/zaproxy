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
// ZAP: 2012:02/01 Changed getHostPort() to return proper port number even if it
// is not explicitly specified in URI
// ZAP: 2011/08/04 Changed to support Logging
// ZAP: 2011/10/29 Log errors
// ZAP: 2011/11/03 Changed isImage() to prevent a NullPointerException when the path doesn't exist
// ZAP: 2011/12/09 Changed HttpRequestHeader(String method, URI uri, String version) to add
//      the Cache-Control header field when the HTTP version is 1.1 and changed a if condition to
//      validate the variable version instead of the variable method.
// ZAP: 2012/03/15 Changed to use the class StringBuilder instead of StringBuffer. Reworked some
// methods.
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/06/24 Added method to add Cookies of type java.net.HttpCookie to request header
// ZAP: 2012/06/24 Added new method of getting cookies from the request header.
// ZAP: 2012/10/08 Issue 361: getHostPort on HttpRequestHeader for HTTPS CONNECT
// requests returns the wrong port
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/03/08 Improved parse error reporting
// ZAP: 2013/04/14 Issue 596: Rename the method HttpRequestHeader.getSecure to isSecure
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2013/12/09 Set Content-type only in case of POST or PUT HTTP methods
// ZAP: 2015/08/07 Issue 1768: Update to use a more recent default user agent
// ZAP: 2016/06/17 Remove redundant initialisations of instance variables
// ZAP: 2016/09/26 JavaDoc tweaks
// ZAP: 2017/02/23 Issue 3227: Limit API access to permitted IP addresses
// ZAP: 2017/04/24 Added more HTTP methods
// ZAP: 2017/10/19 Skip parsing of empty Cookie headers.
// ZAP: 2017/11/22 Address a NPE in isImage().
// ZAP: 2018/01/10 Tweak how cookie header is reconstructed from HtmlParameter(s).
// ZAP: 2018/02/06 Make the upper case changes locale independent (Issue 4327).
// ZAP: 2018/08/10 Allow to set the user agent used by default request headers (Issue 4846).
// ZAP: 2018/11/16 Add Accept header.
// ZAP: 2019/01/25 Add Origin header.
// ZAP: 2019/03/06 Log or include the malformed data in the exception message.
// ZAP: 2019/03/19 Changed the parse method to only parse the authority on CONNECT requests
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/12/09 Address deprecation of getHeaders(String) Vector method.
// ZAP: 2020/11/10 Add convenience method isCss(), refactor isImage() to use new private method
// isSpecificType(Pattern).
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/05/10 Use authority for CONNECT requests.
// ZAP: 2021/07/16 Issue 6691: Do not add zero Content-Length by default in GET requests
// ZAP: 2021/07/19 Include SVG in isImage().
// ZAP: 2022/09/12 Allow arbitrary HTTP versions.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
// ZAP: 2022/11/22 Lower case the HTTP field names for compatibility with HTTP/2.
package org.parosproxy.paros.network;

import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpRequestHeader extends HttpHeader {

    /**
     * The {@code Accept} request header.
     *
     * @since 2.8.0
     */
    public static final String ACCEPT = "accept";

    /**
     * The {@code Origin} request header.
     *
     * @since 2.8.0
     */
    public static final String ORIGIN = "origin";

    private static final long serialVersionUID = 4156598327921777493L;
    private static final Logger log = LogManager.getLogger(HttpRequestHeader.class);

    // method list
    public static final String CONNECT = "CONNECT";
    public static final String DELETE = "DELETE";
    public static final String GET = "GET";
    public static final String HEAD = "HEAD";
    public static final String OPTIONS = "OPTIONS";
    public static final String PATCH = "PATCH";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String TRACE = "TRACE";
    public static final String TRACK = "TRACK";

    // ZAP: Added method array
    public static final String[] METHODS = {
        CONNECT, DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT, TRACE, TRACK
    };
    public static final String HOST = "host";
    private static final Pattern patternRequestLine =
            Pattern.compile(p_METHOD + p_SP + p_URI + p_SP + p_VERSION, Pattern.CASE_INSENSITIVE);
    // private static final Pattern patternHostHeader
    //	= Pattern.compile("([^:]+)\\s*?:?\\s*?(\\d*?)");
    private static final Pattern patternImage =
            Pattern.compile(
                    "\\.(bmp|ico|jpg|jpeg|gif|tiff|tif|png|svg)\\z", Pattern.CASE_INSENSITIVE);
    private static final Pattern patternPartialRequestLine =
            Pattern.compile(
                    "\\A *(OPTIONS|GET|HEAD|POST|PUT|DELETE|TRACE|CONNECT)\\b",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_CSS =
            Pattern.compile("\\.css\\z", Pattern.CASE_INSENSITIVE);

    /**
     * The user agent used by {@link #HttpRequestHeader(String, URI, String) default request
     * header}.
     */
    private static String defaultUserAgent;

    private String mMethod;
    private URI mUri;
    private String mHostName;
    private InetAddress senderAddress;

    /**
     * The host port number of this request message, a non-negative integer.
     *
     * <p>Default is {@code 80}.
     *
     * <p><strong>Note:</strong> All the modifications to the instance variable {@code mHostPort}
     * must be done through the method {@code setHostPort(int)}, so a valid and correct value is set
     * when no port number is defined (which is represented with the negative integer -1).
     *
     * @see #getHostPort()
     * @see #setHostPort(int)
     * @see URI#getPort()
     */
    private int mHostPort;

    private boolean mIsSecure;

    /** Constructor for an empty header. */
    public HttpRequestHeader() {
        super();
        mMethod = "";
        mHostName = "";
        mHostPort = 80;
    }

    /**
     * Constructor of a request header with the string.
     *
     * @param data the request header
     * @param isSecure {@code true} if the request should be secure, {@code false} otherwise
     * @throws HttpMalformedHeaderException if the request being set is malformed
     * @see #setSecure(boolean)
     */
    public HttpRequestHeader(String data, boolean isSecure) throws HttpMalformedHeaderException {
        setMessage(data, isSecure);
    }

    /**
     * Constructor of a request header with the string. Whether this is a secure header depends on
     * the URL given.
     *
     * @param data the request header
     * @throws HttpMalformedHeaderException if the request being set is malformed
     */
    public HttpRequestHeader(String data) throws HttpMalformedHeaderException {
        setMessage(data);
    }

    @Override
    public void clear() {
        super.clear();

        mMethod = "";
        mUri = null;
        mHostName = "";
        setHostPort(-1);
    }

    /**
     * Constructs a {@code HttpRequestHeader} with the given method, URI, and version.
     *
     * <p>The following headers are automatically added:
     *
     * <ul>
     *   <li>{@code Host}, with the domain and port from the given URI.
     *   <li>{@code User-Agent}, using the {@link #getDefaultUserAgent()}.
     *   <li>{@code Pragma: no-cache}
     *   <li>{@code Cache-Control: no-cache}, if version is HTTP/1.1
     *   <li>{@code Content-Type: application/x-www-form-urlencoded}, if the method is POST or PUT.
     * </ul>
     *
     * @param method the request method.
     * @param uri the request target.
     * @param version the version, for example, {@code HTTP/1.1}.
     * @throws HttpMalformedHeaderException if the resulting HTTP header is malformed.
     */
    public HttpRequestHeader(String method, URI uri, String version)
            throws HttpMalformedHeaderException {
        this(method + " " + uri.toString() + " " + version.toUpperCase(Locale.ROOT) + CRLF + CRLF);

        try {
            setHeader(
                    HOST,
                    uri.getHost()
                            + (uri.getPort() > 0 ? ":" + Integer.toString(uri.getPort()) : ""));

        } catch (URIException e) {
            log.error(e.getMessage(), e);
        }

        setHeader(USER_AGENT, defaultUserAgent);
        setHeader(PRAGMA, "no-cache");

        // ZAP: added the Cache-Control header field to comply with HTTP/1.1
        if (version.equalsIgnoreCase(HTTP11)) {
            setHeader(CACHE_CONTROL, "no-cache");
        }

        // ZAP: set content type x-www-urlencoded only if it's a POST or a PUT
        if (method.equalsIgnoreCase(POST) || method.equalsIgnoreCase(PUT)) {
            setHeader(CONTENT_TYPE, "application/x-www-form-urlencoded");
        }

        setHeader(ACCEPT_ENCODING, null);
    }

    /**
     * Constructs a {@code HttpRequestHeader} with the given method, URI, and version.
     *
     * <p>The following headers are automatically added:
     *
     * <ul>
     *   <li>{@code Host}, with the domain and port from the given URI.
     *   <li>{@code User-Agent}, using the {@link #getDefaultUserAgent()}.
     *   <li>{@code Pragma: no-cache}
     *   <li>{@code Cache-Control: no-cache}, if version is HTTP/1.1
     *   <li>{@code Content-Type: application/x-www-form-urlencoded}, if the method is POST or PUT.
     * </ul>
     *
     * @param method the request method.
     * @param uri the request target.
     * @param version the version, for example, {@code HTTP/1.1}.
     * @param params unused.
     * @throws HttpMalformedHeaderException if the resulting HTTP header is malformed.
     * @deprecated (2.8.0) Use {@link #HttpRequestHeader(String, URI, String)} instead.
     * @since 2.4.2
     */
    @Deprecated
    public HttpRequestHeader(String method, URI uri, String version, ConnectionParam params)
            throws HttpMalformedHeaderException {
        this(method, uri, version);
    }

    /**
     * Set this request header with the given message.
     *
     * @param data the request header
     * @param isSecure {@code true} if the request should be secure, {@code false} otherwise
     * @throws HttpMalformedHeaderException if the request being set is malformed
     * @see #setSecure(boolean)
     */
    public void setMessage(String data, boolean isSecure) throws HttpMalformedHeaderException {
        super.setMessage(data);

        try {
            parse(isSecure);

        } catch (HttpMalformedHeaderException e) {
            mMalformedHeader = true;
            log.debug("Malformed header: {}", data, e);

            throw e;

        } catch (Exception e) {
            log.error("Failed to parse:\n{}", data, e);
            mMalformedHeader = true;
            throw new HttpMalformedHeaderException(e.getMessage());
        }
    }

    /**
     * Set this request header with the given message. Whether this is a secure header depends on
     * the URL given.
     */
    @Override
    public void setMessage(String data) throws HttpMalformedHeaderException {
        this.setMessage(data, false);
    }

    /**
     * Get the HTTP method (GET, POST, ..., etc.).
     *
     * @return the request method
     */
    public String getMethod() {
        return mMethod;
    }

    /**
     * Set the HTTP method of this request header.
     *
     * @param method the new method, must not be {@code null}.
     */
    public void setMethod(String method) {
        mMethod = method.toUpperCase(Locale.ROOT);
    }

    /**
     * Get the URI of this request header.
     *
     * @return the request URI
     */
    public URI getURI() {
        return mUri;
    }

    /**
     * Sets the URI of this request header.
     *
     * @param uri the new request URI
     * @throws URIException if an error occurred while setting the request URI
     */
    public void setURI(URI uri) throws URIException {

        if (uri.getScheme() == null || uri.getScheme().equals("")) {
            mUri = new URI(HTTP + "://" + getHeader(HOST) + "/" + mUri.toString(), true);

        } else {
            mUri = uri;
        }

        if (uri.getScheme().equalsIgnoreCase(HTTPS)) {
            mIsSecure = true;

        } else {
            mIsSecure = false;
        }

        setHostPort(mUri.getPort());
    }

    /**
     * Get if this request header is under secure connection.
     *
     * @return {@code true} if the request is secure, {@code false} otherwise
     * @deprecated Replaced by {@link #isSecure()}. It will be removed in a future release.
     */
    @Deprecated
    public boolean getSecure() {
        return mIsSecure;
    }

    /**
     * Tells whether the request is secure, or not. A request is considered secure if it's using the
     * HTTPS protocol.
     *
     * @return {@code true} if the request is secure, {@code false} otherwise.
     */
    public boolean isSecure() {
        return mIsSecure;
    }

    /**
     * Sets whether or not the request is done using a secure scheme, HTTPS.
     *
     * @param isSecure {@code true} if the request should be secure, {@code false} otherwise
     * @throws URIException if an error occurred while rebuilding the request URI
     */
    public void setSecure(boolean isSecure) throws URIException {
        mIsSecure = isSecure;

        if (mUri == null) {
            // mUri not yet set
            return;
        }

        URI newUri = mUri;

        // check if URI consistent
        if (isSecure() && mUri.getScheme().equalsIgnoreCase(HTTP)) {
            newUri = new URI(mUri.toString().replaceFirst(HTTP, HTTPS), true);

        } else if (!isSecure() && mUri.getScheme().equalsIgnoreCase(HTTPS)) {
            newUri = new URI(mUri.toString().replaceFirst(HTTPS, HTTP), true);
        }

        if (newUri != mUri) {
            mUri = newUri;
            setHostPort(mUri.getPort());
        }
    }

    /** Set the HTTP version of this request header. */
    @Override
    public void setVersion(String version) {
        mVersion = version.toUpperCase(Locale.ROOT);
    }

    /**
     * Get the content length in this request header. If the content length is undetermined, 0 will
     * be returned.
     */
    @Override
    public int getContentLength() {
        if (mContentLength == -1) {
            return 0;
        }

        return mContentLength;
    }

    /**
     * Parse this request header.
     *
     * @param isSecure {@code true} if the request is secure, {@code false} otherwise
     * @throws URIException if failed to parse the URI
     * @throws HttpMalformedHeaderException if the request being parsed is malformed
     */
    private void parse(boolean isSecure) throws URIException, HttpMalformedHeaderException {

        mIsSecure = isSecure;
        Matcher matcher = patternRequestLine.matcher(mStartLine);
        if (!matcher.find()) {
            mMalformedHeader = true;
            throw new HttpMalformedHeaderException(
                    "Failed to find pattern " + patternRequestLine + " in: " + mStartLine);
        }

        mMethod = matcher.group(1);
        String sUri = matcher.group(2);
        mVersion = matcher.group(3);

        if (mMethod.equalsIgnoreCase(CONNECT)) {
            parseHostName(sUri);
            mUri = URI.fromAuthority(sUri);

        } else {
            mUri = parseURI(sUri);

            if (mUri.getScheme() == null || mUri.getScheme().equals("")) {
                mUri = new URI(HTTP + "://" + getHeader(HOST) + mUri.toString(), true);
            }

            if (isSecure() && mUri.getScheme().equalsIgnoreCase(HTTP)) {
                mUri = new URI(mUri.toString().replaceFirst(HTTP, HTTPS), true);
            }

            if (mUri.getScheme().equalsIgnoreCase(HTTPS)) {
                setSecure(true);
            }
            mHostName = mUri.getHost();
            setHostPort(mUri.getPort());
        }
    }

    private void parseHostName(String hostHeader) {
        // no host header given but a valid host name already exist.
        if (hostHeader == null) {
            return;
        }

        int port = -1;
        int pos;
        if ((pos = hostHeader.indexOf(':', 2)) > -1) {
            mHostName = hostHeader.substring(0, pos).trim();
            try {
                port = Integer.parseInt(hostHeader.substring(pos + 1));
            } catch (NumberFormatException e) {
            }

        } else {
            mHostName = hostHeader.trim();
        }

        setHostPort(port);
    }

    /**
     * Get the host name in this request header.
     *
     * @return Host name.
     */
    public String getHostName() {
        String hostName = mHostName;
        try {
            // ZAP: fixed cases, where host name is null
            hostName = ((mUri.getHost() != null) ? mUri.getHost() : mHostName);

        } catch (URIException e) {
            if (log.isDebugEnabled()) {
                log.warn(e);
            }
        }

        return hostName;
    }

    /**
     * Gets the host port number of this request message, a non-negative integer.
     *
     * <p>If no port is defined the default port for the used scheme will be returned, either 80 for
     * HTTP or 443 for HTTPS.
     *
     * @return the host port number, a non-negative integer
     */
    public int getHostPort() {
        return mHostPort;
    }

    /**
     * Sets the host port number of this request message.
     *
     * <p>If the given {@code port} number is negative (usually -1 to represent that no port number
     * is defined), the port number set will be the default port number for the used scheme known
     * using the method {@code isSecure()}, either 80 for HTTP or 443 for HTTPS.
     *
     * @param port the new port number
     * @see #mHostPort
     * @see #isSecure()
     * @see URI#getPort()
     */
    private void setHostPort(int port) {
        if (port > -1) {
            mHostPort = port;

        } else if (this.isSecure()) {
            mHostPort = 443;

        } else {
            mHostPort = 80;
        }
    }

    /** Return if this request header is a image request basing on the path suffix. */
    @Override
    public boolean isImage() {
        return isSpecificType(patternImage);
    }

    public boolean isCss() {
        return isSpecificType(PATTERN_CSS);
    }

    private boolean isSpecificType(Pattern pattern) {
        if (getURI() == null) {
            return false;
        }

        try {
            // ZAP: prevents a NullPointerException when no path exists
            final String path = getURI().getPath();
            if (path != null) {
                return (pattern.matcher(path).find());
            }

        } catch (URIException e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    /**
     * Return if the data given is a request header basing on the first start line.
     *
     * @param data the data to be checked
     * @return {@code true} if the data contains a request line, {@code false} otherwise.
     */
    public static boolean isRequestLine(String data) {
        return patternPartialRequestLine.matcher(data).find();
    }

    /** Return the prime header (first line). */
    @Override
    public String getPrimeHeader() {
        return getMethod() + " " + getURI().toString() + " " + getVersion();
    }
    /*
     * private static final char[] DELIM_UNWISE_CHAR = { '<', '>', '#', '"', '
     * ', '{', '}', '|', '\\', '^', '[', ']', '`' };
     */
    private static final String DELIM = "<>#\"";
    private static final String UNWISE = "{}|\\^[]`";
    private static final String DELIM_UNWISE = DELIM + UNWISE;

    public static URI parseURI(String sUri) throws URIException {
        URI uri;

        int len = sUri.length();
        StringBuilder sb = new StringBuilder(len);
        char[] charray = new char[1];
        String s;

        for (int i = 0; i < len; i++) {
            char ch = sUri.charAt(i);
            // String ch = sUri.substring(i, i+1);
            if (DELIM_UNWISE.indexOf(ch) >= 0) {
                // check if unwise or delim in RFC.  If so, encode it.
                charray[0] = ch;
                s = new String(charray);
                try {
                    s = URLEncoder.encode(s, "UTF8");

                } catch (UnsupportedEncodingException e1) {
                }

                sb.append(s);

            } else if (ch == '%') {

                // % is exception - no encoding to be done because some server may not handle
                // correctly when % is invalid.
                //

                // sb.append(ch);

                // if % followed by hex, no encode.

                try {
                    String hex = sUri.substring(i + 1, i + 3);
                    Integer.parseInt(hex, 16);
                    sb.append(ch);

                } catch (Exception e) {
                    charray[0] = ch;
                    s = new String(charray);
                    try {
                        s = URLEncoder.encode(s, "UTF8");

                    } catch (UnsupportedEncodingException e1) {
                    }
                    sb.append(s);
                }

            } else if (ch == ' ') {
                // if URLencode, '+' will be appended.
                sb.append("%20");

            } else {
                sb.append(ch);
            }
        }

        uri = new URI(sb.toString(), true);
        return uri;
    }

    // Construct new GET url of request
    // Based on getParams
    public void setGetParams(TreeSet<HtmlParameter> getParams) {
        if (mUri == null) {
            return;
        }

        if (getParams.isEmpty()) {
            try {
                mUri.setQuery("");

            } catch (URIException e) {
                log.error(e.getMessage(), e);
            }

            return;
        }

        StringBuilder sbQuery = new StringBuilder();
        for (HtmlParameter parameter : getParams) {
            if (parameter.getType() != HtmlParameter.Type.url) {
                continue;
            }

            sbQuery.append(parameter.getName());
            sbQuery.append('=');
            sbQuery.append(parameter.getValue());
            sbQuery.append('&');
        }

        if (sbQuery.length() <= 2) {
            try {
                mUri.setQuery("");

            } catch (URIException e) {
                log.error(e.getMessage(), e);
            }

            return;
        }

        String query = sbQuery.substring(0, sbQuery.length() - 1);

        try {
            // The previous behaviour was escaping the query,
            // so it is maintained with the use of setQuery.
            mUri.setQuery(query);

        } catch (URIException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Construct new "Cookie:" line in request header based on HttpCookies.
     *
     * @param cookies the new cookies
     */
    public void setCookies(List<HttpCookie> cookies) {
        if (cookies.isEmpty()) {
            setHeader(HttpHeader.COOKIE, null);
        }

        StringBuilder sbData = new StringBuilder();

        for (HttpCookie c : cookies) {
            sbData.append(c.getName());
            sbData.append('=');
            sbData.append(c.getValue());
            sbData.append("; ");
        }

        if (sbData.length() <= 3) {
            setHeader(HttpHeader.COOKIE, null);
            return;
        }

        final String data = sbData.substring(0, sbData.length() - 2);
        setHeader(HttpHeader.COOKIE, data);
    }

    // Construct new "Cookie:" line in request header,
    // based on cookieParams
    public void setCookieParams(TreeSet<HtmlParameter> cookieParams) {
        if (cookieParams.isEmpty()) {
            setHeader(HttpHeader.COOKIE, null);
        }

        StringBuilder sbData = new StringBuilder();

        for (HtmlParameter parameter : cookieParams) {
            if (parameter.getType() != HtmlParameter.Type.cookie) {
                continue;
            }

            String cookieName = parameter.getName();
            if (!cookieName.isEmpty()) {
                sbData.append(cookieName);
                sbData.append('=');
            }
            sbData.append(parameter.getValue());
            sbData.append("; ");
        }

        if (sbData.length() <= 2) {
            setHeader(HttpHeader.COOKIE, null);
            return;
        }

        final String data = sbData.substring(0, sbData.length() - 2);
        setHeader(HttpHeader.COOKIE, data);
    }

    public TreeSet<HtmlParameter> getCookieParams() {
        TreeSet<HtmlParameter> set = new TreeSet<>();

        for (String cookieLine : getHeaderValues(HttpHeader.COOKIE)) {
            // watch out for the scenario where the first cookie name starts with "cookie"
            // (uppercase or lowercase)
            if (cookieLine.toUpperCase().startsWith(HttpHeader.COOKIE.toUpperCase() + ":")) {
                // HttpCookie wont parse lines starting with "Cookie:"
                cookieLine = cookieLine.substring(HttpHeader.COOKIE.length() + 1);
            }

            if (cookieLine.isEmpty()) {
                // Nothing to parse.
                continue;
            }

            // These can be comma separated type=value
            String[] cookieArray = cookieLine.split(";");
            for (String cookie : cookieArray) {
                set.add(new HtmlParameter(cookie));
            }
        }

        return set;
    }

    // ZAP: Added method for working directly with HttpCookie
    /**
     * Gets a list of the http cookies from this request Header.
     *
     * @return the http cookies
     * @throws IllegalArgumentException if a problem is encountered while processing the "Cookie: "
     *     header line.
     */
    public List<HttpCookie> getHttpCookies() {
        List<HttpCookie> cookies = new LinkedList<>();
        // Use getCookieParams to reduce the places we parse cookies
        TreeSet<HtmlParameter> ts = getCookieParams();
        Iterator<HtmlParameter> it = ts.iterator();
        while (it.hasNext()) {
            HtmlParameter htmlParameter = it.next();
            if (!htmlParameter.getName().isEmpty()) {
                try {
                    cookies.add(new HttpCookie(htmlParameter.getName(), htmlParameter.getValue()));

                } catch (IllegalArgumentException e) {
                    // Occurs while scanning ;)
                    log.debug("{} {}", e.getMessage(), htmlParameter.getName());
                }
            }
        }

        return cookies;
    }

    /**
     * Sets the senders IP address. Note that this is not persisted.
     *
     * @param inetAddress the senders IP address
     * @since 2.6.0
     */
    public void setSenderAddress(InetAddress inetAddress) {
        this.senderAddress = inetAddress;
    }

    /**
     * Gets the senders IP address
     *
     * @return the senders IP address
     * @since 2.6.0
     */
    public InetAddress getSenderAddress() {
        return senderAddress;
    }

    /**
     * Sets the user agent used by {@link #HttpRequestHeader(String, URI, String) default request
     * header}.
     *
     * <p>This is expected to be called only by core code, when the corresponding option is changed.
     *
     * @param defaultUserAgent the default user agent.
     * @since 2.8.0
     */
    public static void setDefaultUserAgent(String defaultUserAgent) {
        HttpRequestHeader.defaultUserAgent = defaultUserAgent;
    }

    /**
     * Gets the user agent used by {@link #HttpRequestHeader(String, URI, String) default request
     * header}.
     *
     * @return the default user agent.
     * @since 2.8.0
     */
    public static String getDefaultUserAgent() {
        return defaultUserAgent;
    }
}
