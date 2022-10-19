/*
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
// ZAP: 2013/07/02 Changed Vector to generic List
// ZAP: 2013/07/02 Changed API to public for future extensible Variant model
// ZAP: 2016/05/04 Add JavaDoc to getParamList()
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/08/27 Added default methods for modifying the Sites tree
// ZAP: 2021/05/06 Add method to get a short name of the variant
package org.parosproxy.paros.core.scanner;

import java.util.List;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.core.scanner.InputVector;

public interface Variant {

    public void setMessage(HttpMessage msg);

    /**
     * Gets the list of parameters handled by this variant.
     *
     * @return a {@code List} containing the parameters
     */
    public List<NameValuePair> getParamList();

    public String setParameter(
            HttpMessage msg, NameValuePair originalPair, String param, String value);

    public String setEscapedParameter(
            HttpMessage msg, NameValuePair originalPair, String param, String value);

    /**
     * Sets the parameters into the given {@code message}.
     *
     * @param message the message that will be changed
     * @param inputVectors list of name of the parameter
     * @since 2.11.0
     */
    default void setParameters(HttpMessage message, List<InputVector> inputVectors) {}

    /**
     * Gets a short name of the Variant
     *
     * @return a {@code String} the short name of the variant
     * @since 2.12.0
     */
    default String getShortName() {
        return "";
    }

    /**
     * Gets the name of the node to be used for the given {@code msg} in the Site Map. Returning
     * null is taken to mean use the default name. This is currently the last element of the path
     * (given in {@code nodeName}) followed by the url parameter names in brackets (if any) followed
     * by the form parameter names in brackets (if any).
     *
     * @param nodeName the last element of the path
     * @param msg the message
     */
    default String getLeafName(String nodeName, HttpMessage msg) {
        return null;
    }

    /**
     * Returns the tree path elements for the given {@code message}. Returning null is taken to mean
     * use the default methods for obtaining tree path elements. This will determine the position of
     * this message in the Site Map.
     *
     * <p>By default the elements are returned for the following URL are:
     *
     * <ul>
     *   <li><i>http://example.org/path/to/element?aa=bb&amp;cc==dd</i> : ["path", "to", "element"]
     *   <li><i>http://example.org/path/to/element</i> : ["path", "to", "element"]
     *   <li><i>http://example.org/path/to/</i> : ["path", "to"]
     *   <li><i>http://example.org/path/to</i> : ["path", "to"]
     * </ul>
     *
     * @param msg
     * @return a {@code List} containing the tree path elements
     * @throws URIException
     */
    default List<String> getTreePath(HttpMessage msg) throws URIException {
        return null;
    }
}
