/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.extension.spider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.PatternSyntaxException;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiException.Type;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;

/** @deprecated (2.12.0) See the spider add-on in zap-extensions instead. */
@Deprecated
public class SpiderAPI extends ApiImplementor {

    private static final Logger log = LogManager.getLogger(SpiderAPI.class);

    /** The Constant PREFIX defining the name/prefix of the api. */
    private static final String PREFIX = "spider";
    /** The Constant ACTION_START_SCAN that defines the action of starting a new scan. */
    private static final String ACTION_START_SCAN = "scan";

    private static final String ACTION_START_SCAN_AS_USER = "scanAsUser";

    private static final String ACTION_PAUSE_SCAN = "pause";
    private static final String ACTION_RESUME_SCAN = "resume";
    /** The Constant ACTION_STOP_SCAN that defines the action of stopping a pending scan. */
    private static final String ACTION_STOP_SCAN = "stop";

    private static final String ACTION_PAUSE_ALL_SCANS = "pauseAllScans";
    private static final String ACTION_RESUME_ALL_SCANS = "resumeAllScans";
    private static final String ACTION_STOP_ALL_SCANS = "stopAllScans";
    private static final String ACTION_REMOVE_SCAN = "removeScan";
    private static final String ACTION_REMOVE_ALL_SCANS = "removeAllScans";

    private static final String ACTION_ADD_DOMAIN_ALWAYS_IN_SCOPE = "addDomainAlwaysInScope";
    private static final String ACTION_MODIFY_DOMAIN_ALWAYS_IN_SCOPE = "modifyDomainAlwaysInScope";
    private static final String ACTION_REMOVE_DOMAIN_ALWAYS_IN_SCOPE = "removeDomainAlwaysInScope";
    private static final String ACTION_ENABLE_ALL_DOMAINS_ALWAYS_IN_SCOPE =
            "enableAllDomainsAlwaysInScope";
    private static final String ACTION_DISABLE_ALL_DOMAINS_ALWAYS_IN_SCOPE =
            "disableAllDomainsAlwaysInScope";

    /**
     * The Constant VIEW_STATUS that defines the view which describes the current status of the
     * scan.
     */
    private static final String VIEW_STATUS = "status";

    /**
     * The Constant VIEW_RESULTS that defines the view which describes the urls found during the
     * scan.
     */
    private static final String VIEW_RESULTS = "results";

    private static final String VIEW_FULL_RESULTS = "fullResults";
    private static final String VIEW_SCANS = "scans";
    private static final String VIEW_ALL_URLS = "allUrls";
    private static final String VIEW_ADDED_NODES = "addedNodes";

    private static final String VIEW_DOMAINS_ALWAYS_IN_SCOPE = "domainsAlwaysInScope";
    private static final String VIEW_OPTION_DOMAINS_ALWAYS_IN_SCOPE = "optionDomainsAlwaysInScope";
    private static final String VIEW_OPTION_DOMAINS_ALWAYS_IN_SCOPE_ENABLED =
            "optionDomainsAlwaysInScopeEnabled";

    /** The Constant PARAM_URL that defines the parameter defining the url of the scan. */
    private static final String PARAM_URL = "url";

    private static final String PARAM_USER_ID = "userId";
    private static final String PARAM_CONTEXT_ID = "contextId";
    private static final String PARAM_CONTEXT_NAME = "contextName";
    private static final String PARAM_REGEX = "regex";
    private static final String PARAM_RECURSE = "recurse";
    private static final String PARAM_SCAN_ID = "scanId";
    private static final String PARAM_MAX_CHILDREN = "maxChildren";
    private static final String PARAM_SUBTREE_ONLY = "subtreeOnly";
    private static final String PARAM_VALUE = "value";
    private static final String PARAM_IDX = "idx";
    private static final String PARAM_IS_REGEX = "isRegex";
    private static final String PARAM_IS_ENABLED = "isEnabled";

    private static final String ACTION_EXCLUDE_FROM_SCAN = "excludeFromScan";
    private static final String ACTION_CLEAR_EXCLUDED_FROM_SCAN = "clearExcludedFromScan";

    private static final String VIEW_EXCLUDED_FROM_SCAN = "excludedFromScan";

    /** The spider extension. */
    private ExtensionSpider extension;

    /**
     * Instantiates a new spider API.
     *
     * @param extension the extension
     */
    public SpiderAPI(ExtensionSpider extension) {
        this.extension = extension;
        // Register the actions
        this.addApiAction(
                new ApiAction(
                        ACTION_START_SCAN,
                        null,
                        new String[] {
                            PARAM_URL,
                            PARAM_MAX_CHILDREN,
                            PARAM_RECURSE,
                            PARAM_CONTEXT_NAME,
                            PARAM_SUBTREE_ONLY
                        }));
        this.addApiAction(
                new ApiAction(
                        ACTION_START_SCAN_AS_USER,
                        new String[] {PARAM_CONTEXT_ID, PARAM_USER_ID},
                        new String[] {
                            PARAM_URL, PARAM_MAX_CHILDREN, PARAM_RECURSE, PARAM_SUBTREE_ONLY
                        }));
        this.addApiAction(new ApiAction(ACTION_PAUSE_SCAN, new String[] {PARAM_SCAN_ID}));
        this.addApiAction(new ApiAction(ACTION_RESUME_SCAN, new String[] {PARAM_SCAN_ID}));
        this.addApiAction(new ApiAction(ACTION_STOP_SCAN, null, new String[] {PARAM_SCAN_ID}));
        this.addApiAction(new ApiAction(ACTION_REMOVE_SCAN, new String[] {PARAM_SCAN_ID}));
        this.addApiAction(new ApiAction(ACTION_PAUSE_ALL_SCANS));
        this.addApiAction(new ApiAction(ACTION_RESUME_ALL_SCANS));
        this.addApiAction(new ApiAction(ACTION_STOP_ALL_SCANS));
        this.addApiAction(new ApiAction(ACTION_REMOVE_ALL_SCANS));
        this.addApiAction(new ApiAction(ACTION_CLEAR_EXCLUDED_FROM_SCAN));
        this.addApiAction(new ApiAction(ACTION_EXCLUDE_FROM_SCAN, new String[] {PARAM_REGEX}));

        this.addApiAction(
                new ApiAction(
                        ACTION_ADD_DOMAIN_ALWAYS_IN_SCOPE,
                        new String[] {PARAM_VALUE},
                        new String[] {PARAM_IS_REGEX, PARAM_IS_ENABLED}));
        this.addApiAction(
                new ApiAction(
                        ACTION_MODIFY_DOMAIN_ALWAYS_IN_SCOPE,
                        new String[] {PARAM_IDX},
                        new String[] {PARAM_VALUE, PARAM_IS_REGEX, PARAM_IS_ENABLED}));
        this.addApiAction(
                new ApiAction(ACTION_REMOVE_DOMAIN_ALWAYS_IN_SCOPE, new String[] {PARAM_IDX}));
        this.addApiAction(new ApiAction(ACTION_ENABLE_ALL_DOMAINS_ALWAYS_IN_SCOPE));
        this.addApiAction(new ApiAction(ACTION_DISABLE_ALL_DOMAINS_ALWAYS_IN_SCOPE));

        // Register the views
        this.addApiView(new ApiView(VIEW_STATUS, null, new String[] {PARAM_SCAN_ID}));
        this.addApiView(new ApiView(VIEW_RESULTS, null, new String[] {PARAM_SCAN_ID}));
        this.addApiView(new ApiView(VIEW_FULL_RESULTS, new String[] {PARAM_SCAN_ID}));
        this.addApiView(new ApiView(VIEW_SCANS));
        this.addApiView(new ApiView(VIEW_EXCLUDED_FROM_SCAN));
        this.addApiView(new ApiView(VIEW_ALL_URLS));
        this.addApiView(new ApiView(VIEW_ADDED_NODES, null, new String[] {PARAM_SCAN_ID}));

        this.addApiView(new ApiView(VIEW_DOMAINS_ALWAYS_IN_SCOPE));
        ApiView view = new ApiView(VIEW_OPTION_DOMAINS_ALWAYS_IN_SCOPE);
        view.setDeprecated(true);
        this.addApiView(view);
        view = new ApiView(VIEW_OPTION_DOMAINS_ALWAYS_IN_SCOPE_ENABLED);
        view.setDeprecated(true);
        this.addApiView(view);
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        log.debug("Request for handleApiAction: {} (params: {})", name, params);
        SpiderScan scan;
        int maxChildren = -1;
        Context context = null;

        switch (name) {
            case ACTION_START_SCAN:
                // The action is to start a new Scan
                String url = ApiUtils.getOptionalStringParam(params, PARAM_URL);
                if (params.containsKey(PARAM_MAX_CHILDREN)) {
                    String maxChildrenStr = params.getString(PARAM_MAX_CHILDREN);
                    if (maxChildrenStr != null && maxChildrenStr.length() > 0) {
                        try {
                            maxChildren = Integer.parseInt(maxChildrenStr);
                        } catch (NumberFormatException e) {
                            throw new ApiException(Type.ILLEGAL_PARAMETER, PARAM_MAX_CHILDREN);
                        }
                    }
                }
                if (params.containsKey(PARAM_CONTEXT_NAME)) {
                    String contextName = params.getString(PARAM_CONTEXT_NAME);
                    if (!contextName.isEmpty()) {
                        context = ApiUtils.getContextByName(contextName);
                    }
                }
                int scanId =
                        scanURL(
                                url,
                                null,
                                maxChildren,
                                this.getParam(params, PARAM_RECURSE, true),
                                context,
                                getParam(params, PARAM_SUBTREE_ONLY, false));
                return new ApiResponseElement(name, Integer.toString(scanId));

            case ACTION_START_SCAN_AS_USER:
                // The action is to start a new Scan from the perspective of a user
                String urlUserScan = ApiUtils.getOptionalStringParam(params, PARAM_URL);
                int userID = ApiUtils.getIntParam(params, PARAM_USER_ID);
                ExtensionUserManagement usersExtension =
                        Control.getSingleton()
                                .getExtensionLoader()
                                .getExtension(ExtensionUserManagement.class);
                if (usersExtension == null) {
                    throw new ApiException(Type.NO_IMPLEMENTOR, ExtensionUserManagement.NAME);
                }
                context = ApiUtils.getContextByParamId(params, PARAM_CONTEXT_ID);
                User user =
                        usersExtension
                                .getContextUserAuthManager(context.getId())
                                .getUserById(userID);
                if (user == null) {
                    throw new ApiException(Type.USER_NOT_FOUND, PARAM_USER_ID);
                }
                if (params.containsKey(PARAM_MAX_CHILDREN)) {
                    String maxChildrenStr = params.getString(PARAM_MAX_CHILDREN);
                    if (maxChildrenStr != null && maxChildrenStr.length() > 0) {
                        try {
                            maxChildren = Integer.parseInt(maxChildrenStr);
                        } catch (NumberFormatException e) {
                            throw new ApiException(Type.ILLEGAL_PARAMETER, PARAM_MAX_CHILDREN);
                        }
                    }
                }
                scanId =
                        scanURL(
                                urlUserScan,
                                user,
                                maxChildren,
                                this.getParam(params, PARAM_RECURSE, true),
                                context,
                                getParam(params, PARAM_SUBTREE_ONLY, false));

                return new ApiResponseElement(name, Integer.toString(scanId));

            case ACTION_PAUSE_SCAN:
                scan = getSpiderScan(params);
                extension.pauseScan(scan.getScanId());
                break;
            case ACTION_RESUME_SCAN:
                scan = getSpiderScan(params);
                extension.resumeScan(scan.getScanId());
                break;
            case ACTION_STOP_SCAN:
                // The action is to stop a pending scan
                scan = getSpiderScan(params);
                extension.stopScan(scan.getScanId());
                break;
            case ACTION_REMOVE_SCAN:
                // Note that we're removing the scan with this call, not just getting it ;)
                scan = getSpiderScan(params);
                extension.removeScan(scan.getScanId());
                break;
            case ACTION_PAUSE_ALL_SCANS:
                extension.pauseAllScans();
                break;
            case ACTION_RESUME_ALL_SCANS:
                extension.resumeAllScans();
                break;
            case ACTION_STOP_ALL_SCANS:
                extension.stopAllScans();
                break;
            case ACTION_REMOVE_ALL_SCANS:
                extension.removeAllScans();
                break;
            case ACTION_CLEAR_EXCLUDED_FROM_SCAN:
                try {
                    Session session = Model.getSingleton().getSession();
                    session.setExcludeFromSpiderRegexs(new ArrayList<>());
                } catch (DatabaseException e) {
                    throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
                }
                break;
            case ACTION_EXCLUDE_FROM_SCAN:
                String regex = params.getString(PARAM_REGEX);
                try {
                    Session session = Model.getSingleton().getSession();
                    session.addExcludeFromSpiderRegex(regex);
                } catch (DatabaseException e) {
                    throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
                } catch (PatternSyntaxException e) {
                    throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_REGEX);
                }
                break;

            case ACTION_ADD_DOMAIN_ALWAYS_IN_SCOPE:
                try {
                    String value = params.getString(PARAM_VALUE);
                    org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher domainAlwaysInScope;
                    if (getParam(params, PARAM_IS_REGEX, false)) {
                        domainAlwaysInScope =
                                new org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher(
                                        org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher
                                                .createPattern(value));
                    } else {
                        domainAlwaysInScope =
                                new org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher(value);
                    }
                    domainAlwaysInScope.setEnabled(getParam(params, PARAM_IS_ENABLED, true));

                    List<org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher> domainsAlwaysInScope =
                            new ArrayList<>(extension.getSpiderParam().getDomainsAlwaysInScope());
                    domainsAlwaysInScope.add(domainAlwaysInScope);
                    extension.getSpiderParam().setDomainsAlwaysInScope(domainsAlwaysInScope);
                } catch (IllegalArgumentException e) {
                    throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_VALUE, e);
                }
                break;
            case ACTION_MODIFY_DOMAIN_ALWAYS_IN_SCOPE:
                try {
                    int idx = params.getInt(PARAM_IDX);
                    if (idx < 0
                            || idx >= extension.getSpiderParam().getDomainsAlwaysInScope().size()) {
                        throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_IDX);
                    }

                    org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher oldDomain =
                            extension.getSpiderParam().getDomainsAlwaysInScope().get(idx);
                    String value = getParam(params, PARAM_VALUE, oldDomain.getValue());
                    if (value.isEmpty()) {
                        value = oldDomain.getValue();
                    }

                    org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher newDomain;
                    if (getParam(params, PARAM_IS_REGEX, oldDomain.isRegex())) {
                        newDomain =
                                new org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher(
                                        org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher
                                                .createPattern(value));
                    } else {
                        newDomain = new org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher(value);
                    }
                    newDomain.setEnabled(getParam(params, PARAM_IS_ENABLED, oldDomain.isEnabled()));

                    if (oldDomain.equals(newDomain)) {
                        break;
                    }

                    List<org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher> domainsAlwaysInScope =
                            new ArrayList<>(extension.getSpiderParam().getDomainsAlwaysInScope());
                    domainsAlwaysInScope.set(idx, newDomain);
                    extension.getSpiderParam().setDomainsAlwaysInScope(domainsAlwaysInScope);
                } catch (JSONException e) {
                    throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_IDX, e);
                } catch (IllegalArgumentException e) {
                    throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_VALUE, e);
                }
                break;
            case ACTION_REMOVE_DOMAIN_ALWAYS_IN_SCOPE:
                try {
                    int idx = params.getInt(PARAM_IDX);
                    if (idx < 0
                            || idx >= extension.getSpiderParam().getDomainsAlwaysInScope().size()) {
                        throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_IDX);
                    }

                    List<org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher> domainsAlwaysInScope =
                            new ArrayList<>(extension.getSpiderParam().getDomainsAlwaysInScope());
                    domainsAlwaysInScope.remove(idx);
                    extension.getSpiderParam().setDomainsAlwaysInScope(domainsAlwaysInScope);
                } catch (JSONException e) {
                    throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_IDX, e);
                }
                break;
            case ACTION_ENABLE_ALL_DOMAINS_ALWAYS_IN_SCOPE:
                setDomainsAlwaysInScopeEnabled(true);
                break;
            case ACTION_DISABLE_ALL_DOMAINS_ALWAYS_IN_SCOPE:
                setDomainsAlwaysInScopeEnabled(false);
                break;
            default:
                throw new ApiException(ApiException.Type.BAD_ACTION);
        }
        return ApiResponseElement.OK;
    }

    private void setDomainsAlwaysInScopeEnabled(boolean enabled) {
        List<org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher> domainsAlwaysInScope =
                extension.getSpiderParam().getDomainsAlwaysInScope();
        for (org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher x :
                extension.getSpiderParam().getDomainsAlwaysInScope()) {
            x.setEnabled(enabled);
        }
        extension.getSpiderParam().setDomainsAlwaysInScope(domainsAlwaysInScope);
    }

    /**
     * Returns the specified GenericScanner2 or the last scan available.
     *
     * @param params the parameters of the API call
     * @return the GenericScanner2 with the given scan ID or, if not present, the last scan
     *     available
     * @throws ApiException if there's no scan with the given scan ID
     * @see #PARAM_SCAN_ID
     */
    private SpiderScan getSpiderScan(JSONObject params) throws ApiException {
        SpiderScan spiderScan;
        int id = getParam(params, PARAM_SCAN_ID, -1);
        if (id == -1) {
            spiderScan = extension.getLastScan();
        } else {
            spiderScan = extension.getScan(id);
        }

        if (spiderScan == null) {
            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_SCAN_ID);
        }

        return spiderScan;
    }

    /**
     * Starts a spider scan at the given {@code url} and, optionally, with the perspective of the
     * given {@code user}.
     *
     * @param url the url to start the spider scan
     * @param user the user to scan as, or null if the scan is done without the perspective of any
     *     user
     * @param maxChildren Max number of children to scan
     * @param recurse Whether or not to scan recursively
     * @param context the context that will be used during spider process, might be {@code null}
     * @param subtreeOnly if the scan should be done only under a site's subtree
     * @return the ID of the newly started scan
     * @throws ApiException if the {@code url} is not valid
     */
    private int scanURL(
            String url,
            User user,
            int maxChildren,
            boolean recurse,
            Context context,
            boolean subtreeOnly)
            throws ApiException {
        log.debug("API Spider scanning url: {}", url);

        boolean useUrl = true;
        if (url == null || url.isEmpty()) {
            if (context == null || !context.hasNodesInContextFromSiteTree()) {
                throw new ApiException(Type.MISSING_PARAMETER, PARAM_URL);
            }
            useUrl = false;
        } else if (context != null && !context.isInContext(url)) {
            throw new ApiException(Type.URL_NOT_IN_CONTEXT, PARAM_URL);
        }

        StructuralNode node = null;
        URI startURI = null;
        if (useUrl) {
            try {
                // Try to build uri
                startURI = new URI(url, true);
            } catch (URIException e) {
                throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_URL);
            }
            String scheme = startURI.getScheme();
            if (scheme == null
                    || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_URL);
            }

            node = getStartNode(startURI, recurse);
        }
        Target target = new Target();
        if (useUrl && node != null) {
            target.setStartNode(node);
        }
        target.setContext(context);
        target.setRecurse(recurse);

        switch (Control.getSingleton().getMode()) {
            case safe:
                throw new ApiException(ApiException.Type.MODE_VIOLATION);
            case protect:
                if ((useUrl && !Model.getSingleton().getSession().isInScope(url))
                        || (context != null && !context.isInScope())) {
                    throw new ApiException(ApiException.Type.MODE_VIOLATION);
                }
                // No problem
                break;
            case standard:
                // No problem
                break;
            case attack:
                // No problem
                break;
        }

        List<Object> objs = new ArrayList<>(4);
        if (startURI != null) {
            objs.add(startURI);
            if (subtreeOnly) {
                objs.add(new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(startURI));
            }
        }

        if (maxChildren > 0) {
            // Add the filters to filter on maximum number of children
            org.zaproxy.zap.spider.filters.MaxChildrenFetchFilter maxChildrenFetchFilter =
                    new org.zaproxy.zap.spider.filters.MaxChildrenFetchFilter();
            maxChildrenFetchFilter.setMaxChildren(maxChildren);
            maxChildrenFetchFilter.setModel(extension.getModel());

            org.zaproxy.zap.spider.filters.MaxChildrenParseFilter maxChildrenParseFilter =
                    new org.zaproxy.zap.spider.filters.MaxChildrenParseFilter(
                            extension.getMessages());
            maxChildrenParseFilter.setMaxChildren(maxChildren);
            maxChildrenParseFilter.setModel(extension.getModel());
            objs.add(maxChildrenFetchFilter);
            objs.add(maxChildrenParseFilter);
        }

        return extension.startScan(target, user, objs.toArray(new Object[objs.size()]));
    }

    /**
     * Gets a node for the given URI and recurse value, for use as start/seed of the spider.
     *
     * <p>If {@code recurse} is {@code true} it returns a node that represents a directory (if
     * present in the session) otherwise the node represents a GET node of the URI. If neither of
     * the nodes is found in the session it returns {@code null}.
     *
     * @param startURI the starting URI, must not be {@code null}.
     * @param recurse {@code true} if the spider should use existing URLs as seeds, {@code false}
     *     otherwise.
     * @return the start node, might be {@code null}.
     * @throws ApiException if an error occurred while obtaining the start node.
     */
    private StructuralNode getStartNode(URI startURI, boolean recurse) throws ApiException {
        StructuralNode startNode = null;
        try {
            if (recurse) {
                startNode = SessionStructure.find(Model.getSingleton(), startURI, "", "");
            }
            if (startNode == null) {
                startNode = SessionStructure.find(Model.getSingleton(), startURI, "GET", "");
            }
        } catch (Exception e) {
            throw new ApiException(ApiException.Type.INTERNAL_ERROR, e);
        }
        return startNode;
    }

    @Override
    public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
        ApiResponse result;
        if (VIEW_STATUS.equals(name)) {
            SpiderScan scan = this.getSpiderScan(params);
            int progress = 0;
            if (scan.isStopped()) {
                progress = 100;
            } else {
                progress = scan.getProgress();
            }
            result = new ApiResponseElement(name, Integer.toString(progress));
        } else if (VIEW_RESULTS.equals(name)) {
            result = new ApiResponseList(name);
            SpiderScan scan = this.getSpiderScan(params);
            synchronized (scan.getResults()) {
                for (String s : scan.getResults()) {
                    ((ApiResponseList) result).addItem(new ApiResponseElement("url", s));
                }
            }
        } else if (VIEW_FULL_RESULTS.equals(name)) {
            ApiResponseList resultUrls = new ApiResponseList(name);
            SpiderScan scan = this.getSpiderScan(params);
            ApiResponseList resultList = new ApiResponseList("urlsInScope");
            synchronized (scan.getResourcesFound()) {
                for (SpiderResource sr : scan.getResourcesFound()) {
                    resultList.addItem(
                            createApiResponseSet(sr, sr.isProcessed(), sr.getReasonNotProcessed()));
                }
            }
            resultUrls.addItem(resultList);

            resultList = new ApiResponseList("urlsOutOfScope");
            synchronized (scan.getResultsOutOfScope()) {
                for (String url : scan.getResultsOutOfScope()) {
                    resultList.addItem(new ApiResponseElement("url", url));
                }
            }
            resultUrls.addItem(resultList);

            resultList = new ApiResponseList("urlsIoError");
            synchronized (scan.getResourcesIoErrors()) {
                for (SpiderResource sr : scan.getResourcesIoErrors()) {
                    resultList.addItem(
                            createApiResponseSet(sr, sr.isProcessed(), sr.getReasonNotProcessed()));
                }
            }
            resultUrls.addItem(resultList);
            result = resultUrls;
        } else if (VIEW_EXCLUDED_FROM_SCAN.equals(name)) {
            result = new ApiResponseList(name);
            Session session = Model.getSingleton().getSession();
            List<String> regexs = session.getExcludeFromSpiderRegexs();
            for (String regex : regexs) {
                ((ApiResponseList) result).addItem(new ApiResponseElement("regex", regex));
            }
        } else if (VIEW_SCANS.equals(name)) {
            ApiResponseList resultList = new ApiResponseList(name);
            for (SpiderScan spiderScan : extension.getAllScans()) {
                Map<String, String> map = new HashMap<>();
                map.put("id", Integer.toString(spiderScan.getScanId()));
                map.put("progress", Integer.toString(spiderScan.getProgress()));
                map.put("state", spiderScan.getState());
                resultList.addItem(new ApiResponseSet<>("scan", map));
            }
            result = resultList;
        } else if (VIEW_ALL_URLS.equals(name)) {
            ApiResponseList resultUrls = new ApiResponseList(name);
            Set<String> urlSet = new HashSet<>();

            TableHistory tableHistory = extension.getModel().getDb().getTableHistory();
            List<Integer> ids = Collections.emptyList();

            try {
                ids =
                        tableHistory.getHistoryIdsOfHistType(
                                extension.getModel().getSession().getSessionId(),
                                HistoryReference.TYPE_SPIDER,
                                HistoryReference.TYPE_SPIDER_TASK);
            } catch (DatabaseException e) {
                throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
            }

            String url;
            for (Integer id : ids) {
                try {
                    RecordHistory rh = tableHistory.read(id);
                    if (rh != null) {
                        url = rh.getHttpMessage().getRequestHeader().getURI().toString();
                        if (urlSet.add(url)) {
                            resultUrls.addItem(new ApiResponseElement("url", url));
                        }
                    }
                } catch (HttpMalformedHeaderException | DatabaseException e) {
                    throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
                }
            }

            result = resultUrls;
        } else if (VIEW_ADDED_NODES.equals(name)) {
            result = new ApiResponseList(name);
            SpiderScan scan = this.getSpiderScan(params);
            for (String s : scan.getAddedNodesTableModel().getAddedNodes()) {
                ((ApiResponseList) result).addItem(new ApiResponseElement("url", s));
            }
        } else if (VIEW_DOMAINS_ALWAYS_IN_SCOPE.equals(name)
                || VIEW_OPTION_DOMAINS_ALWAYS_IN_SCOPE.equals(name)) {
            result =
                    domainMatchersToApiResponseList(
                            name, extension.getSpiderParam().getDomainsAlwaysInScope(), false);
        } else if (VIEW_OPTION_DOMAINS_ALWAYS_IN_SCOPE_ENABLED.equals(name)) {
            result =
                    domainMatchersToApiResponseList(
                            name, extension.getSpiderParam().getDomainsAlwaysInScope(), true);
        } else {
            throw new ApiException(ApiException.Type.BAD_VIEW);
        }
        return result;
    }

    private static ApiResponseSet<String> createApiResponseSet(
            SpiderResource sr, boolean processed, String reasonNotProcessed) {
        Map<String, String> map = new HashMap<>();
        map.put("messageId", Integer.toString(sr.getHistoryId()));
        map.put("method", sr.getMethod());
        map.put("url", sr.getUri());
        map.put("statusCode", Integer.toString(sr.getStatusCode()));
        map.put("statusReason", sr.getStatusReason());
        map.put("processed", Boolean.toString(processed));
        map.put("reasonNotProcessed", reasonNotProcessed);
        return new ApiResponseSet<>("resource", map);
    }

    private ApiResponse domainMatchersToApiResponseList(
            String name,
            List<org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher> domains,
            boolean excludeDisabled) {
        ApiResponseList apiResponse = new ApiResponseList(name);
        for (int i = 0; i < domains.size(); i++) {
            org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher domain = domains.get(i);
            if (!domain.isEnabled() && excludeDisabled) {
                continue;
            }
            Map<String, Object> domainData = new HashMap<>();
            domainData.put("idx", i);
            domainData.put("value", domain.getValue());
            domainData.put("regex", domain.isRegex());
            domainData.put("enabled", domain.isEnabled());
            apiResponse.addItem(new ApiResponseSet<>("domain", domainData));
        }
        return apiResponse;
    }
}
