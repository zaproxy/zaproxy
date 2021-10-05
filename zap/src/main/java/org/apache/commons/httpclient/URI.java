/*
 * HeadURL: https://svn.apache.org/repos/asf/httpcomponents/oac.hc3x/trunk/src/java/org/apache/commons/httpclient/URI.java
 * Revision: 564973
 * Date: 2007-08-11 21:51:47 +0100 (Sat, 11 Aug 2007)
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.httpclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.BitSet;
import java.util.Hashtable;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.util.EncodingUtil;

/*
 * Forked class...
 * 
 * It was forked because ZAP depends (and uses) Commons HttpClient which is not compatible with, the newer version, 
 * HttpComponents Client.
 * 
 * Changes:
 *  - Removed the characters "$" from the previous SVN keywords (HeadURL, Revision and Date) to avoid accidental expansions;
 *  - Address some JavaDoc warns.
 *  - Add missing override and deprecated annotations;
 *  - Fix unchecked/rawtypes warnings (required by lint checks).
 *  - Allow to use underscores in hostnames.
 *  - Use neutral Locale when converting to lower case.
 *  - Allow to create a URI from the authority component.
 *  - Replace usages of StringBuffer with StringBuilder.
 */
/**
 * The interface for the URI(Uniform Resource Identifiers) version of RFC 2396.
 * This class has the purpose of supportting of parsing a URI reference to
 * extend any specific protocols, the character encoding of the protocol to 
 * be transported and the charset of the document.
 * <p>
 * A URI is always in an "escaped" form, since escaping or unescaping a
 * completed URI might change its semantics.  
 * <p>
 * Implementers should be careful not to escape or unescape the same string
 * more than once, since unescaping an already unescaped string might lead to
 * misinterpreting a percent data character as another escaped character,
 * or vice versa in the case of escaping an already escaped string.
 * <p>
 * In order to avoid these problems, data types used as follows:
 * <p><blockquote><pre>
 *   URI character sequence: char
 *   octet sequence: byte
 *   original character sequence: String
 * </pre></blockquote><p>
 *
 * So, a URI is a sequence of characters as an array of a char type, which
 * is not always represented as a sequence of octets as an array of byte.
 * <p>
 * 
 * URI Syntactic Components
 * <p><blockquote><pre>
 * - In general, written as follows:
 *   Absolute URI = &lt;scheme&gt;:&lt;scheme-specific-part&gt;
 *   Generic URI = &lt;scheme&gt;://&lt;authority&gt;&lt;path&gt;?&lt;query&gt;
 *
 * - Syntax
 *   absoluteURI   = scheme ":" ( hier_part | opaque_part )
 *   hier_part     = ( net_path | abs_path ) [ "?" query ]
 *   net_path      = "//" authority [ abs_path ]
 *   abs_path      = "/"  path_segments
 * </pre></blockquote><p>
 *
 * The following examples illustrate URI that are in common use.
 * <pre>
 * ftp://ftp.is.co.za/rfc/rfc1808.txt
 *    -- ftp scheme for File Transfer Protocol services
 * gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles
 *    -- gopher scheme for Gopher and Gopher+ Protocol services
 * http://www.math.uio.no/faq/compression-faq/part1.html
 *    -- http scheme for Hypertext Transfer Protocol services
 * mailto:mduerst@ifi.unizh.ch
 *    -- mailto scheme for electronic mail addresses
 * news:comp.infosystems.www.servers.unix
 *    -- news scheme for USENET news groups and articles
 * telnet://melvyl.ucop.edu/
 *    -- telnet scheme for interactive services via the TELNET Protocol
 * </pre>
 * Please, notice that there are many modifications from URL(RFC 1738) and
 * relative URL(RFC 1808).
 * <p>
 * <b>The expressions for a URI</b>
 * <p><pre>
 * For escaped URI forms
 *  - URI(char[]) // constructor
 *  - char[] getRawXxx() // method
 *  - String getEscapedXxx() // method
 *  - String toString() // method
 * <p>
 * For unescaped URI forms
 *  - URI(String) // constructor
 *  - String getXXX() // method
 * </pre><p>
 *
 * @author <a href="mailto:jericho@apache.org">Sung-Gu</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @version Revision: 564973 Date: 2002/03/14 15:14:01 
 */
public class URI implements Cloneable, Comparable<Object>, Serializable {


    // ----------------------------------------------------------- Constructors

    /** Create an instance as an internal use */
    protected URI() {
    }

    /**
     * Construct a URI from a string with the given charset. The input string can 
     * be either in escaped or unescaped form. 
     *
     * @param s URI character sequence
     * @param escaped <tt>true</tt> if URI character sequence is in escaped form. 
     *                <tt>false</tt> otherwise. 
     * @param charset the charset string to do escape encoding, if required
     * 
     * @throws URIException If the URI cannot be created.
     * @throws NullPointerException if input string is <code>null</code>
     * 
     * @see #getProtocolCharset
     * 
     * @since 3.0
     */
    public URI(String s, boolean escaped, String charset)
        throws URIException, NullPointerException {
        protocolCharset = charset;
        parseUriReference(s, escaped);
    }

    /**
     * Construct a URI from a string with the given charset. The input string can 
     * be either in escaped or unescaped form. 
     *
     * @param s URI character sequence
     * @param escaped <tt>true</tt> if URI character sequence is in escaped form. 
     *                <tt>false</tt> otherwise. 
     * 
     * @throws URIException If the URI cannot be created.
     * @throws NullPointerException if input string is <code>null</code>
     * 
     * @see #getProtocolCharset
     * 
     * @since 3.0
     */
    public URI(String s, boolean escaped)
        throws URIException, NullPointerException {
        parseUriReference(s, escaped);
    }

    /**
     * Construct a URI as an escaped form of a character array with the given
     * charset.
     *
     * @param escaped the URI character sequence
     * @param charset the charset string to do escape encoding
     * @throws URIException If the URI cannot be created.
     * @throws NullPointerException if <code>escaped</code> is <code>null</code>
     * @see #getProtocolCharset
     * 
     * @deprecated Use #URI(String, boolean, String)
     */
    @Deprecated
    public URI(char[] escaped, String charset) 
        throws URIException, NullPointerException {
        protocolCharset = charset;
        parseUriReference(new String(escaped), true);
    }


    /**
     * Construct a URI as an escaped form of a character array.
     * An URI can be placed within double-quotes or angle brackets like 
     * "http://test.com/" and &lt;http://test.com/&gt;
     * 
     * @param escaped the URI character sequence
     * @throws URIException If the URI cannot be created.
     * @throws NullPointerException if <code>escaped</code> is <code>null</code>
     * @see #getDefaultProtocolCharset
     * 
     * @deprecated Use #URI(String, boolean)
     */
    @Deprecated
    public URI(char[] escaped) 
        throws URIException, NullPointerException {
        parseUriReference(new String(escaped), true);
    }


    /**
     * Construct a URI from the given string with the given charset.
     *
     * @param original the string to be represented to URI character sequence
     * It is one of absoluteURI and relativeURI.
     * @param charset the charset string to do escape encoding
     * @throws URIException If the URI cannot be created.
     * @see #getProtocolCharset
     * 
     * @deprecated Use #URI(String, boolean, String)
     */
    @Deprecated
    public URI(String original, String charset) throws URIException {
        protocolCharset = charset;
        parseUriReference(original, false);
    }


    /**
     * Construct a URI from the given string.
     * <p><blockquote><pre>
     *   URI-reference = [ absoluteURI | relativeURI ] [ "#" fragment ]
     * </pre></blockquote><p>
     * An URI can be placed within double-quotes or angle brackets like 
     * "http://test.com/" and &lt;http://test.com/&gt;
     *
     * @param original the string to be represented to URI character sequence
     * It is one of absoluteURI and relativeURI.
     * @throws URIException If the URI cannot be created.
     * @see #getDefaultProtocolCharset
     * 
     * @deprecated Use #URI(String, boolean)
     */
    @Deprecated
    public URI(String original) throws URIException {
        parseUriReference(original, false);
    }


    /**
     * Construct a general URI from the given components.
     * <p><blockquote><pre>
     *   URI-reference = [ absoluteURI | relativeURI ] [ "#" fragment ]
     *   absoluteURI   = scheme ":" ( hier_part | opaque_part )
     *   opaque_part   = uric_no_slash *uric
     * </pre></blockquote><p>
     * It's for absolute URI = &lt;scheme&gt;:&lt;scheme-specific-part&gt;#
     * &lt;fragment&gt;.
     *
     * @param scheme the scheme string
     * @param schemeSpecificPart scheme_specific_part
     * @param fragment the fragment string
     * @throws URIException If the URI cannot be created.
     * @see #getDefaultProtocolCharset
     */
    public URI(String scheme, String schemeSpecificPart, String fragment)
        throws URIException {

        // validate and contruct the URI character sequence
        if (scheme == null) {
           throw new URIException(URIException.PARSING, "scheme required");
        }
        char[] s = scheme.toLowerCase(Locale.ROOT).toCharArray();
        if (validate(s, URI.scheme)) {
            _scheme = s; // is_absoluteURI
        } else {
            throw new URIException(URIException.PARSING, "incorrect scheme");
        }
        _opaque = encode(schemeSpecificPart, allowed_opaque_part,
                getProtocolCharset());
        // Set flag
        _is_opaque_part = true;
        _fragment = fragment == null ? null : fragment.toCharArray(); 
        setURI();
    }


    /**
     * Construct a general URI from the given components.
     * <p><blockquote><pre>
     *   URI-reference = [ absoluteURI | relativeURI ] [ "#" fragment ]
     *   absoluteURI   = scheme ":" ( hier_part | opaque_part )
     *   relativeURI   = ( net_path | abs_path | rel_path ) [ "?" query ]
     *   hier_part     = ( net_path | abs_path ) [ "?" query ]
     * </pre></blockquote><p>
     * It's for absolute URI = &lt;scheme&gt;:&lt;path&gt;?&lt;query&gt;#&lt;
     * fragment&gt; and relative URI = &lt;path&gt;?&lt;query&gt;#&lt;fragment
     * &gt;.
     *
     * @param scheme the scheme string
     * @param authority the authority string
     * @param path the path string
     * @param query the query string
     * @param fragment the fragment string
     * @throws URIException If the new URI cannot be created.
     * @see #getDefaultProtocolCharset
     */
    public URI(String scheme, String authority, String path, String query,
               String fragment) throws URIException {

        // validate and contruct the URI character sequence
        StringBuilder buff = new StringBuilder();
        if (scheme != null) {
            buff.append(scheme);
            buff.append(':');
        }
        if (authority != null) {
            buff.append("//");
            buff.append(authority);
        }
        if (path != null) {  // accept empty path
            if ((scheme != null || authority != null)
                    && !path.startsWith("/")) {
                throw new URIException(URIException.PARSING,
                        "abs_path requested");
            }
            buff.append(path);
        }
        if (query != null) {
            buff.append('?');
            buff.append(query);
        }
        if (fragment != null) {
            buff.append('#');
            buff.append(fragment);
        }
        parseUriReference(buff.toString(), false);
    }


    /**
     * Construct a general URI from the given components.
     *
     * @param scheme the scheme string
     * @param userinfo the userinfo string
     * @param host the host string
     * @param port the port number
     * @throws URIException If the new URI cannot be created.
     * @see #getDefaultProtocolCharset
     */
    public URI(String scheme, String userinfo, String host, int port)
        throws URIException {

        this(scheme, userinfo, host, port, null, null, null);
    }


    /**
     * Construct a general URI from the given components.
     *
     * @param scheme the scheme string
     * @param userinfo the userinfo string
     * @param host the host string
     * @param port the port number
     * @param path the path string
     * @throws URIException If the new URI cannot be created.
     * @see #getDefaultProtocolCharset
     */
    public URI(String scheme, String userinfo, String host, int port,
            String path) throws URIException {

        this(scheme, userinfo, host, port, path, null, null);
    }


    /**
     * Construct a general URI from the given components.
     *
     * @param scheme the scheme string
     * @param userinfo the userinfo string
     * @param host the host string
     * @param port the port number
     * @param path the path string
     * @param query the query string
     * @throws URIException If the new URI cannot be created.
     * @see #getDefaultProtocolCharset
     */
    public URI(String scheme, String userinfo, String host, int port,
            String path, String query) throws URIException {

        this(scheme, userinfo, host, port, path, query, null);
    }


    /**
     * Construct a general URI from the given components.
     *
     * @param scheme the scheme string
     * @param userinfo the userinfo string
     * @param host the host string
     * @param port the port number
     * @param path the path string
     * @param query the query string
     * @param fragment the fragment string
     * @throws URIException If the new URI cannot be created.
     * @see #getDefaultProtocolCharset
     */
    public URI(String scheme, String userinfo, String host, int port,
            String path, String query, String fragment) throws URIException {

        this(scheme, (host == null) ? null 
            : ((userinfo != null) ? userinfo + '@' : "") + host 
                + ((port != -1) ? ":" + port : ""), path, query, fragment);
    }


    /**
     * Construct a general URI from the given components.
     *
     * @param scheme the scheme string
     * @param host the host string
     * @param path the path string
     * @param fragment the fragment string
     * @throws URIException If the new URI cannot be created.
     * @see #getDefaultProtocolCharset
     */
    public URI(String scheme, String host, String path, String fragment)
        throws URIException {

        this(scheme, host, path, null, fragment);
    }


    /**
     * Construct a general URI with the given relative URI string.
     *
     * @param base the base URI
     * @param relative the relative URI string
     * @throws URIException If the new URI cannot be created.
     * 
     * @deprecated Use #URI(URI, String, boolean)
     */
    @Deprecated
    public URI(URI base, String relative) throws URIException {
        this(base, new URI(relative));
    }


    /**
     * Construct a general URI with the given relative URI string.
     *
     * @param base the base URI
     * @param relative the relative URI string
     * @param escaped <tt>true</tt> if URI character sequence is in escaped form. 
     *                <tt>false</tt> otherwise.
     *  
     * @throws URIException If the new URI cannot be created.
     * 
     * @since 3.0
     */
    public URI(URI base, String relative, boolean escaped) throws URIException {
        this(base, new URI(relative, escaped));
    }


    /**
     * Construct a general URI with the given relative URI.
     * <p><blockquote><pre>
     *   URI-reference = [ absoluteURI | relativeURI ] [ "#" fragment ]
     *   relativeURI   = ( net_path | abs_path | rel_path ) [ "?" query ]
     * </pre></blockquote><p>
     * Resolving Relative References to Absolute Form.
     *
     * <strong>Examples of Resolving Relative URI References</strong>
     *
     * Within an object with a well-defined base URI of
     * <p><blockquote><pre>
     *   http://a/b/c/d;p?q
     * </pre></blockquote><p>
     * the relative URI would be resolved as follows:
     *
     * Normal Examples
     *
     * <p><blockquote><pre>
     *   g:h           =  g:h
     *   g             =  http://a/b/c/g
     *   ./g           =  http://a/b/c/g
     *   g/            =  http://a/b/c/g/
     *   /g            =  http://a/g
     *   //g           =  http://g
     *   ?y            =  http://a/b/c/?y
     *   g?y           =  http://a/b/c/g?y
     *   #s            =  (current document)#s
     *   g#s           =  http://a/b/c/g#s
     *   g?y#s         =  http://a/b/c/g?y#s
     *   ;x            =  http://a/b/c/;x
     *   g;x           =  http://a/b/c/g;x
     *   g;x?y#s       =  http://a/b/c/g;x?y#s
     *   .             =  http://a/b/c/
     *   ./            =  http://a/b/c/
     *   ..            =  http://a/b/
     *   ../           =  http://a/b/
     *   ../g          =  http://a/b/g
     *   ../..         =  http://a/
     *   ../../        =  http://a/ 
     *   ../../g       =  http://a/g
     * </pre></blockquote><p>
     *
     * Some URI schemes do not allow a hierarchical syntax matching the
     * <hier_part> syntax, and thus cannot use relative references.
     *
     * @param base the base URI
     * @param relative the relative URI
     * @throws URIException If the new URI cannot be created.
     */
    public URI(URI base, URI relative) throws URIException {

        if (base._scheme == null) {
            throw new URIException(URIException.PARSING, "base URI required");
        }
        if (base._scheme != null) {
            this._scheme = base._scheme;
            this._authority = base._authority;
            this._is_net_path = base._is_net_path; 
        }
        if (base._is_opaque_part || relative._is_opaque_part) {
            this._scheme = base._scheme;
            this._is_opaque_part = base._is_opaque_part 
                || relative._is_opaque_part;
            this._opaque = relative._opaque;
            this._fragment = relative._fragment;
            this.setURI();
            return;
        }
        boolean schemesEqual = Arrays.equals(base._scheme,relative._scheme);
        if (relative._scheme != null 
                && (!schemesEqual  || relative._authority != null)) {
            this._scheme = relative._scheme;
            this._is_net_path = relative._is_net_path;
            this._authority = relative._authority;
            if (relative._is_server) {
                this._is_server = relative._is_server;
                this._userinfo = relative._userinfo;
                this._host = relative._host;
                this._port = relative._port;
            } else if (relative._is_reg_name) {
                this._is_reg_name = relative._is_reg_name;
            }
            this._is_abs_path = relative._is_abs_path;
            this._is_rel_path = relative._is_rel_path;
            this._path = relative._path;
        } else if (base._authority != null && relative._scheme == null) {
            this._is_net_path = base._is_net_path;
            this._authority = base._authority;
            if (base._is_server) {
                this._is_server = base._is_server;
                this._userinfo = base._userinfo;
                this._host = base._host;
                this._port = base._port;
            } else if (base._is_reg_name) {
                this._is_reg_name = base._is_reg_name;
            }
        }
        if (relative._authority != null) {
            this._is_net_path = relative._is_net_path;
            this._authority = relative._authority;
            if (relative._is_server) {
                this._is_server = relative._is_server;
                this._userinfo = relative._userinfo;
                this._host = relative._host;
                this._port = relative._port;
            } else if (relative._is_reg_name) {
                this._is_reg_name = relative._is_reg_name;
            }
            this._is_abs_path = relative._is_abs_path;
            this._is_rel_path = relative._is_rel_path;
            this._path = relative._path;
        }
        // resolve the path and query if necessary
        if (relative._authority == null 
            && (relative._scheme == null || schemesEqual)) {
            if ((relative._path == null || relative._path.length == 0)
                && relative._query == null) {
                // handle a reference to the current document, see RFC 2396 
                // section 5.2 step 2
                this._path = base._path;
                this._query = base._query;
            } else {
                this._path = resolvePath(base._path, relative._path);
            }
        }
        // base._query removed
        if (relative._query != null) {
            this._query = relative._query;
        }
        // base._fragment removed
        if (relative._fragment != null) {
            this._fragment = relative._fragment;
        }
        this.setURI();
        // reparse the newly built URI, this will ensure that all flags are set correctly.
        // TODO there must be a better way to do this
        parseUriReference(new String(_uri), true);
    }

    // --------------------------------------------------- Instance Variables

    /** Version ID for serialization */
    static final long serialVersionUID = 604752400577948726L;


    /**
     * Cache the hash code for this URI.
     */
    protected int hash = 0;


    /**
     * This Uniform Resource Identifier (URI).
     * The URI is always in an "escaped" form, since escaping or unescaping
     * a completed URI might change its semantics.  
     */
    protected char[] _uri = null;


    /**
     * The charset of the protocol used by this URI instance.
     */
    protected String protocolCharset = null;


    /**
     * The default charset of the protocol.  RFC 2277, 2396
     */
    protected static String defaultProtocolCharset = "UTF-8";


    /**
     * The default charset of the document.  RFC 2277, 2396
     * The platform's charset is used for the document by default.
     */
    protected static String defaultDocumentCharset = null;
    protected static String defaultDocumentCharsetByLocale = null;
    protected static String defaultDocumentCharsetByPlatform = null;
    // Static initializer for defaultDocumentCharset
    static {
        Locale locale = Locale.getDefault();
        // in order to support backward compatiblity
        if (locale != null) {
            defaultDocumentCharsetByLocale =
                LocaleToCharsetMap.getCharset(locale);
            // set the default document charset
            defaultDocumentCharset = defaultDocumentCharsetByLocale;
        }
        // in order to support platform encoding
        try {
            defaultDocumentCharsetByPlatform = System.getProperty("file.encoding");
        } catch (SecurityException ignore) {
        }
        if (defaultDocumentCharset == null) {
            // set the default document charset
            defaultDocumentCharset = defaultDocumentCharsetByPlatform;
        }
    }


    /**
     * The scheme.
     */
    protected char[] _scheme = null;


    /**
     * The opaque.
     */
    protected char[] _opaque = null;


    /**
     * The authority.
     */
    protected char[] _authority = null;


    /**
     * The userinfo.
     */
    protected char[] _userinfo = null;


    /**
     * The host.
     */
    protected char[] _host = null;


    /**
     * The port.
     */
    protected int _port = -1;


    /**
     * The path.
     */
    protected char[] _path = null;


    /**
     * The query.
     */
    protected char[] _query = null;


    /**
     * The fragment.
     */
    protected char[] _fragment = null;


    /**
     * The root path.
     */
    protected static final char[] rootPath = { '/' };

    // ---------------------- Generous characters for each component validation

    /**
     * The percent "%" character always has the reserved purpose of being the
     * escape indicator, it must be escaped as "%25" in order to be used as
     * data within a URI.
     */
    protected static final BitSet percent = new BitSet(256);
    // Static initializer for percent
    static {
        percent.set('%');
    }


    /**
     * BitSet for digit.
     * <p><blockquote><pre>
     * digit    = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" |
     *            "8" | "9"
     * </pre></blockquote><p>
     */
    protected static final BitSet digit = new BitSet(256);
    // Static initializer for digit
    static {
        for (int i = '0'; i <= '9'; i++) {
            digit.set(i);
        }
    }


    /**
     * BitSet for alpha.
     * <p><blockquote><pre>
     * alpha         = lowalpha | upalpha
     * </pre></blockquote><p>
     */
    protected static final BitSet alpha = new BitSet(256);
    // Static initializer for alpha
    static {
        for (int i = 'a'; i <= 'z'; i++) {
            alpha.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            alpha.set(i);
        }
    }


    /**
     * BitSet for alphanum (join of alpha &amp; digit).
     * <p><blockquote><pre>
     *  alphanum      = alpha | digit
     * </pre></blockquote><p>
     */
    protected static final BitSet alphanum = new BitSet(256);
    // Static initializer for alphanum
    static {
        alphanum.or(alpha);
        alphanum.or(digit);
    }


    /**
     * BitSet for hex.
     * <p><blockquote><pre>
     * hex           = digit | "A" | "B" | "C" | "D" | "E" | "F" |
     *                         "a" | "b" | "c" | "d" | "e" | "f"
     * </pre></blockquote><p>
     */
    protected static final BitSet hex = new BitSet(256);
    // Static initializer for hex
    static {
        hex.or(digit);
        for (int i = 'a'; i <= 'f'; i++) {
            hex.set(i);
        }
        for (int i = 'A'; i <= 'F'; i++) {
            hex.set(i);
        }
    }


    /**
     * BitSet for escaped.
     * <p><blockquote><pre>
     * escaped       = "%" hex hex
     * </pre></blockquote><p>
     */
    protected static final BitSet escaped = new BitSet(256);
    // Static initializer for escaped
    static {
        escaped.or(percent);
        escaped.or(hex);
    }


    /**
     * BitSet for mark.
     * <p><blockquote><pre>
     * mark          = "-" | "_" | "." | "!" | "~" | "*" | "'" |
     *                 "(" | ")"
     * </pre></blockquote><p>
     */
    protected static final BitSet mark = new BitSet(256);
    // Static initializer for mark
    static {
        mark.set('-');
        mark.set('_');
        mark.set('.');
        mark.set('!');
        mark.set('~');
        mark.set('*');
        mark.set('\'');
        mark.set('(');
        mark.set(')');
    }


    /**
     * Data characters that are allowed in a URI but do not have a reserved
     * purpose are called unreserved.
     * <p><blockquote><pre>
     * unreserved    = alphanum | mark
     * </pre></blockquote><p>
     */
    protected static final BitSet unreserved = new BitSet(256);
    // Static initializer for unreserved
    static {
        unreserved.or(alphanum);
        unreserved.or(mark);
    }


    /**
     * BitSet for reserved.
     * <p><blockquote><pre>
     * reserved      = ";" | "/" | "?" | ":" | "@" | "&amp;" | "=" | "+" |
     *                 "$" | ","
     * </pre></blockquote><p>
     */
    protected static final BitSet reserved = new BitSet(256);
    // Static initializer for reserved
    static {
        reserved.set(';');
        reserved.set('/');
        reserved.set('?');
        reserved.set(':');
        reserved.set('@');
        reserved.set('&');
        reserved.set('=');
        reserved.set('+');
        reserved.set('$');
        reserved.set(',');
    }


    /**
     * BitSet for uric.
     * <p><blockquote><pre>
     * uric          = reserved | unreserved | escaped
     * </pre></blockquote><p>
     */
    protected static final BitSet uric = new BitSet(256);
    // Static initializer for uric
    static {
        uric.or(reserved);
        uric.or(unreserved);
        uric.or(escaped);
    }


    /**
     * BitSet for fragment (alias for uric).
     * <p><blockquote><pre>
     * fragment      = *uric
     * </pre></blockquote><p>
     */
    protected static final BitSet fragment = uric;


    /**
     * BitSet for query (alias for uric).
     * <p><blockquote><pre>
     * query         = *uric
     * </pre></blockquote><p>
     */
    protected static final BitSet query = uric;


    /**
     * BitSet for pchar.
     * <p><blockquote><pre>
     * pchar         = unreserved | escaped |
     *                 ":" | "@" | "&amp;" | "=" | "+" | "$" | ","
     * </pre></blockquote><p>
     */
    protected static final BitSet pchar = new BitSet(256);
    // Static initializer for pchar
    static {
        pchar.or(unreserved);
        pchar.or(escaped);
        pchar.set(':');
        pchar.set('@');
        pchar.set('&');
        pchar.set('=');
        pchar.set('+');
        pchar.set('$');
        pchar.set(',');
    }


    /**
     * BitSet for param (alias for pchar).
     * <p><blockquote><pre>
     * param         = *pchar
     * </pre></blockquote><p>
     */
    protected static final BitSet param = pchar;


    /**
     * BitSet for segment.
     * <p><blockquote><pre>
     * segment       = *pchar *( ";" param )
     * </pre></blockquote><p>
     */
    protected static final BitSet segment = new BitSet(256);
    // Static initializer for segment
    static {
        segment.or(pchar);
        segment.set(';');
        segment.or(param);
    }


    /**
     * BitSet for path segments.
     * <p><blockquote><pre>
     * path_segments = segment *( "/" segment )
     * </pre></blockquote><p>
     */
    protected static final BitSet path_segments = new BitSet(256);
    // Static initializer for path_segments
    static {
        path_segments.set('/');
        path_segments.or(segment);
    }


    /**
     * URI absolute path.
     * <p><blockquote><pre>
     * abs_path      = "/"  path_segments
     * </pre></blockquote><p>
     */
    protected static final BitSet abs_path = new BitSet(256);
    // Static initializer for abs_path
    static {
        abs_path.set('/');
        abs_path.or(path_segments);
    }


    /**
     * URI bitset for encoding typical non-slash characters.
     * <p><blockquote><pre>
     * uric_no_slash = unreserved | escaped | ";" | "?" | ":" | "@" |
     *                 "&amp;" | "=" | "+" | "$" | ","
     * </pre></blockquote><p>
     */
    protected static final BitSet uric_no_slash = new BitSet(256);
    // Static initializer for uric_no_slash
    static {
        uric_no_slash.or(unreserved);
        uric_no_slash.or(escaped);
        uric_no_slash.set(';');
        uric_no_slash.set('?');
        uric_no_slash.set(';');
        uric_no_slash.set('@');
        uric_no_slash.set('&');
        uric_no_slash.set('=');
        uric_no_slash.set('+');
        uric_no_slash.set('$');
        uric_no_slash.set(',');
    }
    

    /**
     * URI bitset that combines uric_no_slash and uric.
     * <p><blockquote><pre>
     * opaque_part   = uric_no_slash *uric
     * </pre></blockquote><p>
     */
    protected static final BitSet opaque_part = new BitSet(256);
    // Static initializer for opaque_part
    static {
        // it's generous. because first character must not include a slash
        opaque_part.or(uric_no_slash);
        opaque_part.or(uric);
    }
    

    /**
     * URI bitset that combines absolute path and opaque part.
     * <p><blockquote><pre>
     * path          = [ abs_path | opaque_part ]
     * </pre></blockquote><p>
     */
    protected static final BitSet path = new BitSet(256);
    // Static initializer for path
    static {
        path.or(abs_path);
        path.or(opaque_part);
    }


    /**
     * Port, a logical alias for digit.
     */
    protected static final BitSet port = digit;


    /**
     * Bitset that combines digit and dot fo IPv$address.
     * <p><blockquote><pre>
     * IPv4address   = 1*digit "." 1*digit "." 1*digit "." 1*digit
     * </pre></blockquote><p>
     */
    protected static final BitSet IPv4address = new BitSet(256);
    // Static initializer for IPv4address
    static {
        IPv4address.or(digit);
        IPv4address.set('.');
    }


    /**
     * RFC 2373.
     * <p><blockquote><pre>
     * IPv6address = hexpart [ ":" IPv4address ]
     * </pre></blockquote><p>
     */
    protected static final BitSet IPv6address = new BitSet(256);
    // Static initializer for IPv6address reference
    static {
        IPv6address.or(hex); // hexpart
        IPv6address.set(':');
        IPv6address.or(IPv4address);
    }


    /**
     * RFC 2732, 2373.
     * <p><blockquote><pre>
     * IPv6reference   = "[" IPv6address "]"
     * </pre></blockquote><p>
     */
    protected static final BitSet IPv6reference = new BitSet(256);
    // Static initializer for IPv6reference
    static {
        IPv6reference.set('[');
        IPv6reference.or(IPv6address);
        IPv6reference.set(']');
    }


    /**
     * BitSet for toplabel.
     * <p><blockquote><pre>
     * toplabel      = alpha | alpha *( alphanum | "-" ) alphanum
     * </pre></blockquote><p>
     */
    protected static final BitSet toplabel = new BitSet(256);
    // Static initializer for toplabel
    static {
        toplabel.or(alphanum);
        toplabel.set('-');
    }


    /**
     * BitSet for domainlabel.
     * <p><blockquote><pre>
     * domainlabel   = alphanum | alphanum *( alphanum | "-" ) alphanum
     * </pre></blockquote><p>
     */
    protected static final BitSet domainlabel = toplabel;


    /**
     * BitSet for hostname.
     * <p><blockquote><pre>
     * hostname      = *( domainlabel "." ) toplabel [ "." ]
     * </pre></blockquote><p>
     */
    protected static final BitSet hostname = new BitSet(256);
    // Static initializer for hostname
    static {
        hostname.or(toplabel);
        // hostname.or(domainlabel);
        hostname.set('.');
        // ZAP: Allow underscores.
        hostname.set('_');
    }


    /**
     * BitSet for host.
     * <p><blockquote><pre>
     * host          = hostname | IPv4address | IPv6reference
     * </pre></blockquote><p>
     */
    protected static final BitSet host = new BitSet(256);
    // Static initializer for host
    static {
        host.or(hostname);
        // host.or(IPv4address);
        host.or(IPv6reference); // IPv4address
    }


    /**
     * BitSet for hostport.
     * <p><blockquote><pre>
     * hostport      = host [ ":" port ]
     * </pre></blockquote><p>
     */
    protected static final BitSet hostport = new BitSet(256);
    // Static initializer for hostport
    static {
        hostport.or(host);
        hostport.set(':');
        hostport.or(port);
    }


    /**
     * Bitset for userinfo.
     * <p><blockquote><pre>
     * userinfo      = *( unreserved | escaped |
     *                    ";" | ":" | "&amp;" | "=" | "+" | "$" | "," )
     * </pre></blockquote><p>
     */
    protected static final BitSet userinfo = new BitSet(256);
    // Static initializer for userinfo
    static {
        userinfo.or(unreserved);
        userinfo.or(escaped);
        userinfo.set(';');
        userinfo.set(':');
        userinfo.set('&');
        userinfo.set('=');
        userinfo.set('+');
        userinfo.set('$');
        userinfo.set(',');
    }


    /**
     * BitSet for within the userinfo component like user and password.
     */
    public static final BitSet within_userinfo = new BitSet(256);
    // Static initializer for within_userinfo
    static {
        within_userinfo.or(userinfo);
        within_userinfo.clear(';'); // reserved within authority
        within_userinfo.clear(':');
        within_userinfo.clear('@');
        within_userinfo.clear('?');
        within_userinfo.clear('/');
    }


    /**
     * Bitset for server.
     * <p><blockquote><pre>
     * server        = [ [ userinfo "@" ] hostport ]
     * </pre></blockquote><p>
     */
    protected static final BitSet server = new BitSet(256);
    // Static initializer for server
    static {
        server.or(userinfo);
        server.set('@');
        server.or(hostport);
    }


    /**
     * BitSet for reg_name.
     * <p><blockquote><pre>
     * reg_name      = 1*( unreserved | escaped | "$" | "," |
     *                     ";" | ":" | "@" | "&amp;" | "=" | "+" )
     * </pre></blockquote><p>
     */
    protected static final BitSet reg_name = new BitSet(256);
    // Static initializer for reg_name
    static {
        reg_name.or(unreserved);
        reg_name.or(escaped);
        reg_name.set('$');
        reg_name.set(',');
        reg_name.set(';');
        reg_name.set(':');
        reg_name.set('@');
        reg_name.set('&');
        reg_name.set('=');
        reg_name.set('+');
    }


    /**
     * BitSet for authority.
     * <p><blockquote><pre>
     * authority     = server | reg_name
     * </pre></blockquote><p>
     */
    protected static final BitSet authority = new BitSet(256);
    // Static initializer for authority
    static {
        authority.or(server);
        authority.or(reg_name);
    }


    /**
     * BitSet for scheme.
     * <p><blockquote><pre>
     * scheme        = alpha *( alpha | digit | "+" | "-" | "." )
     * </pre></blockquote><p>
     */
    protected static final BitSet scheme = new BitSet(256);
    // Static initializer for scheme
    static {
        scheme.or(alpha);
        scheme.or(digit);
        scheme.set('+');
        scheme.set('-');
        scheme.set('.');
    }


    /**
     * BitSet for rel_segment.
     * <p><blockquote><pre>
     * rel_segment   = 1*( unreserved | escaped |
     *                     ";" | "@" | "&amp;" | "=" | "+" | "$" | "," )
     * </pre></blockquote><p>
     */
    protected static final BitSet rel_segment = new BitSet(256);
    // Static initializer for rel_segment
    static {
        rel_segment.or(unreserved);
        rel_segment.or(escaped);
        rel_segment.set(';');
        rel_segment.set('@');
        rel_segment.set('&');
        rel_segment.set('=');
        rel_segment.set('+');
        rel_segment.set('$');
        rel_segment.set(',');
    }


    /**
     * BitSet for rel_path.
     * <p><blockquote><pre>
     * rel_path      = rel_segment [ abs_path ]
     * </pre></blockquote><p>
     */
    protected static final BitSet rel_path = new BitSet(256);
    // Static initializer for rel_path
    static {
        rel_path.or(rel_segment);
        rel_path.or(abs_path);
    }


    /**
     * BitSet for net_path.
     * <p><blockquote><pre>
     * net_path      = "//" authority [ abs_path ]
     * </pre></blockquote><p>
     */
    protected static final BitSet net_path = new BitSet(256);
    // Static initializer for net_path
    static {
        net_path.set('/');
        net_path.or(authority);
        net_path.or(abs_path);
    }
    

    /**
     * BitSet for hier_part.
     * <p><blockquote><pre>
     * hier_part     = ( net_path | abs_path ) [ "?" query ]
     * </pre></blockquote><p>
     */
    protected static final BitSet hier_part = new BitSet(256);
    // Static initializer for hier_part
    static {
        hier_part.or(net_path);
        hier_part.or(abs_path);
        // hier_part.set('?'); aleady included
        hier_part.or(query);
    }


    /**
     * BitSet for relativeURI.
     * <p><blockquote><pre>
     * relativeURI   = ( net_path | abs_path | rel_path ) [ "?" query ]
     * </pre></blockquote><p>
     */
    protected static final BitSet relativeURI = new BitSet(256);
    // Static initializer for relativeURI
    static {
        relativeURI.or(net_path);
        relativeURI.or(abs_path);
        relativeURI.or(rel_path);
        // relativeURI.set('?'); aleady included
        relativeURI.or(query);
    }


    /**
     * BitSet for absoluteURI.
     * <p><blockquote><pre>
     * absoluteURI   = scheme ":" ( hier_part | opaque_part )
     * </pre></blockquote><p>
     */
    protected static final BitSet absoluteURI = new BitSet(256);
    // Static initializer for absoluteURI
    static {
        absoluteURI.or(scheme);
        absoluteURI.set(':');
        absoluteURI.or(hier_part);
        absoluteURI.or(opaque_part);
    }


    /**
     * BitSet for URI-reference.
     * <p><blockquote><pre>
     * URI-reference = [ absoluteURI | relativeURI ] [ "#" fragment ]
     * </pre></blockquote><p>
     */
    protected static final BitSet URI_reference = new BitSet(256);
    // Static initializer for URI_reference
    static {
        URI_reference.or(absoluteURI);
        URI_reference.or(relativeURI);
        URI_reference.set('#');
        URI_reference.or(fragment);
    }

    // ---------------------------- Characters disallowed within the URI syntax
    // Excluded US-ASCII Characters are like control, space, delims and unwise

    /**
     * BitSet for control.
     */
    public static final BitSet control = new BitSet(256);
    // Static initializer for control
    static {
        for (int i = 0; i <= 0x1F; i++) {
            control.set(i);
        }
        control.set(0x7F);
    }

    /**
     * BitSet for space.
     */
    public static final BitSet space = new BitSet(256);
    // Static initializer for space
    static {
        space.set(0x20);
    }


    /**
     * BitSet for delims.
     */
    public static final BitSet delims = new BitSet(256);
    // Static initializer for delims
    static {
        delims.set('<');
        delims.set('>');
        delims.set('#');
        delims.set('%');
        delims.set('"');
    }


    /**
     * BitSet for unwise.
     */
    public static final BitSet unwise = new BitSet(256);
    // Static initializer for unwise
    static {
        unwise.set('{');
        unwise.set('}');
        unwise.set('|');
        unwise.set('\\');
        unwise.set('^');
        unwise.set('[');
        unwise.set(']');
        unwise.set('`');
    }


    /**
     * Disallowed rel_path before escaping.
     */
    public static final BitSet disallowed_rel_path = new BitSet(256);
    // Static initializer for disallowed_rel_path
    static {
        disallowed_rel_path.or(uric);
        disallowed_rel_path.andNot(rel_path);
    }


    /**
     * Disallowed opaque_part before escaping.
     */
    public static final BitSet disallowed_opaque_part = new BitSet(256);
    // Static initializer for disallowed_opaque_part
    static {
        disallowed_opaque_part.or(uric);
        disallowed_opaque_part.andNot(opaque_part);
    }

    // ----------------------- Characters allowed within and for each component

    /**
     * Those characters that are allowed for the authority component.
     */
    public static final BitSet allowed_authority = new BitSet(256);
    // Static initializer for allowed_authority
    static {
        allowed_authority.or(authority);
        allowed_authority.clear('%');
    }


    /**
     * Those characters that are allowed for the opaque_part.
     */
    public static final BitSet allowed_opaque_part = new BitSet(256);
    // Static initializer for allowed_opaque_part 
    static {
        allowed_opaque_part.or(opaque_part);
        allowed_opaque_part.clear('%');
    }


    /**
     * Those characters that are allowed for the reg_name.
     */
    public static final BitSet allowed_reg_name = new BitSet(256);
    // Static initializer for allowed_reg_name 
    static {
        allowed_reg_name.or(reg_name);
        // allowed_reg_name.andNot(percent);
        allowed_reg_name.clear('%');
    }


    /**
     * Those characters that are allowed for the userinfo component.
     */
    public static final BitSet allowed_userinfo = new BitSet(256);
    // Static initializer for allowed_userinfo
    static {
        allowed_userinfo.or(userinfo);
        // allowed_userinfo.andNot(percent);
        allowed_userinfo.clear('%');
    }


    /**
     * Those characters that are allowed for within the userinfo component.
     */
    public static final BitSet allowed_within_userinfo = new BitSet(256);
    // Static initializer for allowed_within_userinfo
    static {
        allowed_within_userinfo.or(within_userinfo);
        allowed_within_userinfo.clear('%');
    }


    /**
     * Those characters that are allowed for the IPv6reference component.
     * The characters '[', ']' in IPv6reference should be excluded.
     */
    public static final BitSet allowed_IPv6reference = new BitSet(256);
    // Static initializer for allowed_IPv6reference
    static {
        allowed_IPv6reference.or(IPv6reference);
        // allowed_IPv6reference.andNot(unwise);
        allowed_IPv6reference.clear('[');
        allowed_IPv6reference.clear(']');
    }


    /**
     * Those characters that are allowed for the host component.
     * The characters '[', ']' in IPv6reference should be excluded.
     */
    public static final BitSet allowed_host = new BitSet(256);
    // Static initializer for allowed_host
    static {
        allowed_host.or(hostname);
        allowed_host.or(allowed_IPv6reference);
    }


    /**
     * Those characters that are allowed for the authority component.
     */
    public static final BitSet allowed_within_authority = new BitSet(256);
    // Static initializer for allowed_within_authority
    static {
        allowed_within_authority.or(server);
        allowed_within_authority.or(reg_name);
        allowed_within_authority.clear(';');
        allowed_within_authority.clear(':');
        allowed_within_authority.clear('@');
        allowed_within_authority.clear('?');
        allowed_within_authority.clear('/');
    }


    /**
     * Those characters that are allowed for the abs_path.
     */
    public static final BitSet allowed_abs_path = new BitSet(256);
    // Static initializer for allowed_abs_path
    static {
        allowed_abs_path.or(abs_path);
        // allowed_abs_path.set('/');  // aleady included
        allowed_abs_path.andNot(percent);
        allowed_abs_path.clear('+');
    }


    /**
     * Those characters that are allowed for the rel_path.
     */
    public static final BitSet allowed_rel_path = new BitSet(256);
    // Static initializer for allowed_rel_path
    static {
        allowed_rel_path.or(rel_path);
        allowed_rel_path.clear('%');
        allowed_rel_path.clear('+');
    }


    /**
     * Those characters that are allowed within the path.
     */
    public static final BitSet allowed_within_path = new BitSet(256);
    // Static initializer for allowed_within_path
    static {
        allowed_within_path.or(abs_path);
        allowed_within_path.clear('/');
        allowed_within_path.clear(';');
        allowed_within_path.clear('=');
        allowed_within_path.clear('?');
    }


    /**
     * Those characters that are allowed for the query component.
     */
    public static final BitSet allowed_query = new BitSet(256);
    // Static initializer for allowed_query
    static {
        allowed_query.or(uric);
        allowed_query.clear('%');
    }


    /**
     * Those characters that are allowed within the query component.
     */
    public static final BitSet allowed_within_query = new BitSet(256);
    // Static initializer for allowed_within_query
    static {
        allowed_within_query.or(allowed_query);
        allowed_within_query.andNot(reserved); // excluded 'reserved'
    }


    /**
     * Those characters that are allowed for the fragment component.
     */
    public static final BitSet allowed_fragment = new BitSet(256);
    // Static initializer for allowed_fragment
    static {
        allowed_fragment.or(uric);
        allowed_fragment.clear('%');
    }

    // ------------------------------------------- Flags for this URI-reference

    // TODO: Figure out what all these variables are for and provide javadoc

    // URI-reference = [ absoluteURI | relativeURI ] [ "#" fragment ]
    // absoluteURI   = scheme ":" ( hier_part | opaque_part )
    protected boolean _is_hier_part;
    protected boolean _is_opaque_part;
    // relativeURI   = ( net_path | abs_path | rel_path ) [ "?" query ] 
    // hier_part     = ( net_path | abs_path ) [ "?" query ]
    protected boolean _is_net_path;
    protected boolean _is_abs_path;
    protected boolean _is_rel_path;
    // net_path      = "//" authority [ abs_path ] 
    // authority     = server | reg_name
    protected boolean _is_reg_name;
    protected boolean _is_server;  // = _has_server
    // server        = [ [ userinfo "@" ] hostport ]
    // host          = hostname | IPv4address | IPv6reference
    protected boolean _is_hostname;
    protected boolean _is_IPv4address;
    protected boolean _is_IPv6reference;

    // ------------------------------------------ Character and escape encoding
    
    /**
     * Encodes URI string.
     *
     * This is a two mapping, one from original characters to octets, and
     * subsequently a second from octets to URI characters:
     * <p><blockquote><pre>
     *   original character sequence-&gt;octet sequence-&gt;URI character sequence
     * </pre></blockquote><p>
     *
     * An escaped octet is encoded as a character triplet, consisting of the
     * percent character "%" followed by the two hexadecimal digits
     * representing the octet code. For example, "%20" is the escaped
     * encoding for the US-ASCII space character.
     * <p>
     * Conversion from the local filesystem character set to UTF-8 will
     * normally involve a two step process. First convert the local character
     * set to the UCS; then convert the UCS to UTF-8.
     * The first step in the process can be performed by maintaining a mapping
     * table that includes the local character set code and the corresponding
     * UCS code.
     * The next step is to convert the UCS character code to the UTF-8 encoding.
     * <p>
     * Mapping between vendor codepages can be done in a very similar manner
     * as described above.
     * <p>
     * The only time escape encodings can allowedly be made is when a URI is
     * being created from its component parts.  The escape and validate methods
     * are internally performed within this method.
     *
     * @param original the original character sequence
     * @param allowed those characters that are allowed within a component
     * @param charset the protocol charset
     * @return URI character sequence
     * @throws URIException null component or unsupported character encoding
     */
        
    protected static char[] encode(String original, BitSet allowed,
            String charset) throws URIException {
        if (original == null) {
            throw new IllegalArgumentException("Original string may not be null");
        }
        if (allowed == null) {
            throw new IllegalArgumentException("Allowed bitset may not be null");
        }
        byte[] rawdata = URLCodec.encodeUrl(allowed, EncodingUtil.getBytes(original, charset));
        return EncodingUtil.getAsciiString(rawdata).toCharArray();
    }

    /**
     * Decodes URI encoded string.
     *
     * This is a two mapping, one from URI characters to octets, and
     * subsequently a second from octets to original characters:
     * <p><blockquote><pre>
     *   URI character sequence-&gt;octet sequence-&gt;original character sequence
     * </pre></blockquote><p>
     *
     * A URI must be separated into its components before the escaped
     * characters within those components can be allowedly decoded.
     * <p>
     * Notice that there is a chance that URI characters that are non UTF-8
     * may be parsed as valid UTF-8.  A recent non-scientific analysis found
     * that EUC encoded Japanese words had a 2.7% false reading; SJIS had a
     * 0.0005% false reading; other encoding such as ASCII or KOI-8 have a 0%
     * false reading.
     * <p>
     * The percent "%" character always has the reserved purpose of being
     * the escape indicator, it must be escaped as "%25" in order to be used
     * as data within a URI.
     * <p>
     * The unescape method is internally performed within this method.
     *
     * @param component the URI character sequence
     * @param charset the protocol charset
     * @return original character sequence
     * @throws URIException incomplete trailing escape pattern or unsupported
     * character encoding
     */
    protected static String decode(char[] component, String charset) 
        throws URIException {
        if (component == null) {
            throw new IllegalArgumentException("Component array of chars may not be null");
        }
        return decode(new String(component), charset);
    }

    /**
     * Decodes URI encoded string.
     *
     * This is a two mapping, one from URI characters to octets, and
     * subsequently a second from octets to original characters:
     * <p><blockquote><pre>
     *   URI character sequence-&gt;octet sequence-&gt;original character sequence
     * </pre></blockquote><p>
     *
     * A URI must be separated into its components before the escaped
     * characters within those components can be allowedly decoded.
     * <p>
     * Notice that there is a chance that URI characters that are non UTF-8
     * may be parsed as valid UTF-8.  A recent non-scientific analysis found
     * that EUC encoded Japanese words had a 2.7% false reading; SJIS had a
     * 0.0005% false reading; other encoding such as ASCII or KOI-8 have a 0%
     * false reading.
     * <p>
     * The percent "%" character always has the reserved purpose of being
     * the escape indicator, it must be escaped as "%25" in order to be used
     * as data within a URI.
     * <p>
     * The unescape method is internally performed within this method.
     *
     * @param component the URI character sequence
     * @param charset the protocol charset
     * @return original character sequence
     * @throws URIException incomplete trailing escape pattern or unsupported
     * character encoding
     * 
     * @since 3.0
     */
    protected static String decode(String component, String charset) 
        throws URIException {
        if (component == null) {
            throw new IllegalArgumentException("Component array of chars may not be null");
        }
        byte[] rawdata = null;
        try { 
            rawdata = URLCodec.decodeUrl(EncodingUtil.getAsciiBytes(component));
        } catch (DecoderException e) {
            throw new URIException(e.getMessage());
        }
        return EncodingUtil.getString(rawdata, charset);
    }
    /**
     * Pre-validate the unescaped URI string within a specific component.
     *
     * @param component the component string within the component
     * @param disallowed those characters disallowed within the component
     * @return if true, it doesn't have the disallowed characters
     * if false, the component is undefined or an incorrect one
     */
    protected boolean prevalidate(String component, BitSet disallowed) {
        // prevalidate the given component by disallowed characters
        if (component == null) {
            return false; // undefined
        }
        char[] target = component.toCharArray();
        for (int i = 0; i < target.length; i++) {
            if (disallowed.get(target[i])) {
                return false;
            }
        }
        return true;
    }


    /**
     * Validate the URI characters within a specific component.
     * The component must be performed after escape encoding. Or it doesn't
     * include escaped characters.
     *
     * @param component the characters sequence within the component
     * @param generous those characters that are allowed within a component
     * @return if true, it's the correct URI character sequence
     */
    protected boolean validate(char[] component, BitSet generous) {
        // validate each component by generous characters
        return validate(component, 0, -1, generous);
    }


    /**
     * Validate the URI characters within a specific component.
     * The component must be performed after escape encoding. Or it doesn't
     * include escaped characters.
     * <p>
     * It's not that much strict, generous.  The strict validation might be 
     * performed before being called this method.
     *
     * @param component the characters sequence within the component
     * @param soffset the starting offset of the given component
     * @param eoffset the ending offset of the given component
     * if -1, it means the length of the component
     * @param generous those characters that are allowed within a component
     * @return if true, it's the correct URI character sequence
     */
    protected boolean validate(char[] component, int soffset, int eoffset,
            BitSet generous) {
        // validate each component by generous characters
        if (eoffset == -1) {
            eoffset = component.length - 1;
        }
        for (int i = soffset; i <= eoffset; i++) {
            if (!generous.get(component[i])) { 
                return false;
            }
        }
        return true;
    }


    /**
     * In order to avoid any possilbity of conflict with non-ASCII characters,
     * Parse a URI reference as a <code>String</code> with the character
     * encoding of the local system or the document.
     * <p>
     * The following line is the regular expression for breaking-down a URI
     * reference into its components.
     * <p><blockquote><pre>
     *   ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
     *    12            3  4          5       6  7        8 9
     * </pre></blockquote><p>
     * For example, matching the above expression to
     *   http://jakarta.apache.org/ietf/uri/#Related
     * results in the following subexpression matches:
     * <p><blockquote><pre>
     *               $1 = http:
     *  scheme    =  $2 = http
     *               $3 = //jakarta.apache.org
     *  authority =  $4 = jakarta.apache.org
     *  path      =  $5 = /ietf/uri/
     *               $6 = <undefined>
     *  query     =  $7 = <undefined>
     *               $8 = #Related
     *  fragment  =  $9 = Related
     * </pre></blockquote><p>
     *
     * @param original the original character sequence
     * @param escaped <code>true</code> if <code>original</code> is escaped
     * @throws URIException If an error occurs.
     */
    protected void parseUriReference(String original, boolean escaped)
        throws URIException {

        // validate and contruct the URI character sequence
        if (original == null) {
            throw new URIException("URI-Reference required");
        }

        /* @
         *  ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
         */
        String tmp = original.trim();
        
        /*
         * The length of the string sequence of characters.
         * It may not be equal to the length of the byte array.
         */
        int length = tmp.length();

        /*
         * Remove the delimiters like angle brackets around an URI.
         */
        if (length > 0) {
            char[] firstDelimiter = { tmp.charAt(0) };
            if (validate(firstDelimiter, delims)) {
                if (length >= 2) {
                    char[] lastDelimiter = { tmp.charAt(length - 1) };
                    if (validate(lastDelimiter, delims)) {
                        tmp = tmp.substring(1, length - 1);
                        length = length - 2;
                    }
                }
            }
        }

        /*
         * The starting index
         */
        int from = 0;

        /*
         * The test flag whether the URI is started from the path component.
         */
        boolean isStartedFromPath = false;
        int atColon = tmp.indexOf(':');
        int atSlash = tmp.indexOf('/');
        if ((atColon <= 0 && !tmp.startsWith("//"))
            || (atSlash >= 0 && atSlash < atColon)) {
            isStartedFromPath = true;
        }

        /*
         * <p><blockquote><pre>
         *     @@@@@@@@
         *  ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
         * </pre></blockquote><p>
         */
        int at = indexFirstOf(tmp, isStartedFromPath ? "/?#" : ":/?#", from);
        if (at == -1) { 
            at = 0;
        }

        /*
         * Parse the scheme.
         * <p><blockquote><pre>
         *  scheme    =  $2 = http
         *              @
         *  ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
         * </pre></blockquote><p>
         */
        if (at > 0 && at < length && tmp.charAt(at) == ':') {
            char[] target = tmp.substring(0, at).toLowerCase(Locale.ROOT).toCharArray();
            if (validate(target, scheme)) {
                _scheme = target;
            } else {
                throw new URIException("incorrect scheme");
            }
            from = ++at;
        }

        /*
         * Parse the authority component.
         * <p><blockquote><pre>
         *  authority =  $4 = jakarta.apache.org
         *                  @@
         *  ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
         * </pre></blockquote><p>
         */
        // Reset flags
        _is_net_path = _is_abs_path = _is_rel_path = _is_hier_part = false;
        if (0 <= at && at < length && tmp.charAt(at) == '/') {
            // Set flag
            _is_hier_part = true;
            if (at + 2 < length && tmp.charAt(at + 1) == '/' 
                && !isStartedFromPath) {
                // the temporary index to start the search from
                int next = indexFirstOf(tmp, "/?#", at + 2);
                if (next == -1) {
                    next = (tmp.substring(at + 2).length() == 0) ? at + 2 
                        : tmp.length();
                }
                parseAuthority(tmp.substring(at + 2, next), escaped);
                from = at = next;
                // Set flag
                _is_net_path = true;
            }
            if (from == at) {
                // Set flag
                _is_abs_path = true;
            }
        }

        /*
         * Parse the path component.
         * <p><blockquote><pre>
         *  path      =  $5 = /ietf/uri/
         *                                @@@@@@
         *  ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
         * </pre></blockquote><p>
         */
        if (from < length) {
            // rel_path = rel_segment [ abs_path ]
            int next = indexFirstOf(tmp, "?#", from);
            if (next == -1) {
                next = tmp.length();
            }
            if (!_is_abs_path) {
                if (!escaped 
                    && prevalidate(tmp.substring(from, next), disallowed_rel_path) 
                    || escaped 
                    && validate(tmp.substring(from, next).toCharArray(), rel_path)) {
                    // Set flag
                    _is_rel_path = true;
                } else if (!escaped 
                    && prevalidate(tmp.substring(from, next), disallowed_opaque_part) 
                    || escaped 
                    && validate(tmp.substring(from, next).toCharArray(), opaque_part)) {
                    // Set flag
                    _is_opaque_part = true;
                } else {
                    // the path component may be empty
                    _path = null;
                }
            }
            String s = tmp.substring(from, next);
            if (escaped) {
                setRawPath(s.toCharArray());
            } else {
                setPath(s);
            }
            at = next;
        }

        // set the charset to do escape encoding
        String charset = getProtocolCharset();

        /*
         * Parse the query component.
         * <p><blockquote><pre>
         *  query     =  $7 = <undefined>
         *                                        @@@@@@@@@
         *  ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
         * </pre></blockquote><p>
         */
        if (0 <= at && at + 1 < length && tmp.charAt(at) == '?') {
            int next = tmp.indexOf('#', at + 1);
            if (next == -1) {
                next = tmp.length();
            }
            if (escaped) {
                _query = tmp.substring(at + 1, next).toCharArray();
                if (!validate(_query, uric)) {
                    throw new URIException("Invalid query");
                }
            } else {
                _query = encode(tmp.substring(at + 1, next), allowed_query, charset);
            }
            at = next;
        }

        /*
         * Parse the fragment component.
         * <p><blockquote><pre>
         *  fragment  =  $9 = Related
         *                                                   @@@@@@@@
         *  ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
         * </pre></blockquote><p>
         */
        if (0 <= at && at + 1 <= length && tmp.charAt(at) == '#') {
            if (at + 1 == length) { // empty fragment
                _fragment = "".toCharArray();
            } else {
                _fragment = (escaped) ? tmp.substring(at + 1).toCharArray() 
                    : encode(tmp.substring(at + 1), allowed_fragment, charset);
            }
        }

        // set this URI.
        setURI();
    }


    /**
     * Get the earlier index that to be searched for the first occurrance in
     * one of any of the given string.
     *
     * @param s the string to be indexed
     * @param delims the delimiters used to index
     * @return the earlier index if there are delimiters
     */
    protected int indexFirstOf(String s, String delims) {
        return indexFirstOf(s, delims, -1);
    }


    /**
     * Get the earlier index that to be searched for the first occurrance in
     * one of any of the given string.
     *
     * @param s the string to be indexed
     * @param delims the delimiters used to index
     * @param offset the from index
     * @return the earlier index if there are delimiters
     */
    protected int indexFirstOf(String s, String delims, int offset) {
        if (s == null || s.length() == 0) {
            return -1;
        }
        if (delims == null || delims.length() == 0) {
            return -1;
        }
        // check boundaries
        if (offset < 0) {
            offset = 0;
        } else if (offset > s.length()) {
            return -1;
        }
        // s is never null
        int min = s.length();
        char[] delim = delims.toCharArray();
        for (int i = 0; i < delim.length; i++) {
            int at = s.indexOf(delim[i], offset);
            if (at >= 0 && at < min) {
                min = at;
            }
        }
        return (min == s.length()) ? -1 : min;
    }


    /**
     * Get the earlier index that to be searched for the first occurrance in
     * one of any of the given array.
     *
     * @param s the character array to be indexed
     * @param delim the delimiter used to index
     * @return the ealier index if there are a delimiter
     */
    protected int indexFirstOf(char[] s, char delim) {
        return indexFirstOf(s, delim, 0);
    }


    /**
     * Get the earlier index that to be searched for the first occurrance in
     * one of any of the given array.
     *
     * @param s the character array to be indexed
     * @param delim the delimiter used to index
     * @param offset The offset.
     * @return the ealier index if there is a delimiter
     */
    protected int indexFirstOf(char[] s, char delim, int offset) {
        if (s == null || s.length == 0) {
            return -1;
        }
        // check boundaries
        if (offset < 0) {
            offset = 0;
        } else if (offset > s.length) {
            return -1;
        }
        for (int i = offset; i < s.length; i++) {
            if (s[i] == delim) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Creates a {@code URI} from the given authority.
     *
     * <p><strong>Note:</strong> Not part of the public API.
     *
     * @param authority the authority component.
     * @return the URI.
     * @throws URIException if an error occurred while parsing the authority.
     */
    public static URI fromAuthority(String authority) throws URIException {
        URI uri = new URI();
        uri.parseAuthority(authority, true);
        uri.setURI();
        uri._uri = authority.toCharArray();
        return uri;
    }

    /**
     * Parse the authority component.
     *
     * @param original the original character sequence of authority component
     * @param escaped <code>true</code> if <code>original</code> is escaped
     * @throws URIException If an error occurs.
     */
    protected void parseAuthority(String original, boolean escaped)
        throws URIException {

        // Reset flags
        _is_reg_name = _is_server =
        _is_hostname = _is_IPv4address = _is_IPv6reference = false;

        // set the charset to do escape encoding
        String charset = getProtocolCharset();

        boolean hasPort = true;
        int from = 0;
        int next = original.indexOf('@');
        if (next != -1) { // neither -1 and 0
            // each protocol extented from URI supports the specific userinfo
            _userinfo = (escaped) ? original.substring(0, next).toCharArray() 
                : encode(original.substring(0, next), allowed_userinfo,
                        charset);
            from = next + 1;
        }
        next = original.indexOf('[', from);
        if (next >= from) {
            next = original.indexOf(']', from);
            if (next == -1) {
                throw new URIException(URIException.PARSING, "IPv6reference");
            } else {
                next++;
            }
            // In IPv6reference, '[', ']' should be excluded
            _host = (escaped) ? original.substring(from, next).toCharArray() 
                : encode(original.substring(from, next), allowed_IPv6reference,
                        charset);
            // Set flag
            _is_IPv6reference = true;
        } else { // only for !_is_IPv6reference
            next = original.indexOf(':', from);
            if (next == -1) {
                next = original.length();
                hasPort = false;
            }
            // REMINDME: it doesn't need the pre-validation
            _host = original.substring(from, next).toCharArray();
            if (validate(_host, IPv4address)) {
                // Set flag
                _is_IPv4address = true;
            } else if (validate(_host, hostname)) {
                // Set flag
                _is_hostname = true;
            } else {
                // Set flag
                _is_reg_name = true;
            }
        }
        if (_is_reg_name) {
            // Reset flags for a server-based naming authority
            _is_server = _is_hostname = _is_IPv4address =
            _is_IPv6reference = false;
            // set a registry-based naming authority
            if (escaped) {
                _authority = original.toCharArray();
                if (!validate(_authority, reg_name)) {
                    throw new URIException("Invalid authority");
                }
            } else {
                _authority = encode(original, allowed_reg_name, charset);
            }
        } else {
            if (original.length() - 1 > next && hasPort 
                && original.charAt(next) == ':') { // not empty
                from = next + 1;
                try {
                    _port = Integer.parseInt(original.substring(from));
                } catch (NumberFormatException error) {
                    throw new URIException(URIException.PARSING,
                            "invalid port number");
                }
            }
            // set a server-based naming authority
            StringBuilder buf = new StringBuilder();
            if (_userinfo != null) { // has_userinfo
                buf.append(_userinfo);
                buf.append('@');
            }
            if (_host != null) {
                buf.append(_host);
                if (_port != -1) {
                    buf.append(':');
                    buf.append(_port);
                }
            }
            _authority = buf.toString().toCharArray();
            // Set flag
            _is_server = true;
        }
    }


    /**
     * Once it's parsed successfully, set this URI.
     *
     * @see #getRawURI
     */
    protected void setURI() {
        // set _uri
        StringBuilder buf = new StringBuilder();
        // ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
        if (_scheme != null) {
            buf.append(_scheme);
            buf.append(':');
        }
        if (_is_net_path) {
            buf.append("//");
            if (_authority != null) { // has_authority
                buf.append(_authority);
            }
        }
        if (_opaque != null && _is_opaque_part) {
            buf.append(_opaque);
        } else if (_path != null) {
            // _is_hier_part or _is_relativeURI
            if (_path.length != 0) {
                buf.append(_path);
            }
        }
        if (_query != null) { // has_query
            buf.append('?');
            buf.append(_query);
        }
        // ignore the fragment identifier
        _uri = buf.toString().toCharArray();
        hash = 0;
    }

    // ----------------------------------------------------------- Test methods
  

    /**
     * Tell whether or not this URI is absolute.
     *
     * @return true iif this URI is absoluteURI
     */
    public boolean isAbsoluteURI() {
        return (_scheme != null);
    }
  

    /**
     * Tell whether or not this URI is relative.
     *
     * @return true iif this URI is relativeURI
     */
    public boolean isRelativeURI() {
        return (_scheme == null);
    }


    /**
     * Tell whether or not the absoluteURI of this URI is hier_part.
     *
     * @return true iif the absoluteURI is hier_part
     */
    public boolean isHierPart() {
        return _is_hier_part;
    }


    /**
     * Tell whether or not the absoluteURI of this URI is opaque_part.
     *
     * @return true iif the absoluteURI is opaque_part
     */
    public boolean isOpaquePart() {
        return _is_opaque_part;
    }


    /**
     * Tell whether or not the relativeURI or heir_part of this URI is net_path.
     * It's the same function as the has_authority() method.
     *
     * @return true iif the relativeURI or heir_part is net_path
     * @see #hasAuthority
     */
    public boolean isNetPath() {
        return _is_net_path || (_authority != null);
    }


    /**
     * Tell whether or not the relativeURI or hier_part of this URI is abs_path.
     *
     * @return true iif the relativeURI or hier_part is abs_path
     */
    public boolean isAbsPath() {
        return _is_abs_path;
    }


    /**
     * Tell whether or not the relativeURI of this URI is rel_path.
     *
     * @return true iif the relativeURI is rel_path
     */
    public boolean isRelPath() {
        return _is_rel_path;
    }


    /**
     * Tell whether or not this URI has authority.
     * It's the same function as the is_net_path() method.
     *
     * @return true iif this URI has authority
     * @see #isNetPath
     */
    public boolean hasAuthority() {
        return (_authority != null) || _is_net_path;
    }

    /**
     * Tell whether or not the authority component of this URI is reg_name.
     *
     * @return true iif the authority component is reg_name
     */
    public boolean isRegName() {
        return _is_reg_name;
    }
  

    /**
     * Tell whether or not the authority component of this URI is server.
     *
     * @return true iif the authority component is server
     */
    public boolean isServer() {
        return _is_server;
    }
  

    /**
     * Tell whether or not this URI has userinfo.
     *
     * @return true iif this URI has userinfo
     */
    public boolean hasUserinfo() {
        return (_userinfo != null);
    }
  

    /**
     * Tell whether or not the host part of this URI is hostname.
     *
     * @return true iif the host part is hostname
     */
    public boolean isHostname() {
        return _is_hostname;
    }


    /**
     * Tell whether or not the host part of this URI is IPv4address.
     *
     * @return true iif the host part is IPv4address
     */
    public boolean isIPv4address() {
        return _is_IPv4address;
    }


    /**
     * Tell whether or not the host part of this URI is IPv6reference.
     *
     * @return true iif the host part is IPv6reference
     */
    public boolean isIPv6reference() {
        return _is_IPv6reference;
    }


    /**
     * Tell whether or not this URI has query.
     *
     * @return true iif this URI has query
     */
    public boolean hasQuery() {
        return (_query != null);
    }
   

    /**
     * Tell whether or not this URI has fragment.
     *
     * @return true iif this URI has fragment
     */
    public boolean hasFragment() {
        return (_fragment != null);
    }
   
   
    // ---------------------------------------------------------------- Charset


    /**
     * Set the default charset of the protocol.
     * <p>
     * The character set used to store files SHALL remain a local decision and
     * MAY depend on the capability of local operating systems. Prior to the
     * exchange of URIs they SHOULD be converted into a ISO/IEC 10646 format
     * and UTF-8 encoded. This approach, while allowing international exchange
     * of URIs, will still allow backward compatibility with older systems
     * because the code set positions for ASCII characters are identical to the
     * one byte sequence in UTF-8.
     * <p>
     * An individual URI scheme may require a single charset, define a default
     * charset, or provide a way to indicate the charset used.
     *
     * <p>
     * Always all the time, the setter method is always succeeded and throws
     * <code>DefaultCharsetChanged</code> exception.
     *
     * So API programmer must follow the following way:
     * <code><pre>
     *  import org.apache.util.URI$DefaultCharsetChanged;
     *      .
     *      .
     *      .
     *  try {
     *      URI.setDefaultProtocolCharset("UTF-8");
     *  } catch (DefaultCharsetChanged cc) {
     *      // CASE 1: the exception could be ignored, when it is set by user
     *      if (cc.getReasonCode() == DefaultCharsetChanged.PROTOCOL_CHARSET) {
     *      // CASE 2: let user know the default protocol charset changed
     *      } else {
     *      // CASE 2: let user know the default document charset changed
     *      }
     *  }
     *  </pre></code>
     *
     * The API programmer is responsible to set the correct charset.
     * And each application should remember its own charset to support.
     *
     * @param charset the default charset for each protocol
     * @throws DefaultCharsetChanged default charset changed
     */
    public static void setDefaultProtocolCharset(String charset) 
        throws DefaultCharsetChanged {
            
        defaultProtocolCharset = charset;
        throw new DefaultCharsetChanged(DefaultCharsetChanged.PROTOCOL_CHARSET,
                "the default protocol charset changed");
    }


    /**
     * Get the default charset of the protocol.
     * <p>
     * An individual URI scheme may require a single charset, define a default
     * charset, or provide a way to indicate the charset used.
     * <p>
     * To work globally either requires support of a number of character sets
     * and to be able to convert between them, or the use of a single preferred
     * character set.
     * For support of global compatibility it is STRONGLY RECOMMENDED that
     * clients and servers use UTF-8 encoding when exchanging URIs.
     *
     * @return the default charset string
     */
    public static String getDefaultProtocolCharset() {
        return defaultProtocolCharset;
    }


    /**
     * Get the protocol charset used by this current URI instance.
     * It was set by the constructor for this instance. If it was not set by
     * contructor, it will return the default protocol charset.
     *
     * @return the protocol charset string
     * @see #getDefaultProtocolCharset
     */
    public String getProtocolCharset() {
        return (protocolCharset != null) 
            ? protocolCharset 
            : defaultProtocolCharset;
    }


    /**
     * Set the default charset of the document.
     * <p>
     * Notice that it will be possible to contain mixed characters (e.g.
     * ftp://host/KoreanNamespace/ChineseResource). To handle the Bi-directional
     * display of these character sets, the protocol charset could be simply
     * used again. Because it's not yet implemented that the insertion of BIDI
     * control characters at different points during composition is extracted.
     * <p>
     *
     * Always all the time, the setter method is always succeeded and throws
     * <code>DefaultCharsetChanged</code> exception.
     *
     * So API programmer must follow the following way:
     * <code><pre>
     *  import org.apache.util.URI$DefaultCharsetChanged;
     *      .
     *      .
     *      .
     *  try {
     *      URI.setDefaultDocumentCharset("EUC-KR");
     *  } catch (DefaultCharsetChanged cc) {
     *      // CASE 1: the exception could be ignored, when it is set by user
     *      if (cc.getReasonCode() == DefaultCharsetChanged.DOCUMENT_CHARSET) {
     *      // CASE 2: let user know the default document charset changed
     *      } else {
     *      // CASE 2: let user know the default protocol charset changed
     *      }
     *  }
     *  </pre></code>
     *
     * The API programmer is responsible to set the correct charset.
     * And each application should remember its own charset to support.
     *
     * @param charset the default charset for the document
     * @throws DefaultCharsetChanged default charset changed
     */
    public static void setDefaultDocumentCharset(String charset) 
        throws DefaultCharsetChanged {
            
        defaultDocumentCharset = charset;
        throw new DefaultCharsetChanged(DefaultCharsetChanged.DOCUMENT_CHARSET,
                "the default document charset changed");
    }


    /**
     * Get the recommended default charset of the document.
     *
     * @return the default charset string
     */
    public static String getDefaultDocumentCharset() {
        return defaultDocumentCharset;
    }


    /**
     * Get the default charset of the document by locale.
     *
     * @return the default charset string by locale
     */
    public static String getDefaultDocumentCharsetByLocale() {
        return defaultDocumentCharsetByLocale;
    }


    /**
     * Get the default charset of the document by platform.
     *
     * @return the default charset string by platform
     */
    public static String getDefaultDocumentCharsetByPlatform() {
        return defaultDocumentCharsetByPlatform;
    }

    // ------------------------------------------------------------- The scheme

    /**
     * Get the scheme.
     *
     * @return the scheme
     */
    public char[] getRawScheme() {
        return _scheme;
    }


    /**
     * Get the scheme.
     *
     * @return the scheme
     * null if undefined scheme
     */
    public String getScheme() {
        return (_scheme == null) ? null : new String(_scheme);
    }

    // ---------------------------------------------------------- The authority

    /**
     * Set the authority.  It can be one type of server, hostport, hostname,
     * IPv4address, IPv6reference and reg_name.
     * <p><blockquote><pre>
     *   authority     = server | reg_name
     * </pre></blockquote><p>
     *
     * @param escapedAuthority the raw escaped authority
     * @throws URIException If {@link 
     * #parseAuthority(java.lang.String,boolean)} fails
     * @throws NullPointerException null authority
     */
    public void setRawAuthority(char[] escapedAuthority) 
        throws URIException, NullPointerException {
            
        parseAuthority(new String(escapedAuthority), true);
        setURI();
    }


    /**
     * Set the authority.  It can be one type of server, hostport, hostname,
     * IPv4address, IPv6reference and reg_name.
     * Note that there is no setAuthority method by the escape encoding reason.
     *
     * @param escapedAuthority the escaped authority string
     * @throws URIException If {@link 
     * #parseAuthority(java.lang.String,boolean)} fails
     */
    public void setEscapedAuthority(String escapedAuthority)
        throws URIException {

        parseAuthority(escapedAuthority, true);
        setURI();
    }


    /**
     * Get the raw-escaped authority.
     *
     * @return the raw-escaped authority
     */
    public char[] getRawAuthority() {
        return _authority;
    }


    /**
     * Get the escaped authority.
     *
     * @return the escaped authority
     */
    public String getEscapedAuthority() {
        return (_authority == null) ? null : new String(_authority);
    }


    /**
     * Get the authority.
     *
     * @return the authority
     * @throws URIException If {@link #decode} fails
     */
    public String getAuthority() throws URIException {
        return (_authority == null) ? null : decode(_authority,
                getProtocolCharset());
    }

    // ----------------------------------------------------------- The userinfo

    /**
     * Get the raw-escaped userinfo.
     *
     * @return the raw-escaped userinfo
     * @see #getAuthority
     */
    public char[] getRawUserinfo() {
        return _userinfo;
    }


    /**
     * Get the escaped userinfo.
     *
     * @return the escaped userinfo
     * @see #getAuthority
     */
    public String getEscapedUserinfo() {
        return (_userinfo == null) ? null : new String(_userinfo);
    }


    /**
     * Get the userinfo.
     *
     * @return the userinfo
     * @throws URIException If {@link #decode} fails
     * @see #getAuthority
     */
    public String getUserinfo() throws URIException {
        return (_userinfo == null) ? null : decode(_userinfo,
                getProtocolCharset());
    }

    // --------------------------------------------------------------- The host

    /**
     * Get the host.
     * <p><blockquote><pre>
     *   host          = hostname | IPv4address | IPv6reference
     * </pre></blockquote><p>
     *
     * @return the host
     * @see #getAuthority
     */
    public char[] getRawHost() {
        return _host;
    }


    /**
     * Get the host.
     * <p><blockquote><pre>
     *   host          = hostname | IPv4address | IPv6reference
     * </pre></blockquote><p>
     *
     * @return the host
     * @throws URIException If {@link #decode} fails
     * @see #getAuthority
     */
    public String getHost() throws URIException {
        if (_host != null) {
            return decode(_host, getProtocolCharset());
        } else {
            return null;
        }
    }

    // --------------------------------------------------------------- The port

    /**
     * Get the port.  In order to get the specfic default port, the specific
     * protocol-supported class extended from the URI class should be used.
     * It has the server-based naming authority.
     *
     * @return the port
     * if -1, it has the default port for the scheme or the server-based
     * naming authority is not supported in the specific URI.
     */
    public int getPort() {
        return _port;
    }

    // --------------------------------------------------------------- The path

    /**
     * Set the raw-escaped path.
     *
     * @param escapedPath the path character sequence
     * @throws URIException encoding error or not proper for initial instance
     * @see #encode
     */
    public void setRawPath(char[] escapedPath) throws URIException {
        if (escapedPath == null || escapedPath.length == 0) {
            _path = _opaque = escapedPath;
            setURI();
            return;
        }
        // remove the fragment identifier
        escapedPath = removeFragmentIdentifier(escapedPath);
        if (_is_net_path || _is_abs_path) {
            if (escapedPath[0] != '/') {
                throw new URIException(URIException.PARSING,
                        "not absolute path");
            }
            if (!validate(escapedPath, abs_path)) {
                throw new URIException(URIException.ESCAPING,
                        "escaped absolute path not valid");
            }
            _path = escapedPath;
        } else if (_is_rel_path) {
            int at = indexFirstOf(escapedPath, '/');
            if (at == 0) {
                throw new URIException(URIException.PARSING, "incorrect path");
            }
            if (at > 0 && !validate(escapedPath, 0, at - 1, rel_segment) 
                && !validate(escapedPath, at, -1, abs_path) 
                || at < 0 && !validate(escapedPath, 0, -1, rel_segment)) {
            
                throw new URIException(URIException.ESCAPING,
                        "escaped relative path not valid");
            }
            _path = escapedPath;
        } else if (_is_opaque_part) {
            if (!uric_no_slash.get(escapedPath[0]) 
                && !validate(escapedPath, 1, -1, uric)) {
                throw new URIException(URIException.ESCAPING,
                    "escaped opaque part not valid");
            }
            _opaque = escapedPath;
        } else {
            throw new URIException(URIException.PARSING, "incorrect path");
        }
        setURI();
    }


    /**
     * Set the escaped path.
     *
     * @param escapedPath the escaped path string
     * @throws URIException encoding error or not proper for initial instance
     * @see #encode
     */
    public void setEscapedPath(String escapedPath) throws URIException {
        if (escapedPath == null) {
            _path = _opaque = null;
            setURI();
            return;
        }
        setRawPath(escapedPath.toCharArray());
    }


    /**
     * Set the path.
     *
     * @param path the path string
     * @throws URIException set incorrectly or fragment only
     * @see #encode
     */
    public void setPath(String path) throws URIException {

        if (path == null || path.length() == 0) {
            _path = _opaque = (path == null) ? null : path.toCharArray();
            setURI();
            return;
        }
        // set the charset to do escape encoding
        String charset = getProtocolCharset();

        if (_is_net_path || _is_abs_path) {
            _path = encode(path, allowed_abs_path, charset);
        } else if (_is_rel_path) {
            StringBuilder buff = new StringBuilder(path.length());
            int at = path.indexOf('/');
            if (at == 0) { // never 0
                throw new URIException(URIException.PARSING,
                        "incorrect relative path");
            }
            if (at > 0) {
                buff.append(encode(path.substring(0, at), allowed_rel_path,
                            charset));
                buff.append(encode(path.substring(at), allowed_abs_path,
                            charset));
            } else {
                buff.append(encode(path, allowed_rel_path, charset));
            }
            _path = buff.toString().toCharArray();
        } else if (_is_opaque_part) {
            StringBuilder buf = new StringBuilder();
            buf.insert(0, encode(path.substring(0, 1), uric_no_slash, charset));
            buf.insert(1, encode(path.substring(1), uric, charset));
            _opaque = buf.toString().toCharArray();
        } else {
            throw new URIException(URIException.PARSING, "incorrect path");
        }
        setURI();
    }


    /**
     * Resolve the base and relative path.
     *
     * @param basePath a character array of the basePath
     * @param relPath a character array of the relPath
     * @return the resolved path
     * @throws URIException no more higher path level to be resolved
     */
    protected char[] resolvePath(char[] basePath, char[] relPath)
        throws URIException {

        // REMINDME: paths are never null
        String base = (basePath == null) ? "" : new String(basePath);

        // _path could be empty
        if (relPath == null || relPath.length == 0) {
            return normalize(basePath);
        } else if (relPath[0] == '/') {
            return normalize(relPath);
        } else {
            int at = base.lastIndexOf('/');
            if (at != -1) {
                basePath = base.substring(0, at + 1).toCharArray();
            }
            StringBuilder buff = new StringBuilder(base.length() 
                + relPath.length);
            buff.append((at != -1) ? base.substring(0, at + 1) : "/");
            buff.append(relPath);
            return normalize(buff.toString().toCharArray());
        }
    }


    /**
     * Get the raw-escaped current hierarchy level in the given path.
     * If the last namespace is a collection, the slash mark ('/') should be
     * ended with at the last character of the path string.
     *
     * @param path the path
     * @return the current hierarchy level
     * @throws URIException no hierarchy level
     */
    protected char[] getRawCurrentHierPath(char[] path) throws URIException {

        if (_is_opaque_part) {
            throw new URIException(URIException.PARSING, "no hierarchy level");
        }
        if (path == null) {
            throw new URIException(URIException.PARSING, "empty path");
        }
        String buff = new String(path);
        int first = buff.indexOf('/');
        int last = buff.lastIndexOf('/');
        if (last == 0) {
            return rootPath;
        } else if (first != last && last != -1) {
            return buff.substring(0, last).toCharArray();
        }
        // FIXME: it could be a document on the server side
        return path;
    }


    /**
     * Get the raw-escaped current hierarchy level.
     *
     * @return the raw-escaped current hierarchy level
     * @throws URIException If {@link #getRawCurrentHierPath(char[])} fails.
     */
    public char[] getRawCurrentHierPath() throws URIException {
        return (_path == null) ? null : getRawCurrentHierPath(_path);
    }
 

    /**
     * Get the escaped current hierarchy level.
     *
     * @return the escaped current hierarchy level
     * @throws URIException If {@link #getRawCurrentHierPath(char[])} fails.
     */
    public String getEscapedCurrentHierPath() throws URIException {
        char[] path = getRawCurrentHierPath();
        return (path == null) ? null : new String(path);
    }
 

    /**
     * Get the current hierarchy level.
     *
     * @return the current hierarchy level
     * @throws URIException If {@link #getRawCurrentHierPath(char[])} fails.
     * @see #decode
     */
    public String getCurrentHierPath() throws URIException {
        char[] path = getRawCurrentHierPath();
        return (path == null) ? null : decode(path, getProtocolCharset());
    }


    /**
     * Get the level above the this hierarchy level.
     *
     * @return the raw above hierarchy level
     * @throws URIException If {@link #getRawCurrentHierPath(char[])} fails.
     */
    public char[] getRawAboveHierPath() throws URIException {
        char[] path = getRawCurrentHierPath();
        return (path == null) ? null : getRawCurrentHierPath(path);
    }


    /**
     * Get the level above the this hierarchy level.
     *
     * @return the raw above hierarchy level
     * @throws URIException If {@link #getRawCurrentHierPath(char[])} fails.
     */
    public String getEscapedAboveHierPath() throws URIException {
        char[] path = getRawAboveHierPath();
        return (path == null) ? null : new String(path);
    }


    /**
     * Get the level above the this hierarchy level.
     *
     * @return the above hierarchy level
     * @throws URIException If {@link #getRawCurrentHierPath(char[])} fails.
     * @see #decode
     */
    public String getAboveHierPath() throws URIException {
        char[] path = getRawAboveHierPath();
        return (path == null) ? null : decode(path, getProtocolCharset());
    }


    /**
     * Get the raw-escaped path.
     * <p><blockquote><pre>
     *   path          = [ abs_path | opaque_part ]
     * </pre></blockquote><p>
     *
     * @return the raw-escaped path
     */
    public char[] getRawPath() {
        return _is_opaque_part ? _opaque : _path;
    }


    /**
     * Get the escaped path.
     * <p><blockquote><pre>
     *   path          = [ abs_path | opaque_part ]
     *   abs_path      = "/"  path_segments 
     *   opaque_part   = uric_no_slash *uric
     * </pre></blockquote><p>
     *
     * @return the escaped path string
     */
    public String getEscapedPath() {
        char[] path = getRawPath();
        return (path == null) ? null : new String(path);
    }


    /**
     * Get the path.
     * <p><blockquote><pre>
     *   path          = [ abs_path | opaque_part ]
     * </pre></blockquote><p>
     * @return the path string
     * @throws URIException If {@link #decode} fails.
     * @see #decode
     */
    public String getPath() throws URIException { 
        char[] path =  getRawPath();
        return (path == null) ? null : decode(path, getProtocolCharset());
    }


    /**
     * Get the raw-escaped basename of the path.
     *
     * @return the raw-escaped basename
     */
    public char[] getRawName() {
        if (_path == null) { 
            return null;
        }

        int at = 0;
        for (int i = _path.length - 1; i >= 0; i--) {
            if (_path[i] == '/') {
                at = i + 1;
                break;
            }
        }
        int len = _path.length - at;
        char[] basename =  new char[len];
        System.arraycopy(_path, at, basename, 0, len);
        return basename;
    }


    /**
     * Get the escaped basename of the path.
     *
     * @return the escaped basename string
     */
    public String getEscapedName() {
        char[] basename = getRawName();
        return (basename == null) ? null : new String(basename);
    }


    /**
     * Get the basename of the path.
     *
     * @return the basename string
     * @throws URIException incomplete trailing escape pattern or unsupported
     * character encoding
     * @see #decode
     */
    public String getName() throws URIException {
        char[] basename = getRawName();
        return (basename == null) ? null : decode(getRawName(),
                getProtocolCharset());
    }

    // ----------------------------------------------------- The path and query 

    /**
     * Get the raw-escaped path and query.
     *
     * @return the raw-escaped path and query
     */
    public char[] getRawPathQuery() {

        if (_path == null && _query == null) {
            return null;
        }
        StringBuilder buff = new StringBuilder();
        if (_path != null) {
            buff.append(_path);
        }
        if (_query != null) {
            buff.append('?');
            buff.append(_query);
        }
        return buff.toString().toCharArray();
    }


    /**
     * Get the escaped query.
     *
     * @return the escaped path and query string
     */
    public String getEscapedPathQuery() {
        char[] rawPathQuery = getRawPathQuery();
        return (rawPathQuery == null) ? null : new String(rawPathQuery);
    }


    /**
     * Get the path and query.
     *
     * @return the path and query string.
     * @throws URIException incomplete trailing escape pattern or unsupported
     * character encoding
     * @see #decode
     */
    public String getPathQuery() throws URIException {
        char[] rawPathQuery = getRawPathQuery();
        return (rawPathQuery == null) ? null : decode(rawPathQuery,
                getProtocolCharset());
    }

    // -------------------------------------------------------------- The query 

    /**
     * Set the raw-escaped query.
     *
     * @param escapedQuery the raw-escaped query
     * @throws URIException escaped query not valid
     */
    public void setRawQuery(char[] escapedQuery) throws URIException {
        if (escapedQuery == null || escapedQuery.length == 0) {
            _query = escapedQuery;
            setURI();
            return;
        }
        // remove the fragment identifier
        escapedQuery = removeFragmentIdentifier(escapedQuery);
        if (!validate(escapedQuery, query)) {
            throw new URIException(URIException.ESCAPING,
                    "escaped query not valid");
        }
        _query = escapedQuery;
        setURI();
    }


    /**
     * Set the escaped query string.
     *
     * @param escapedQuery the escaped query string
     * @throws URIException escaped query not valid
     */
    public void setEscapedQuery(String escapedQuery) throws URIException {
        if (escapedQuery == null) {
            _query = null;
            setURI();
            return;
        }
        setRawQuery(escapedQuery.toCharArray());
    }


    /**
     * Set the query.
     * <p>
     * When a query string is not misunderstood the reserved special characters
     * ("&amp;", "=", "+", ",", and "$") within a query component, it is
     * recommended to use in encoding the whole query with this method.
     * <p>
     * The additional APIs for the special purpose using by the reserved
     * special characters used in each protocol are implemented in each protocol
     * classes inherited from <code>URI</code>.  So refer to the same-named APIs
     * implemented in each specific protocol instance.
     *
     * @param query the query string.
     * @throws URIException incomplete trailing escape pattern or unsupported
     * character encoding
     * @see #encode
     */
    public void setQuery(String query) throws URIException {
        if (query == null || query.length() == 0) {
            _query = (query == null) ? null : query.toCharArray();
            setURI();
            return;
        }
        setRawQuery(encode(query, allowed_query, getProtocolCharset()));
    }


    /**
     * Get the raw-escaped query.
     *
     * @return the raw-escaped query
     */
    public char[] getRawQuery() {
        return _query;
    }


    /**
     * Get the escaped query.
     *
     * @return the escaped query string
     */
    public String getEscapedQuery() {
        return (_query == null) ? null : new String(_query);
    }


    /**
     * Get the query.
     *
     * @return the query string.
     * @throws URIException incomplete trailing escape pattern or unsupported
     * character encoding
     * @see #decode
     */
    public String getQuery() throws URIException {
        return (_query == null) ? null : decode(_query, getProtocolCharset());
    }

    // ----------------------------------------------------------- The fragment 

    /**
     * Set the raw-escaped fragment.
     *
     * @param escapedFragment the raw-escaped fragment
     * @throws URIException escaped fragment not valid
     */
    public void setRawFragment(char[] escapedFragment) throws URIException {
        if (escapedFragment == null || escapedFragment.length == 0) {
            _fragment = escapedFragment;
            hash = 0;
            return;
        }
        if (!validate(escapedFragment, fragment)) {
            throw new URIException(URIException.ESCAPING,
                    "escaped fragment not valid");
        }
        _fragment = escapedFragment;
        hash = 0;
    }


    /**
     * Set the escaped fragment string.
     *
     * @param escapedFragment the escaped fragment string
     * @throws URIException escaped fragment not valid
     */
    public void setEscapedFragment(String escapedFragment) throws URIException {
        if (escapedFragment == null) {
            _fragment = null;
            hash = 0;
            return;
        }
        setRawFragment(escapedFragment.toCharArray());
    }


    /**
     * Set the fragment.
     *
     * @param fragment the fragment string.
     * @throws URIException If an error occurs.
     */
    public void setFragment(String fragment) throws URIException {
        if (fragment == null || fragment.length() == 0) {
            _fragment = (fragment == null) ? null : fragment.toCharArray();
            hash = 0;
            return;
        }
        _fragment = encode(fragment, allowed_fragment, getProtocolCharset());
        hash = 0;
    }


    /**
     * Get the raw-escaped fragment.
     * <p>
     * The optional fragment identifier is not part of a URI, but is often used
     * in conjunction with a URI.
     * <p>
     * The format and interpretation of fragment identifiers is dependent on
     * the media type [RFC2046] of the retrieval result.
     * <p>
     * A fragment identifier is only meaningful when a URI reference is
     * intended for retrieval and the result of that retrieval is a document
     * for which the identified fragment is consistently defined.
     *
     * @return the raw-escaped fragment
     */
    public char[] getRawFragment() {
        return _fragment;
    }


    /**
     * Get the escaped fragment.
     *
     * @return the escaped fragment string
     */
    public String getEscapedFragment() {
        return (_fragment == null) ? null : new String(_fragment);
    }


    /**
     * Get the fragment.
     *
     * @return the fragment string
     * @throws URIException incomplete trailing escape pattern or unsupported
     * character encoding
     * @see #decode
     */
    public String getFragment() throws URIException {
        return (_fragment == null) ? null : decode(_fragment,
                getProtocolCharset());
    }

    // ------------------------------------------------------------- Utilities 

    /**
     * Remove the fragment identifier of the given component.
     *
     * @param component the component that a fragment may be included
     * @return the component that the fragment identifier is removed
     */
    protected char[] removeFragmentIdentifier(char[] component) {
        if (component == null) { 
            return null;
        }
        int lastIndex = new String(component).indexOf('#');
        if (lastIndex != -1) {
            component = new String(component).substring(0,
                    lastIndex).toCharArray();
        }
        return component;
    }


    /**
     * Normalize the given hier path part.
     * 
     * <p>Algorithm taken from URI reference parser at 
     * http://www.apache.org/~fielding/uri/rev-2002/issues.html.
     *
     * @param path the path to normalize
     * @return the normalized path
     * @throws URIException no more higher path level to be normalized
     */
    protected char[] normalize(char[] path) throws URIException {

        if (path == null) { 
            return null;
        }

        String normalized = new String(path);

        // If the buffer begins with "./" or "../", the "." or ".." is removed.
        if (normalized.startsWith("./")) {
            normalized = normalized.substring(1);
        } else if (normalized.startsWith("../")) {
            normalized = normalized.substring(2);
        } else if (normalized.startsWith("..")) {
            normalized = normalized.substring(2);
        }

        // All occurrences of "/./" in the buffer are replaced with "/"
        int index = -1;
        while ((index = normalized.indexOf("/./")) != -1) {
            normalized = normalized.substring(0, index) + normalized.substring(index + 2);
        }

        // If the buffer ends with "/.", the "." is removed.
        if (normalized.endsWith("/.")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        int startIndex = 0;

        // All occurrences of "/<segment>/../" in the buffer, where ".."
        // and <segment> are complete path segments, are iteratively replaced
        // with "/" in order from left to right until no matching pattern remains.
        // If the buffer ends with "/<segment>/..", that is also replaced
        // with "/".  Note that <segment> may be empty.
        while ((index = normalized.indexOf("/../", startIndex)) != -1) {
            int slashIndex = normalized.lastIndexOf('/', index - 1);
            if (slashIndex >= 0) {
                normalized = normalized.substring(0, slashIndex) + normalized.substring(index + 3);
            } else {
                startIndex = index + 3;   
            }
        }
        if (normalized.endsWith("/..")) {
            int slashIndex = normalized.lastIndexOf('/', normalized.length() - 4);
            if (slashIndex >= 0) {
                normalized = normalized.substring(0, slashIndex + 1);
            }
        }

        // All prefixes of "<segment>/../" in the buffer, where ".."
        // and <segment> are complete path segments, are iteratively replaced
        // with "/" in order from left to right until no matching pattern remains.
        // If the buffer ends with "<segment>/..", that is also replaced
        // with "/".  Note that <segment> may be empty.
        while ((index = normalized.indexOf("/../")) != -1) {
            int slashIndex = normalized.lastIndexOf('/', index - 1);
            if (slashIndex >= 0) {
                break;
            } else {
                normalized = normalized.substring(index + 3);
            }
        }
        if (normalized.endsWith("/..")) {
            int slashIndex = normalized.lastIndexOf('/', normalized.length() - 4);
            if (slashIndex < 0) {
                normalized = "/";
            }
        }

        return normalized.toCharArray();
    }


    /**
     * Normalizes the path part of this URI.  Normalization is only meant to be performed on 
     * URIs with an absolute path.  Calling this method on a relative path URI will have no
     * effect.
     *
     * @throws URIException no more higher path level to be normalized
     * 
     * @see #isAbsPath()
     */
    public void normalize() throws URIException {
        if (isAbsPath()) {
            _path = normalize(_path);
            setURI();
        }
    }


    /**
     * Test if the first array is equal to the second array.
     *
     * @param first the first character array
     * @param second the second character array
     * @return true if they're equal
     */
    protected boolean equals(char[] first, char[] second) {

        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        if (first.length != second.length) {
            return false;
        }
        for (int i = 0; i < first.length; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }
        return true;
    }


    /**
     * Test an object if this URI is equal to another.
     *
     * @param obj an object to compare
     * @return true if two URI objects are equal
     */
    @Override
    public boolean equals(Object obj) {

        // normalize and test each components
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof URI)) {
            return false;
        }
        URI another = (URI) obj;
        // scheme
        if (!equals(_scheme, another._scheme)) {
            return false;
        }
        // is_opaque_part or is_hier_part?  and opaque
        if (!equals(_opaque, another._opaque)) {
            return false;
        }
        // is_hier_part
        // has_authority
        if (!equals(_authority, another._authority)) {
            return false;
        }
        // path
        if (!equals(_path, another._path)) {
            return false;
        }
        // has_query
        if (!equals(_query, another._query)) {
            return false;
        }
        // has_fragment?  should be careful of the only fragment case.
        if (!equals(_fragment, another._fragment)) {
            return false;
        }
        return true;
    }

    // ---------------------------------------------------------- Serialization

    /**
     * Write the content of this URI.
     *
     * @param oos the object-output stream
     * @throws IOException If an IO problem occurs.
     */
    private void writeObject(ObjectOutputStream oos)
        throws IOException {

        oos.defaultWriteObject();
    }


    /**
     * Read a URI.
     *
     * @param ois the object-input stream
     * @throws ClassNotFoundException If one of the classes specified in the
     * input stream cannot be found.
     * @throws IOException If an IO problem occurs.
     */
    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, IOException {

        ois.defaultReadObject();
    }

    // -------------------------------------------------------------- Hash code

    /**
     * Return a hash code for this URI.
     *
     * @return a has code value for this URI
     */
    @Override
    public int hashCode() {
        if (hash == 0) {
            char[] c = _uri;
            if (c != null) {
                for (int i = 0, len = c.length; i < len; i++) {
                    hash = 31 * hash + c[i];
                }
            }
            c = _fragment;
            if (c != null) {
                for (int i = 0, len = c.length; i < len; i++) {
                    hash = 31 * hash + c[i];
                }
            }
        }
        return hash;
    }

    // ------------------------------------------------------------- Comparison 

    /**
     * Compare this URI to another object. 
     *
     * @param obj the object to be compared.
     * @return 0, if it's same,
     * -1, if failed, first being compared with in the authority component
     * @throws ClassCastException not URI argument
     */
    @Override
    public int compareTo(Object obj) throws ClassCastException {

        URI another = (URI) obj;
        if (!equals(_authority, another.getRawAuthority())) { 
            return -1;
        }
        return toString().compareTo(another.toString());
    }

    // ------------------------------------------------------------------ Clone

    /**
     * Create and return a copy of this object, the URI-reference containing
     * the userinfo component.  Notice that the whole URI-reference including
     * the userinfo component counld not be gotten as a <code>String</code>.
     * <p>
     * To copy the identical <code>URI</code> object including the userinfo
     * component, it should be used.
     *
     * @return a clone of this instance
     */
    @Override
    public synchronized Object clone() throws CloneNotSupportedException {

        URI instance = (URI) super.clone();

        instance._uri = _uri;
        instance._scheme = _scheme;
        instance._opaque = _opaque;
        instance._authority = _authority;
        instance._userinfo = _userinfo;
        instance._host = _host;
        instance._port = _port;
        instance._path = _path;
        instance._query = _query;
        instance._fragment = _fragment;
        // the charset to do escape encoding for this instance
        instance.protocolCharset = protocolCharset;
        // flags
        instance._is_hier_part = _is_hier_part;
        instance._is_opaque_part = _is_opaque_part;
        instance._is_net_path = _is_net_path;
        instance._is_abs_path = _is_abs_path;
        instance._is_rel_path = _is_rel_path;
        instance._is_reg_name = _is_reg_name;
        instance._is_server = _is_server;
        instance._is_hostname = _is_hostname;
        instance._is_IPv4address = _is_IPv4address;
        instance._is_IPv6reference = _is_IPv6reference;

        return instance;
    }

    // ------------------------------------------------------------ Get the URI

    /**
     * It can be gotten the URI character sequence. It's raw-escaped.
     * For the purpose of the protocol to be transported, it will be useful.
     * <p>
     * It is clearly unwise to use a URL that contains a password which is
     * intended to be secret. In particular, the use of a password within
     * the 'userinfo' component of a URL is strongly disrecommended except
     * in those rare cases where the 'password' parameter is intended to be
     * public.
     * <p>
     * When you want to get each part of the userinfo, you need to use the
     * specific methods in the specific URL. It depends on the specific URL.
     *
     * @return the URI character sequence
     */
    public char[] getRawURI() {
        return _uri;
    }


    /**
     * It can be gotten the URI character sequence. It's escaped.
     * For the purpose of the protocol to be transported, it will be useful.
     *
     * @return the escaped URI string
     */
    public String getEscapedURI() {
        return (_uri == null) ? null : new String(_uri);
    }
    

    /**
     * It can be gotten the URI character sequence.
     *
     * @return the original URI string
     * @throws URIException incomplete trailing escape pattern or unsupported
     * character encoding
     * @see #decode
     */
    public String getURI() throws URIException {
        return (_uri == null) ? null : decode(_uri, getProtocolCharset());
    }


    /**
     * Get the URI reference character sequence.
     *
     * @return the URI reference character sequence
     */
    public char[] getRawURIReference() {
        if (_fragment == null) { 
            return _uri;
        }
        if (_uri == null) { 
            return _fragment;
        }
        // if _uri != null &&  _fragment != null
        String uriReference = new String(_uri) + "#" + new String(_fragment);
        return uriReference.toCharArray();
    }


    /**
     * Get the escaped URI reference string.
     *
     * @return the escaped URI reference string
     */
    public String getEscapedURIReference() {
        char[] uriReference = getRawURIReference();
        return (uriReference == null) ? null : new String(uriReference);
    }


    /**
     * Get the original URI reference string.
     *
     * @return the original URI reference string
     * @throws URIException If {@link #decode} fails.
     */
    public String getURIReference() throws URIException {
        char[] uriReference = getRawURIReference();
        return (uriReference == null) ? null : decode(uriReference,
                getProtocolCharset());
    }


    /**
     * Get the escaped URI string.
     * <p>
     * On the document, the URI-reference form is only used without the userinfo
     * component like http://jakarta.apache.org/ by the security reason.
     * But the URI-reference form with the userinfo component could be parsed.
     * <p>
     * In other words, this URI and any its subclasses must not expose the
     * URI-reference expression with the userinfo component like
     * http://user:password@hostport/restricted_zone.<br>
     * It means that the API client programmer should extract each user and
     * password to access manually.  Probably it will be supported in the each
     * subclass, however, not a whole URI-reference expression.
     *
     * @return the escaped URI string
     * @see #clone()
     */
    @Override
    public String toString() {
        return getEscapedURI();
    }


    // ------------------------------------------------------------ Inner class

    /** 
     * The charset-changed normal operation to represent to be required to
     * alert to user the fact the default charset is changed.
     */
    public static class DefaultCharsetChanged extends RuntimeException {

        // ------------------------------------------------------- constructors

        /**
         * The constructor with a reason string and its code arguments.
         *
         * @param reasonCode the reason code
         * @param reason the reason
         */
        public DefaultCharsetChanged(int reasonCode, String reason) {
            super(reason);
            this.reason = reason;
            this.reasonCode = reasonCode;
        }

        // ---------------------------------------------------------- constants

        private static final long serialVersionUID = 1L;

        /** No specified reason code. */
        public static final int UNKNOWN = 0;

        /** Protocol charset changed. */
        public static final int PROTOCOL_CHARSET = 1;

        /** Document charset changed. */
        public static final int DOCUMENT_CHARSET = 2;

        // ------------------------------------------------- instance variables

        /** The reason code. */
        private int reasonCode;

        /** The reason message. */
        private String reason;

        // ------------------------------------------------------------ methods

        /**
         * Get the reason code.
         *
         * @return the reason code
         */
        public int getReasonCode() {
            return reasonCode;
        }

        /**
         * Get the reason message.
         *
         * @return the reason message
         */
        public String getReason() {
            return reason;
        }

    }


    /** 
     * A mapping to determine the (somewhat arbitrarily) preferred charset for a
     * given locale.  Supports all locales recognized in JDK 1.1.
     * <p>
     * The distribution of this class is Servlets.com.    It was originally
     * written by Jason Hunter [jhunter at acm.org] and used by with permission.
     */
    public static class LocaleToCharsetMap {

        /** A mapping of language code to charset */
        private static final Hashtable<String, String> LOCALE_TO_CHARSET_MAP;
        static {
            LOCALE_TO_CHARSET_MAP = new Hashtable<>();
            LOCALE_TO_CHARSET_MAP.put("ar", "ISO-8859-6");
            LOCALE_TO_CHARSET_MAP.put("be", "ISO-8859-5");
            LOCALE_TO_CHARSET_MAP.put("bg", "ISO-8859-5");
            LOCALE_TO_CHARSET_MAP.put("ca", "ISO-8859-1");
            LOCALE_TO_CHARSET_MAP.put("cs", "ISO-8859-2");
            LOCALE_TO_CHARSET_MAP.put("da", "ISO-8859-1");
            LOCALE_TO_CHARSET_MAP.put("de", "ISO-8859-1");
            LOCALE_TO_CHARSET_MAP.put("el", "ISO-8859-7");
            LOCALE_TO_CHARSET_MAP.put("en", "ISO-8859-1");
            LOCALE_TO_CHARSET_MAP.put("es", "ISO-8859-1");
            LOCALE_TO_CHARSET_MAP.put("et", "ISO-8859-1");
            LOCALE_TO_CHARSET_MAP.put("fi", "ISO-8859-1");
            LOCALE_TO_CHARSET_MAP.put("fr", "ISO-8859-1");
            LOCALE_TO_CHARSET_MAP.put("hr", "ISO-8859-2");
            LOCALE_TO_CHARSET_MAP.put("hu", "ISO-8859-2");
            LOCALE_TO_CHARSET_MAP.put("is", "ISO-8859-1");
            LOCALE_TO_CHARSET_MAP.put("it", "ISO-8859-1");
            LOCALE_TO_CHARSET_MAP.put("iw", "ISO-8859-8");
            LOCALE_TO_CHARSET_MAP.put("ja", "Shift_JIS");
            LOCALE_TO_CHARSET_MAP.put("ko", "EUC-KR");
            LOCALE_TO_CHARSET_MAP.put("lt", "ISO-8859-2");
            LOCALE_TO_CHARSET_MAP.put("lv", "ISO-8859-2");
            LOCALE_TO_CHARSET_MAP.put("mk", "ISO-8859-5");
            LOCALE_TO_CHARSET_MAP.put("nl", "ISO-8859-1");
            LOCALE_TO_CHARSET_MAP.put("no", "ISO-8859-1");
            LOCALE_TO_CHARSET_MAP.put("pl", "ISO-8859-2");
            LOCALE_TO_CHARSET_MAP.put("pt", "ISO-8859-1");
            LOCALE_TO_CHARSET_MAP.put("ro", "ISO-8859-2");
            LOCALE_TO_CHARSET_MAP.put("ru", "ISO-8859-5");
            LOCALE_TO_CHARSET_MAP.put("sh", "ISO-8859-5");
            LOCALE_TO_CHARSET_MAP.put("sk", "ISO-8859-2");
            LOCALE_TO_CHARSET_MAP.put("sl", "ISO-8859-2");
            LOCALE_TO_CHARSET_MAP.put("sq", "ISO-8859-2");
            LOCALE_TO_CHARSET_MAP.put("sr", "ISO-8859-5");
            LOCALE_TO_CHARSET_MAP.put("sv", "ISO-8859-1");
            LOCALE_TO_CHARSET_MAP.put("tr", "ISO-8859-9");
            LOCALE_TO_CHARSET_MAP.put("uk", "ISO-8859-5");
            LOCALE_TO_CHARSET_MAP.put("zh", "GB2312");
            LOCALE_TO_CHARSET_MAP.put("zh_TW", "Big5");
        }
       
        /**
         * Get the preferred charset for the given locale.
         *
         * @param locale the locale
         * @return the preferred charset or null if the locale is not
         * recognized.
         */
        public static String getCharset(Locale locale) {
            // try for an full name match (may include country)
            String charset =  LOCALE_TO_CHARSET_MAP.get(locale.toString());
            if (charset != null) { 
                return charset;
            }
           
            // if a full name didn't match, try just the language
            charset = LOCALE_TO_CHARSET_MAP.get(locale.getLanguage());
            return charset;  // may be null
        }

    }

}

