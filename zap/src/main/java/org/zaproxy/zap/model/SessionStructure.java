/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Variant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordStructure;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HtmlParameter.Type;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

public class SessionStructure {

    public static final String ROOT = "Root";

    public static final String DATA_DRIVEN_NODE_PREFIX = "\u00AB";
    public static final String DATA_DRIVEN_NODE_POSTFIX = "\u00BB";
    public static final String DATA_DRIVEN_NODE_REGEX = "(.+?)";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String MULTIPART_FORM_DATA_DISPLAY = "(" + MULTIPART_FORM_DATA + ")";

    private static final Logger log = LogManager.getLogger(SessionStructure.class);

    /**
     * Adds the message to the Sites tree
     *
     * @param session the session
     * @param ref the history reference
     * @param msg the message
     * @return the node added to the Sites Tree
     * @deprecated Use {@link #addPath(Model, HistoryReference, HttpMessage)}
     */
    @Deprecated
    public static StructuralNode addPath(Session session, HistoryReference ref, HttpMessage msg) {
        return addPath(session, ref, msg, false);
    }

    /**
     * Adds the message to the Sites tree
     *
     * @param model the model
     * @param ref the history reference
     * @param msg the message
     * @return the node added to the Sites Tree
     * @since 2.10.0
     */
    public static StructuralNode addPath(Model model, HistoryReference ref, HttpMessage msg) {
        return addPath(model, ref, msg, false);
    }

    /**
     * Adds the message to the Sites tree
     *
     * @param session the session
     * @param ref the history reference
     * @param msg the message
     * @param newOnly Only return a SiteNode if one was newly created
     * @return the SiteNode that corresponds to the HttpMessage, or null if newOnly and the node
     *     already exists
     * @deprecated Use {@link #addPath(Model, HistoryReference, HttpMessage, boolean)}
     */
    @Deprecated
    public static StructuralNode addPath(
            Session session, HistoryReference ref, HttpMessage msg, boolean newOnly) {
        return addPath(Model.getSingleton(), ref, msg, newOnly);
    }

    /**
     * Adds the message to the Sites tree
     *
     * @param model the model
     * @param ref the history reference
     * @param msg the message
     * @param newOnly Only return a SiteNode if one was newly created
     * @return the SiteNode that corresponds to the HttpMessage, or null if newOnly and the node
     *     already exists
     * @since 2.10.0
     */
    public static StructuralNode addPath(
            Model model, HistoryReference ref, HttpMessage msg, boolean newOnly) {
        Session session = model.getSession();
        if (!Constant.isLowMemoryOptionSet()) {
            SiteNode node = session.getSiteTree().addPath(ref, msg, newOnly);
            if (node != null) {
                return new StructuralSiteNode(node);
            }
            return null;
        } else {
            try {
                List<String> paths = getTreePath(model, msg);
                String host = getHostName(msg.getRequestHeader().getURI());

                RecordStructure rs =
                        addStructure(
                                model, host, msg, paths, paths.size(), ref.getHistoryId(), newOnly);
                if (rs != null) {
                    return new StructuralTableNode(rs);
                } else {
                    return null;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }
    }

    public static List<String> getTreePath(Model model, URI uri) throws URIException {
        return model.getSession().getUrlParamParser(uri.toString()).getTreePath(uri);
    }

    public static List<String> getTreePath(Model model, HttpMessage msg) throws URIException {
        for (Variant variant : model.getVariantFactory().createSiteModifyingVariants()) {
            List<String> path;
            try {
                path = variant.getTreePath(msg);
                if (path != null) {
                    return path;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        URI uri = msg.getRequestHeader().getURI();
        return model.getSession().getUrlParamParser(uri.toString()).getTreePath(msg);
    }

    /**
     * @param model
     * @param msg
     * @return The structural node for the given message
     * @throws DatabaseException
     * @throws URIException
     * @since 2.10.0
     */
    public static StructuralNode find(Model model, HttpMessage msg)
            throws DatabaseException, URIException {
        if (!Constant.isLowMemoryOptionSet()) {
            SiteNode node = model.getSession().getSiteTree().findNode(msg);
            if (node == null) {
                return null;
            }
            return new StructuralSiteNode(node);
        }

        String nodeName = getNodeName(model, msg);
        RecordStructure rs =
                model.getDb()
                        .getTableStructure()
                        .find(
                                model.getSession().getSessionId(),
                                nodeName,
                                msg.getRequestHeader().getMethod());
        if (rs == null) {
            return null;
        }
        return new StructuralTableNode(rs);
    }

    /**
     * Finds the node in the Site tree for the given request data
     *
     * @param sessionId the session id
     * @param uri the URI
     * @param method the method
     * @param postData the POST data
     * @return the site node or null if not found
     * @throws DatabaseException
     * @throws URIException
     * @deprecated Use {@link #find(Model, URI, String, String)}
     */
    @Deprecated
    public static StructuralNode find(long sessionId, URI uri, String method, String postData)
            throws DatabaseException, URIException {
        Model model = Model.getSingleton();
        if (!Constant.isLowMemoryOptionSet()) {
            SiteNode node = model.getSession().getSiteTree().findNode(uri, method, postData);
            if (node == null) {
                return null;
            }
            return new StructuralSiteNode(node);
        }

        String nodeName = getNodeName(model, uri, method, postData, null);
        RecordStructure rs = model.getDb().getTableStructure().find(sessionId, nodeName, method);
        if (rs == null) {
            return null;
        }
        return new StructuralTableNode(rs);
    }

    /**
     * Finds the node in the Site tree for the given request data
     *
     * @param model the model
     * @param uri the URI
     * @param method the method
     * @param postData the POST data
     * @return the site node or null if not found
     * @throws DatabaseException
     * @throws URIException
     * @since 2.10.0
     */
    public static StructuralNode find(Model model, URI uri, String method, String postData)
            throws DatabaseException, URIException {
        Session session = model.getSession();
        if (!Constant.isLowMemoryOptionSet()) {
            SiteNode node = session.getSiteTree().findNode(uri, method, postData);
            if (node == null) {
                return null;
            }
            return new StructuralSiteNode(node);
        }

        String nodeName = getNodeName(model, uri, method, postData, null);
        RecordStructure rs =
                model.getDb().getTableStructure().find(session.getSessionId(), nodeName, method);
        if (rs == null) {
            return null;
        }
        return new StructuralTableNode(rs);
    }

    private static String getNodeName(
            Model model, URI uri, String method, String postData, String contentType)
            throws URIException {

        Session session = model.getSession();
        List<String> paths = getTreePath(model, uri);

        String host = getHostName(uri);
        String nodeUrl = pathsToUrl(host, paths, paths.size());

        String params = getParams(session, uri, postData, contentType);
        if (params.length() > 0) {
            nodeUrl += " " + params;
        }
        return nodeUrl;
    }

    private static String getNodeName(
            Session session, String host, HttpMessage msg, List<String> paths, int size)
            throws URIException {
        String nodeUrl = pathsToUrl(host, paths, size);

        if (msg != null) {
            String params = getParams(session, msg);
            if (params.length() > 0) {
                nodeUrl = nodeUrl + " " + params;
            }
        }
        return nodeUrl;
    }

    /**
     * Returns the node name for the given message
     *
     * @param msg the message
     * @return the node name
     * @throws URIException
     * @deprecated Use {@link #getNodeName(Model, HttpMessage)}
     */
    @Deprecated
    public static String getNodeName(HttpMessage msg) throws URIException {
        return getNodeName(Model.getSingleton(), msg);
    }

    /**
     * Returns the node name for the given message
     *
     * @param model the model
     * @param msg the message
     * @return the node name
     * @throws URIException
     * @since 2.10.0
     */
    public static String getNodeName(Model model, HttpMessage msg) throws URIException {
        return getNodeName(
                model,
                msg.getRequestHeader().getURI(),
                msg.getRequestHeader().getMethod(),
                msg.getRequestBody().toString(),
                msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE));
    }

    public static String getLeafName(Model model, String nodeName, HttpMessage msg) {
        for (Variant variant : model.getVariantFactory().createSiteModifyingVariants()) {
            String name;
            try {
                name = variant.getLeafName(nodeName, msg);
                if (name != null) {
                    return name;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        List<org.parosproxy.paros.core.scanner.NameValuePair> params =
                convertNVP(
                        model.getSession().getParameters(msg, Type.url),
                        org.parosproxy.paros.core.scanner.NameValuePair.TYPE_QUERY_STRING);
        if (msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)) {
            params.addAll(
                    convertNVP(
                            model.getSession().getParameters(msg, Type.form),
                            org.parosproxy.paros.core.scanner.NameValuePair.TYPE_POST_DATA));
        }

        return getLeafName(nodeName, msg, params);
    }

    /**
     * Gets the name of the node to be used for the given parameters in the Site Map.
     *
     * @param model the model
     * @param nodeName the last element of the path
     * @param uri the full uri of the node, must not be {@code null}.
     * @param method the method for the node, must not be {@code null}.
     * @param postData the data of the request body.
     * @return the name of the node to be used in the Site Map
     * @throws HttpMalformedHeaderException if the uri is not correct.
     * @throws NullPointerException if the uri or the method are {@code null}.
     * @since 2.10.0
     */
    public static String getLeafName(
            Model model, String nodeName, URI uri, String method, String postData)
            throws HttpMalformedHeaderException {
        Objects.requireNonNull(uri);
        Objects.requireNonNull(method);
        HttpMessage msg = new HttpMessage(uri);
        msg.getRequestHeader().setMethod(method);
        if (method.equalsIgnoreCase(HttpRequestHeader.POST)) {
            msg.getRequestBody().setBody(postData);
            msg.getRequestHeader().setContentLength(msg.getRequestBody().length());
        }
        return getLeafName(model, nodeName, msg);
    }

    public static String getLeafName(
            String nodeName,
            HttpMessage message,
            List<org.parosproxy.paros.core.scanner.NameValuePair> params) {
        String method = message.getRequestHeader().getMethod();
        StringBuilder sb = new StringBuilder();
        sb.append(method);
        sb.append(":");
        sb.append(nodeName);

        if (method.equalsIgnoreCase(HttpRequestHeader.POST)) {
            sb.append(
                    getQueryParamString(
                            convertParosNVP(
                                    params,
                                    org.parosproxy.paros.core.scanner.NameValuePair
                                            .TYPE_QUERY_STRING),
                            true));

            String contentType = message.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);
            if (contentType != null && contentType.startsWith(MULTIPART_FORM_DATA)) {
                sb.append(MULTIPART_FORM_DATA_DISPLAY);
            } else {
                sb.append(
                        getQueryParamString(
                                convertParosNVP(
                                        params,
                                        org.parosproxy.paros.core.scanner.NameValuePair
                                                .TYPE_POST_DATA),
                                false));
            }
        } else {
            sb.append(
                    getQueryParamString(
                            convertParosNVP(
                                    params,
                                    org.parosproxy.paros.core.scanner.NameValuePair
                                            .TYPE_QUERY_STRING),
                            false));
            sb.append(
                    getQueryParamString(
                            convertParosNVP(
                                    params,
                                    org.parosproxy.paros.core.scanner.NameValuePair.TYPE_POST_DATA),
                            false));
        }

        return sb.toString();
    }

    private static List<org.parosproxy.paros.core.scanner.NameValuePair> convertNVP(
            List<NameValuePair> nvpList, int type) {
        List<org.parosproxy.paros.core.scanner.NameValuePair> params = new ArrayList<>();
        for (NameValuePair nvp : nvpList) {
            params.add(
                    new org.parosproxy.paros.core.scanner.NameValuePair(
                            type, nvp.getName(), nvp.getValue(), -1));
        }
        return params;
    }

    private static List<NameValuePair> convertParosNVP(
            List<org.parosproxy.paros.core.scanner.NameValuePair> nvpList, int type) {
        List<NameValuePair> params = new ArrayList<>();
        for (org.parosproxy.paros.core.scanner.NameValuePair nvp : nvpList) {
            if (nvp.getType() == type) {
                params.add(new DefaultNameValuePair(nvp.getName(), nvp.getValue()));
            }
        }
        return params;
    }

    public static String regexEscape(String str) {
        String chrsToEscape = ".*+?^=!${}()|[]\\";
        StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if (chrsToEscape.indexOf(c) >= 0) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Returns a regex pattern that will match the specified StructuralNode, ignoring the parent and
     * children. For most nodes this will just be the last element in the path, e.g. URL regex name
     * https://www.example.com/aaa/bbb bbb https://www.example.com/aaa aaa https://www.example.com/
     * https://www.example.com Datadriven nodes are different, they will always return (.+?) to
     * match anything.
     *
     * @param sn a StructuralNode
     * @param incParams if true then include URL params in the regex, otherwise exclude them
     * @return a regex pattern that will match the specified StructuralNode, ignoring the parent and
     *     children.
     */
    public static String getRegexName(StructuralNode sn, boolean incParams) {
        return getSpecifiedName(sn, incParams, true);
    }

    /**
     * Returns the name of the node ignoring the parent and children, i.e. the last element in the
     * path. Data driven nodes will return the user specified name surrounded by the double angled
     * brackets.
     *
     * @param sn a StructuralNode
     * @param incParams if true then include URL params in the regex, otherwise exclude them
     * @return the name of the node ignoring the parent and children
     */
    public static String getCleanRelativeName(StructuralNode sn, boolean incParams) {
        return getSpecifiedName(sn, incParams, false);
    }

    private static String getSpecifiedName(
            StructuralNode sn, boolean incParams, boolean dataDrivenNodesAsRegex) {
        String name = sn.getName();
        if (sn.isDataDriven() && dataDrivenNodesAsRegex) {
            // Non-greedy regex pattern
            return DATA_DRIVEN_NODE_REGEX;
        }
        int bracketIndex = name.lastIndexOf("(");
        if (bracketIndex >= 0) {
            // Strip the param summary off
            name = name.substring(0, bracketIndex);
        }
        int queryIndex = name.indexOf("?");
        if (queryIndex >= 0) {
            if (incParams) {
                // Escape the params
                String params = name.substring(queryIndex);
                name = name.substring(0, queryIndex) + regexEscape(params);
            } else {
                // Strip the parameters off
                name = name.substring(0, queryIndex);
            }
        }

        try {
            if (sn.getURI().getPath() == null || sn.getURI().getPath().length() == 0) {
                // Its a top level node, return as is
                return name;
            }
        } catch (URIException e) {
            // Ignore
        }

        int slashIndex = name.lastIndexOf('/');
        if (slashIndex >= 0) {
            name = name.substring(slashIndex + 1);
        }
        if (sn.isLeaf()) {
            int colonIndex = name.indexOf(":");
            if (colonIndex > 0) {
                // Strip the GET/POST/etc. off
                name = name.substring(colonIndex + 1);
            }
        }
        return name;
    }

    public static String getRegexPattern(StructuralNode sn) throws DatabaseException {
        return getRegexPattern(sn, true);
    }

    public static String getRegexPattern(StructuralNode sn, boolean incChildren)
            throws DatabaseException {
        /*
         * The logic...
         * 	Loop up to parent / recurse up
         * 	for std nodes escape special cases
         * 	inc \/ between nodes
         * 	for NSPs use (.+?) ?
         */

        StringBuilder sb = new StringBuilder();
        boolean incParams = sn.isLeaf() || !incChildren;
        boolean lastLeafReturned = false;

        // Work back up the tree..
        while (!sn.isRoot()) {
            if (lastLeafReturned) {
                sb.insert(0, "/");
            }
            sb.insert(0, getRegexName(sn, incParams));
            sn = sn.getParent();
            incParams = false; // Only do this for the top node
            lastLeafReturned = true;
        }
        if (incChildren) {
            sb.append(".*");
        }
        return sb.toString();
    }

    private static RecordStructure addStructure(
            Model model,
            String host,
            HttpMessage msg,
            List<String> paths,
            int size,
            int historyId,
            boolean newOnly)
            throws DatabaseException, URIException {
        // String nodeUrl = pathsToUrl(host, paths, size);
        Session session = model.getSession();
        String nodeName = getNodeName(session, host, msg, paths, size);
        String parentName = pathsToUrl(host, paths, size - 1);
        String url = "";

        if (msg != null) {
            url = msg.getRequestHeader().getURI().toString();
            String params = getParams(session, msg);
            if (params.length() > 0) {
                nodeName = nodeName + " " + params;
            }
        }

        String method = HttpRequestHeader.GET;
        if (msg != null) {
            method = msg.getRequestHeader().getMethod();
        }

        RecordStructure msgRs =
                model.getDb().getTableStructure().find(session.getSessionId(), nodeName, method);
        if (msgRs == null) {
            long parentId = -1;
            if (!nodeName.equals(ROOT)) {
                HttpMessage tmpMsg = null;
                int parentHistoryId = -1;
                if (!parentName.equals(ROOT)) {
                    tmpMsg = getTempHttpMessage(session, parentName, msg);
                    parentHistoryId = tmpMsg.getHistoryRef().getHistoryId();
                }
                RecordStructure parentRs =
                        addStructure(model, host, tmpMsg, paths, size - 1, parentHistoryId, false);
                parentId = parentRs.getStructureId();
            }
            msgRs =
                    model.getDb()
                            .getTableStructure()
                            .insert(
                                    session.getSessionId(),
                                    parentId,
                                    historyId,
                                    nodeName,
                                    url,
                                    method);
        } else if (newOnly) {
            // Already exists
            return null;
        }

        return msgRs;
    }

    private static HttpMessage getTempHttpMessage(Session session, String url, HttpMessage base) {
        try {
            HttpMessage newMsg = base.cloneRequest();
            URI uri = new URI(url, false);
            newMsg.getRequestHeader().setURI(uri);
            newMsg.getRequestHeader().setMethod(HttpRequestHeader.GET);
            newMsg.getRequestBody().setBody("");
            newMsg.getRequestHeader().setHeader(HttpHeader.CONTENT_TYPE, null);
            newMsg.getRequestHeader().setHeader(HttpHeader.CONTENT_LENGTH, null);

            HistoryReference historyRef =
                    new HistoryReference(session, HistoryReference.TYPE_TEMPORARY, newMsg);
            newMsg.setHistoryRef(historyRef);

            return newMsg;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private static String pathsToUrl(String host, List<String> paths, int size) {
        if (size < 0) {
            return ROOT;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(host);
        int i = 1;
        for (String path : paths) {
            if (i > size) {
                break;
            }
            if (sb.length() > 0 && !path.startsWith("/")) {
                sb.append("/");
            }
            sb.append(path);
            i++;
        }
        return sb.toString();
    }

    public static String getHostName(HttpMessage msg) throws URIException {
        return getHostName(msg.getRequestHeader().getURI());
    }

    public static String getHostName(URI uri) throws URIException {
        StringBuilder host = new StringBuilder();

        String scheme = getScheme(uri);
        host.append(scheme).append("://").append(uri.getHost());

        int port = uri.getPort();
        if (port != -1
                && ((port == 80 && !"http".equals(scheme))
                        || (port == 443 && !"https".equals(scheme)
                                || (port != 80 && port != 443)))) {
            host.append(":").append(port);
        }

        return host.toString();
    }

    private static String getScheme(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null) {
            return uri.getPort() == 443 ? "https" : "http";
        }
        return scheme.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the root node
     *
     * @return the root node
     * @deprecated Use {@link #getRootNode(Model)}
     */
    @Deprecated
    public static StructuralNode getRootNode() {
        return getRootNode(Model.getSingleton());
    }

    /**
     * Returns the root node
     *
     * @param model the model
     * @return the root node
     */
    public static StructuralNode getRootNode(Model model) {
        if (!Constant.isLowMemoryOptionSet()) {
            return new StructuralSiteNode(model.getSession().getSiteTree().getRoot());
        }

        Session session = model.getSession();
        RecordStructure rs;
        try {
            rs =
                    model.getDb()
                            .getTableStructure()
                            .find(session.getSessionId(), ROOT, HttpRequestHeader.GET);
            if (rs != null) {
                return new StructuralTableNode(rs);
            }
        } catch (DatabaseException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private static String getParams(Session session, HttpMessage msg) throws URIException {
        return getParams(
                session,
                msg.getRequestHeader().getURI(),
                msg.getRequestBody().toString(),
                msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE));
    }

    private static String getParams(
            Session session, URI uri, String requestBody, String contentType) throws URIException {
        boolean hasReqBody = contentType != null && requestBody != null && !requestBody.isEmpty();
        String leafParams = getQueryParamString(session.getUrlParameters(uri), hasReqBody);
        if (!hasReqBody) {
            return leafParams;
        }

        if (contentType.startsWith(MULTIPART_FORM_DATA)) {
            leafParams += MULTIPART_FORM_DATA_DISPLAY;
        } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
            leafParams += getQueryParamString(session.getFormParameters(uri, requestBody), false);
        }
        return leafParams;
    }

    private static String getQueryParamString(List<NameValuePair> list, boolean isUrlWithPostData) {
        StringBuilder sb = new StringBuilder();
        list.stream()
                .sorted()
                .forEach(
                        entry -> {
                            String name = entry.getName();
                            if (name != null) {
                                if (sb.length() > 0) {
                                    sb.append(',');
                                }
                                if (name.length() > 40) {
                                    // Truncate
                                    name = name.substring(0, 40) + "...";
                                }
                                sb.append(name);
                            }
                        });
        String result = "";
        if (sb.length() > 0 || isUrlWithPostData) {
            result = sb.insert(0, '(').append(')').toString();
        }

        return result;
    }
}
