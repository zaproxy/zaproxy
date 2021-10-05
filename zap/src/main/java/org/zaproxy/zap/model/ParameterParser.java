/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMessage;

public interface ParameterParser {

    void init(String config);

    /**
     * @deprecated 2.10.0 use #getParameters(String) This method will lose duplicated parameter
     *     names
     */
    @Deprecated
    Map<String, String> getParams(HttpMessage msg, HtmlParameter.Type type);

    /**
     * Gets the parameters of the given {@code type} from the given {@code message}.
     *
     * <p>The parameters are split using the key value pair separator(s) and each resulting
     * parameter is split into name/value pairs using key value separator(s).
     *
     * <p>Parameters' names and values are in decoded form, if not malformed, otherwise the original
     * name/value. Names and values are never {@code null}.
     *
     * @param msg the message whose parameters will be extracted from
     * @param type the type of parameters to extract
     * @return a {@code List} containing the parameters
     * @throws IllegalArgumentException if the {@code msg} or {@code type} is {@code null}.
     * @since 2.5.0
     * @see #getDefaultKeyValuePairSeparator()
     * @see #getDefaultKeyValueSeparator()
     */
    List<NameValuePair> getParameters(HttpMessage msg, HtmlParameter.Type type);

    /**
     * @deprecated 2.10.0 use #parseParameters(String) This method will lose duplicated parameter
     *     names
     */
    @Deprecated
    Map<String, String> parse(String paramStr);

    /**
     * Parses the given {@code parameters} into a list of {@link NameValuePair}.
     *
     * <p>The parameters are split using the key value pair separator(s) and each resulting
     * parameter is split into name/value pairs using key value separator(s).
     *
     * <p>Parameters' names and values are in decoded form, if not malformed, otherwise the original
     * name/value. Names and values are never {@code null}.
     *
     * @param parameters the String of parameters to parse, might be {@code null}
     * @return a {@code List} containing the parameters parsed
     * @since 2.5.0
     * @see #getDefaultKeyValuePairSeparator()
     * @see #getDefaultKeyValueSeparator()
     */
    List<NameValuePair> parseParameters(String parameters);

    /**
     * Parses the given {@code parameters} into a list of {@link NameValuePair}.
     *
     * <p>The parameters are split using the key value pair separator(s) and each resulting
     * parameter is split into name/value pairs using key value separator(s).
     *
     * <p>Unlike {@link #parseParameters(String)} the parameters' names and values are not decoded.
     * This allows to rebuild the original string without (re)encoding issues. Values might be
     * {@code null}, when not present.
     *
     * <p>By default returns an empty list.
     *
     * @param parameters the String of parameters to parse, might be {@code null}.
     * @return a {@code List} containing the parameters parsed, never {@code null}.
     * @since 2.10.0
     * @see #getDefaultKeyValuePairSeparator()
     * @see #getDefaultKeyValueSeparator()
     */
    default List<NameValuePair> parseRawParameters(String parameters) {
        return Collections.emptyList();
    }

    List<String> getTreePath(URI uri) throws URIException;

    List<String> getTreePath(HttpMessage msg) throws URIException;

    /**
     * Gets the path of the URI's ancestor found at the given depth, taking into account any context
     * specific configuration (e.g. structural parameters). The depth could also be seen as the
     * number of path elements returned.
     *
     * <p>A few examples (uri, depth):
     *
     * <ul>
     *   <li>(<i>http://example.org/path/to/element</i>, 0) -&gt; ""
     *   <li>(<i>http://example.org/path/to/element</i>, 1) -&gt; "/path"
     *   <li>(<i>http://example.org/path/to/element</i>, 3) -&gt; "/path/to/element"
     *   <li>(<i>http://example.org/path?page=12&amp;data=123</i>, 2) -&gt; "/path?page=12", if
     *       {@code page} is a structural parameter
     *   <li>(<i>http://example.org/path?page=12&amp;data=123&amp;type=1</i>, 3) -&gt;
     *       "/path?page=12&amp;type=1", if {@code page} and {@code type} are both structural
     *       parameter
     * </ul>
     *
     * @param uri the URI
     * @param depth the depth
     * @return the path of the ancestor
     * @throws URIException if an error occurred while accessing the provided uri
     */
    String getAncestorPath(URI uri, int depth) throws URIException;

    String getDefaultKeyValuePairSeparator();

    String getDefaultKeyValueSeparator();

    String getConfig();

    ParameterParser clone();

    void setContext(Context context);

    Context getContext();
}
