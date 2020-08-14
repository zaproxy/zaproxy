/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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

import java.util.List;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMessage;

public interface SiteMapModifier {

    /**
     * Gets the parameters of the given {@code type} from the given {@code message}. Returning null
     * is taken to mean use the default methods for obtaining parameters. The parameter names are
     * shown in brackets after the site tree node names.
     *
     * <p>The parameters are split using the key value pair separator(s) and each resulting
     * parameter is split into name/value pairs using key value separator(s).
     *
     * <p>Parameters' names and values are in decoded form.
     *
     * @param msg the message whose parameters will be extracted from
     * @param type the type of parameters to extract
     * @param helper a helper class, currently unused
     * @return a {@code List} containing the parameters
     * @throws IllegalArgumentException if the {@code msg} or {@code type} is {@code null}.
     * @see #getDefaultKeyValuePairSeparator()
     * @see #getDefaultKeyValueSeparator()
     */
    List<NameValuePair> getParameters(
            HttpMessage msg, HtmlParameter.Type type, SiteMapModifierHelper helper);

    /**
     * Returns the tree path elements for the given {@code message}. Returning null is taken to mean
     * use the default methods for obtaining tree path elements. This will determine the position of
     * this message in the sites tree.
     *
     * <p>By default the elements are returned for the following URL are:
     *
     * <ul>
     *   <li><i>http://example.org/path/to/element?aa=bb&cc==dd</i> : ["path", "to", "element"]
     *   <li><i>http://example.org/path/to/element</i> : ["path", "to", "element"]
     *   <li><i>http://example.org/path/to/</i> : ["path", "to"]
     *   <li><i>http://example.org/path/to</i> : ["path", "to"]
     * </ul>
     *
     * @param msg
     * @param helper a helper class, currently unused
     * @return a {@code List} containing the tree path elements
     * @throws URIException
     */
    List<String> getTreePath(HttpMessage msg, SiteMapModifierHelper helper) throws URIException;
}
