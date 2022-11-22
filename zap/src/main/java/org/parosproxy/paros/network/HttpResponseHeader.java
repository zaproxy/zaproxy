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
// ZAP: 2012/03/15 Added the @Override annotation to the appropriate methods.
// Moved to this class the method getCookieParams().
// ZAP: 2012/06/24 Added new method of getting cookies from the request header.
// ZAP: 2012/07/11 Added method to check if response type is text/html (isHtml())
// ZAP: 2012/08/06 Modified isText() to also consider javascript as text
// ZAP: 2013/02/12 Modified isText() to also consider atom+xml as text
// ZAP: 2013/03/08 Improved parse error reporting
// ZAP: 2014/02/21 i1046: The getHttpCookies() method in the HttpResponseHeader does not properly
// set the domain
// ZAP: 2014/04/09 i1145: Cookie parsing error if a comma is used
// ZAP: 2015/02/26 Include json as a text content type
// ZAP: 2016/06/17 Remove redundant initialisations of instance variables
// ZAP: 2017/03/21 Add method to check if response type is json (isJson())
// ZAP: 2017/11/10 Allow to set the status code and reason.
// ZAP: 2018/02/06 Make the lower/upper case changes locale independent (Issue 4327).
// ZAP: 2018/07/23 Add CSP headers.
// ZAP: 2018/08/15 Add Server header.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/12/09 Address deprecation of getHeaders(String) Vector method.
// ZAP: 2020/11/10 Add convenience method isCss().
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/05/14 Remove redundant type arguments.
// ZAP: 2022/09/12 Allow arbitrary HTTP versions.
// ZAP: 2022/09/21 Use format specifiers instead of concatenation when logging.
// ZAP: 2022/11/22 Lower case the HTTP field names for compatibility with HTTP/2.
package org.parosproxy.paros.network;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpResponseHeader extends HttpHeader {

    /**
     * The {@code Content-Security-Policy} response header.
     *
     * @since 2.8.0
     */
    public static final String CSP = "content-security-policy";

    /**
     * The {@code Content-Security-Policy-Report-Only} response header.
     *
     * @since 2.8.0
     */
    public static final String CSP_REPORT_ONLY = "content-security-policy-report-only";

    /**
     * The {@code X-Content-Security-Policy} response header.
     *
     * @since 2.8.0
     */
    public static final String XCSP = "x-content-security-policy";

    /**
     * The {@code X-WebKit-CSP} response header.
     *
     * @since 2.8.0
     */
    public static final String WEBKIT_CSP = "x-webkit-csp";

    /**
     * The {@code Server} response header.
     *
     * @since 2.8.0
     */
    public static final String SERVER = "server";

    private static final long serialVersionUID = 2812716126742059785L;
    private static final Logger log = LogManager.getLogger(HttpResponseHeader.class);

    public static final String HTTP_CLIENT_BAD_REQUEST = "HTTP/1.0 400 Bad request" + CRLF + CRLF;
    private static final String _CONTENT_TYPE_CSS = "css";
    private static final String _CONTENT_TYPE_IMAGE = "image";
    private static final String _CONTENT_TYPE_TEXT = "text";
    private static final String _CONTENT_TYPE_HTML = "html";
    private static final String _CONTENT_TYPE_JAVASCRIPT = "javascript";
    private static final String _CONTENT_TYPE_JSON = "json";
    private static final String _CONTENT_TYPE_XML = "xml";

    static final Pattern patternStatusLine =
            Pattern.compile(
                    p_VERSION + p_SP + p_STATUS_CODE + " *" + p_REASON_PHRASE,
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern patternPartialStatusLine =
            Pattern.compile("\\A *" + p_VERSION, Pattern.CASE_INSENSITIVE);

    private String mStatusCodeString;
    private int mStatusCode;
    private String mReasonPhrase;

    public HttpResponseHeader() {
        mStatusCodeString = "";
        mReasonPhrase = "";
    }

    public HttpResponseHeader(String data) throws HttpMalformedHeaderException {
        super(data);
    }

    @Override
    public void clear() {
        super.clear();
        mStatusCodeString = "";
        mStatusCode = 0;
        mReasonPhrase = "";
    }

    @Override
    public void setMessage(String data) throws HttpMalformedHeaderException {
        super.setMessage(data);
        try {
            parse();
        } catch (HttpMalformedHeaderException e) {
            mMalformedHeader = true;
            throw e;
        }
    }

    @Override
    public void setVersion(String version) {
        mVersion = version.toUpperCase(Locale.ROOT);
    }

    /**
     * Gets the status code.
     *
     * @return the status code.
     * @see #setStatusCode(int)
     */
    public int getStatusCode() {
        return mStatusCode;
    }

    /**
     * Sets the status code.
     *
     * <p><code>status-code = 3DIGIT</code>
     *
     * @param statusCode the new status code.
     * @throws IllegalArgumentException if the given status code is not a (positive) 3 digit number.
     * @see #getStatusCode()
     * @since 2.7.0
     */
    public void setStatusCode(int statusCode) {
        if (statusCode < 100 || statusCode > 999) {
            throw new IllegalArgumentException(
                    "The status code must be a (positive) 3 digit number.");
        }
        this.mStatusCode = statusCode;
    }

    /**
     * Gets the reason phrase.
     *
     * @return the reason phrase.
     * @see #setReasonPhrase(String)
     */
    public String getReasonPhrase() {
        return mReasonPhrase;
    }

    /**
     * Sets the reason phrase.
     *
     * <p>If {@code null} it's set an empty string.
     *
     * @param reasonPhrase the new reason phrase.
     * @see #getReasonPhrase()
     * @since 2.7.0
     */
    public void setReasonPhrase(String reasonPhrase) {
        this.mReasonPhrase = reasonPhrase != null ? reasonPhrase : "";
    }

    private void parse() throws HttpMalformedHeaderException {

        Matcher matcher = patternStatusLine.matcher(mStartLine);
        if (!matcher.find()) {
            mMalformedHeader = true;
            throw new HttpMalformedHeaderException("Failed to find pattern: " + patternStatusLine);
        }

        mVersion = matcher.group(1);
        mStatusCodeString = matcher.group(2);
        setReasonPhrase(matcher.group(3));

        try {
            mStatusCode = Integer.parseInt(mStatusCodeString);
        } catch (NumberFormatException e) {
            mMalformedHeader = true;
            throw new HttpMalformedHeaderException("Unexpected status code: " + mStatusCodeString);
        }
    }

    @Override
    public int getContentLength() {
        int len = super.getContentLength();

        if ((mStatusCode >= 100 && mStatusCode < 200)
                || mStatusCode == HttpStatusCode.NO_CONTENT
                || mStatusCode == HttpStatusCode.NOT_MODIFIED) {
            return 0;
        } else if (mStatusCode >= 200 && mStatusCode < 300) {
            return len;
        } else if (len > 0) {
            return len;
        } else {
            return 0;
        }
    }

    public static HttpResponseHeader getError(String msg) {
        HttpResponseHeader res = null;
        try {
            res = new HttpResponseHeader(msg);
        } catch (HttpMalformedHeaderException e) {
        }
        return res;
    }

    @Override
    public boolean isImage() {
        return hasContentType(_CONTENT_TYPE_IMAGE);
    }

    @Override
    public boolean isText() {
        return hasContentType(
                _CONTENT_TYPE_TEXT,
                _CONTENT_TYPE_HTML,
                _CONTENT_TYPE_JAVASCRIPT,
                _CONTENT_TYPE_JSON,
                _CONTENT_TYPE_XML);
    }

    public boolean isHtml() {
        return hasContentType(_CONTENT_TYPE_HTML);
    }

    public boolean isXml() {
        return hasContentType(_CONTENT_TYPE_XML);
    }

    public boolean isJson() {
        return hasContentType(_CONTENT_TYPE_JSON);
    }

    public boolean isJavaScript() {
        return hasContentType(_CONTENT_TYPE_JAVASCRIPT);
    }

    public boolean isCss() {
        return hasContentType(_CONTENT_TYPE_CSS);
    }

    public static boolean isStatusLine(String data) {
        return patternPartialStatusLine.matcher(data).find();
    }

    @Override
    public String getPrimeHeader() {
        String prime = getVersion() + " " + getStatusCode();
        if (getReasonPhrase() != null && !getReasonPhrase().equals("")) {
            prime = prime + " " + getReasonPhrase();
        }
        return prime;
    }

    // ZAP: Added method for working directly with HttpCookie

    /**
     * Parses the response headers and build a lis of all the http cookies set. For the cookies
     * whose domain could not be determined, the {@code defaultDomain} is set.
     *
     * @param defaultDomain the default domain
     * @return the http cookies
     */
    public List<HttpCookie> getHttpCookies(String defaultDomain) {
        List<HttpCookie> cookies = new LinkedList<>();

        List<String> cookiesS = getHeaderValues(HttpHeader.SET_COOKIE);

        for (String c : cookiesS) {
            cookies.addAll(parseCookieString(c, defaultDomain));
        }

        cookiesS = getHeaderValues(HttpHeader.SET_COOKIE2);
        for (String c : cookiesS) {
            cookies.addAll(parseCookieString(c, defaultDomain));
        }

        return cookies;
    }

    private List<HttpCookie> parseCookieString(String c, String defaultDomain) {
        try {
            List<HttpCookie> parsedCookies = HttpCookie.parse(c);
            if (defaultDomain != null) {
                for (HttpCookie cookie : parsedCookies) {
                    if (cookie.getDomain() == null) {
                        cookie.setDomain(defaultDomain);
                    }
                }
            }
            return parsedCookies;
        } catch (IllegalArgumentException e) {
            if (c.indexOf(',') >= 0) {
                try {
                    // Some sites seem to use comma separators, which HttpCookie doesn't like, try
                    // replacing them
                    List<HttpCookie> parsedCookies = HttpCookie.parse(c.replace(',', ';'));
                    if (defaultDomain != null) {
                        for (HttpCookie cookie : parsedCookies) {
                            if (cookie.getDomain() == null) {
                                cookie.setDomain(defaultDomain);
                            }
                        }
                    }
                    return parsedCookies;
                } catch (IllegalArgumentException e2) {
                    log.error("Failed to parse cookie: {}", c, e);
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * Parses the response headers and build a lis of all the http cookies set. <br>
     * NOTE: For the cookies whose domain could not be determined, no domain is set, so this must be
     * taken into account.
     *
     * @return the http cookies
     * @deprecated Use the {@link #getHttpCookies(String)} method to take into account the default
     *     domain for cookie
     */
    @Deprecated
    public List<HttpCookie> getHttpCookies() {
        return getHttpCookies(null);
    }

    // ZAP: Added method.
    public TreeSet<HtmlParameter> getCookieParams() {
        TreeSet<HtmlParameter> set = new TreeSet<>();

        Iterator<String> cookiesIt = getHeaderValues(HttpHeader.SET_COOKIE).iterator();
        while (cookiesIt.hasNext()) {
            set.add(new HtmlParameter(cookiesIt.next()));
        }

        Iterator<String> cookies2It = getHeaderValues(HttpHeader.SET_COOKIE2).iterator();
        while (cookies2It.hasNext()) {
            set.add(new HtmlParameter(cookies2It.next()));
        }
        return set;
    }
}
