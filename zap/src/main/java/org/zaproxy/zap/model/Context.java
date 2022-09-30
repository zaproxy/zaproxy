/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.tree.TreeNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpStatusCode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.authentication.AuthenticationMethod;
import org.zaproxy.zap.authentication.ManualAuthenticationMethodType.ManualAuthenticationMethod;
import org.zaproxy.zap.extension.authorization.AuthorizationDetectionMethod;
import org.zaproxy.zap.extension.authorization.BasicAuthorizationDetectionMethod;
import org.zaproxy.zap.extension.authorization.BasicAuthorizationDetectionMethod.LogicalOperator;
import org.zaproxy.zap.extension.custompages.CustomPage;
import org.zaproxy.zap.extension.custompages.ExtensionCustomPages;
import org.zaproxy.zap.session.CookieBasedSessionManagementMethodType.CookieBasedSessionManagementMethod;
import org.zaproxy.zap.session.SessionManagementMethod;

public class Context {

    public static final String CONTEXT_CONFIG = "context";
    public static final String CONTEXT_CONFIG_NAME = CONTEXT_CONFIG + ".name";
    public static final String CONTEXT_CONFIG_DESC = CONTEXT_CONFIG + ".desc";
    public static final String CONTEXT_CONFIG_INSCOPE = CONTEXT_CONFIG + ".inscope";
    public static final String CONTEXT_CONFIG_INC_REGEXES = CONTEXT_CONFIG + ".incregexes";
    public static final String CONTEXT_CONFIG_EXC_REGEXES = CONTEXT_CONFIG + ".excregexes";
    public static final String CONTEXT_CONFIG_TECH = CONTEXT_CONFIG + ".tech";
    public static final String CONTEXT_CONFIG_TECH_INCLUDE = CONTEXT_CONFIG_TECH + ".include";
    public static final String CONTEXT_CONFIG_TECH_EXCLUDE = CONTEXT_CONFIG_TECH + ".exclude";
    public static final String CONTEXT_CONFIG_URLPARSER = CONTEXT_CONFIG + ".urlparser";
    public static final String CONTEXT_CONFIG_URLPARSER_CLASS = CONTEXT_CONFIG_URLPARSER + ".class";
    public static final String CONTEXT_CONFIG_URLPARSER_CONFIG =
            CONTEXT_CONFIG_URLPARSER + ".config";
    public static final String CONTEXT_CONFIG_POSTPARSER = CONTEXT_CONFIG + ".postparser";
    public static final String CONTEXT_CONFIG_POSTPARSER_CLASS =
            CONTEXT_CONFIG_POSTPARSER + ".class";
    public static final String CONTEXT_CONFIG_POSTPARSER_CONFIG =
            CONTEXT_CONFIG_POSTPARSER + ".config";
    public static final String CONTEXT_CONFIG_DATA_DRIVEN_NODES = CONTEXT_CONFIG + ".ddns";

    private static Logger log = LogManager.getLogger(Context.class);

    private Session session;
    private int id;
    private String name;
    private String description = "";

    private List<String> includeInRegexs = new ArrayList<>();
    private List<String> excludeFromRegexs = new ArrayList<>();
    private List<Pattern> includeInPatterns = new ArrayList<>();
    private List<Pattern> excludeFromPatterns = new ArrayList<>();
    private List<StructuralNodeModifier> dataDrivenNodes = new ArrayList<>();

    /** The authentication method. */
    private AuthenticationMethod authenticationMethod = null;

    /** The session management method. */
    private SessionManagementMethod sessionManagementMethod;

    /** The authorization detection method used for this context. */
    private AuthorizationDetectionMethod authorizationDetectionMethod;

    private List<CustomPage> customPages = new ArrayList<>();

    private TechSet techSet = new TechSet(Tech.getAll());
    private boolean inScope = true;
    private ParameterParser urlParamParser = new StandardParameterParser();
    private ParameterParser postParamParser = new StandardParameterParser();

    public Context(Session session, int id) {
        this.session = session;
        this.id = id;
        this.name = String.valueOf(id);
        this.sessionManagementMethod = new CookieBasedSessionManagementMethod(id);
        this.authenticationMethod = new ManualAuthenticationMethod(id);
        this.authorizationDetectionMethod =
                new BasicAuthorizationDetectionMethod(null, null, null, LogicalOperator.AND);
        this.urlParamParser.setContext(this);
        this.postParamParser.setContext(this);
    }

    public boolean isIncludedInScope(SiteNode sn) {
        if (!this.inScope) {
            return false;
        }
        return this.isIncluded(sn);
    }

    public boolean isIncluded(SiteNode sn) {
        if (sn == null) {
            return false;
        }
        return isIncluded(sn.getHierarchicNodeName());
    }

    /*
     * Not needed right now, but may be needed in the future? public boolean
     * isExplicitlyIncluded(SiteNode sn) { if (sn == null) { return false; } return
     * isExplicitlyIncluded(sn.getHierarchicNodeName()); }
     *
     * public boolean isExplicitlyIncluded(String url) { if (url == null) { return false; } try {
     * return this.includeInPatterns.contains(this.getPatternUrl(url, false)) ||
     * this.includeInPatterns.contains(this.getPatternUrl(url, false)); } catch (Exception e) {
     * return false; } }
     */

    public boolean isIncluded(String url) {
        if (url == null) {
            return false;
        }
        if (url.indexOf("?") > 0) {
            // Strip off any parameters
            url = url.substring(0, url.indexOf("?"));
        }
        for (Pattern p : this.includeInPatterns) {
            if (p.matcher(url).matches()) {
                return true;
            }
        }
        return false;
    }

    public boolean isExcludedFromScope(SiteNode sn) {
        if (!this.inScope) {
            return false;
        }
        return this.isExcluded(sn);
    }

    public boolean isExcluded(SiteNode sn) {
        if (sn == null) {
            return false;
        }
        return isExcluded(sn.getHierarchicNodeName());
    }

    public boolean isExcluded(String url) {
        if (url == null) {
            return false;
        }
        if (url.indexOf("?") > 0) {
            // Strip off any parameters
            url = url.substring(0, url.indexOf("?"));
        }
        for (Pattern p : this.excludeFromPatterns) {
            if (p.matcher(url).matches()) {
                return true;
            }
        }
        return false;
    }

    public boolean isInContext(HistoryReference href) {
        if (href == null) {
            return false;
        }
        if (href.getSiteNode() != null) {
            return this.isInContext(href.getSiteNode());
        }
        try {
            return this.isInContext(href.getURI().toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean isInContext(SiteNode sn) {
        if (sn == null) {
            return false;
        }
        return isInContext(sn.getHierarchicNodeName());
    }

    public boolean isInContext(String url) {
        if (url.indexOf("?") > 0) {
            // String off any parameters
            url = url.substring(0, url.indexOf("?"));
        }
        if (!this.isIncluded(url)) {
            // Not explicitly included
            return false;
        }
        // Check to see if its explicitly excluded
        return !this.isExcluded(url);
    }

    /**
     * Gets the nodes from the site tree which are "In Scope". Searches recursively starting from
     * the root node. Should be used with care, as it is time-consuming, querying the database for
     * every node in the Site Tree.
     *
     * @return the nodes in scope from site tree
     * @see #hasNodesInContextFromSiteTree()
     */
    public List<SiteNode> getNodesInContextFromSiteTree() {
        List<SiteNode> nodes = new LinkedList<>();
        SiteNode rootNode = session.getSiteTree().getRoot();
        fillNodesInContext(rootNode, nodes);
        return nodes;
    }

    /**
     * Tells whether or not there's at least one node from the sites tree in context.
     *
     * @return {@code true} if the context has at least one node from the sites tree in context,
     *     {@code false} otherwise
     * @since 2.5.0
     * @see #getNodesInContextFromSiteTree()
     */
    public boolean hasNodesInContextFromSiteTree() {
        return hasNodesInContext(session.getSiteTree().getRoot());
    }

    /**
     * Tells whether or not there's at least one node from the sites tree in context.
     *
     * <p>The whole tree is traversed until is found one node in context.
     *
     * @param node the node to check, recursively
     * @return {@code true} if the context has at least one node from the sites tree in context,
     *     {@code false} otherwise
     */
    private boolean hasNodesInContext(SiteNode node) {
        @SuppressWarnings("unchecked")
        Enumeration<TreeNode> en = node.children();
        while (en.hasMoreElements()) {
            SiteNode sn = (SiteNode) en.nextElement();
            if (isInContext(sn)) {
                return true;
            }
            if (hasNodesInContext(sn)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the nodes from the site tree which are "In Scope". Searches recursively starting from
     * the root node. Should be used with care, as it is time-consuming, querying the database for
     * every node in the Site Tree.
     *
     * @return the nodes in scope from site tree
     */
    public List<SiteNode> getTopNodesInContextFromSiteTree() {
        List<SiteNode> nodes = new LinkedList<>();
        SiteNode rootNode = session.getSiteTree().getRoot();
        @SuppressWarnings("unchecked")
        Enumeration<TreeNode> en = rootNode.children();
        while (en.hasMoreElements()) {
            SiteNode sn = (SiteNode) en.nextElement();
            if (isContainsNodesInContext(sn)) {
                nodes.add(sn);
            }
        }
        return nodes;
    }

    /**
     * Fills a given list with nodes in scope, searching recursively.
     *
     * @param rootNode the root node
     * @param nodesList the nodes list
     */
    private void fillNodesInContext(SiteNode rootNode, List<SiteNode> nodesList) {
        @SuppressWarnings("unchecked")
        Enumeration<TreeNode> en = rootNode.children();
        while (en.hasMoreElements()) {
            SiteNode sn = (SiteNode) en.nextElement();
            if (isInContext(sn)) {
                nodesList.add(sn);
            }
            fillNodesInContext(sn, nodesList);
        }
    }

    /**
     * Tells whether or not the given node or any of its child nodes are in context.
     *
     * @param node the node to start the check
     * @return {@code true} if at least one node is in context, {@code false} otherwise
     */
    private boolean isContainsNodesInContext(SiteNode node) {
        if (isInContext(node)) {
            return true;
        }
        @SuppressWarnings("unchecked")
        Enumeration<TreeNode> en = node.children();
        while (en.hasMoreElements()) {
            SiteNode sn = (SiteNode) en.nextElement();
            if (isContainsNodesInContext(sn)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getIncludeInContextRegexs() {
        return Collections.unmodifiableList(includeInRegexs);
    }

    /**
     * Validates that the given regular expressions are valid and they're not {@code null} nor
     * empty.
     *
     * @param regexs the regular expressions to be validated, must not be {@code null} nor empty.
     * @throws IllegalArgumentException if one of the regular expressions is {@code null} or empty.
     * @throws PatternSyntaxException if one of the regular expressions is invalid.
     */
    private static void validateRegexs(List<String> regexs) {
        for (String regex : regexs) {
            validateRegex(regex);
        }
    }

    /**
     * Validates that the given regular expression is valid and it's not {@code null} nor empty.
     *
     * @param regex the regular expression to be validated
     * @throws IllegalArgumentException if the regular expression is {@code null} or empty.
     * @throws PatternSyntaxException if the regular expression is invalid.
     */
    private static void validateRegex(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("The regular expression must not be null.");
        }
        String trimmedRegex = regex.trim();
        if (trimmedRegex.isEmpty()) {
            throw new IllegalArgumentException("The regular expression must not be empty.");
        }
        Pattern.compile(trimmedRegex, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Sets the regular expressions used to include a URL in context.
     *
     * @param includeRegexs the regular expressions
     * @throws IllegalArgumentException if one of the regular expressions is {@code null} or empty.
     * @throws PatternSyntaxException if one of the regular expressions is invalid.
     */
    public void setIncludeInContextRegexs(List<String> includeRegexs) {
        validateRegexs(includeRegexs);
        // Check if they've been changed
        if (includeInRegexs.equals(includeRegexs)) {
            // No point reapplying the same regexs
            return;
        }
        includeInRegexs.clear();
        includeInPatterns.clear();
        for (String url : includeRegexs) {
            url = url.trim();
            if (url.length() > 0) {
                Pattern p = Pattern.compile(url, Pattern.CASE_INSENSITIVE);
                includeInRegexs.add(url);
                includeInPatterns.add(p);
            }
        }
    }

    public void excludeFromContext(SiteNode sn, boolean recurse) throws Exception {
        excludeFromContext(new StructuralSiteNode(sn), recurse);
    }

    public void excludeFromContext(StructuralNode sn, boolean recurse) throws Exception {
        addExcludeFromContextRegex(sn.getRegexPattern(recurse));
    }

    public void addIncludeInContextRegex(String includeRegex) {
        validateRegex(includeRegex);
        includeInPatterns.add(Pattern.compile(includeRegex, Pattern.CASE_INSENSITIVE));
        includeInRegexs.add(includeRegex);
    }

    public List<String> getExcludeFromContextRegexs() {
        return Collections.unmodifiableList(excludeFromRegexs);
    }

    /**
     * Sets the regular expressions used to exclude a URL from the context.
     *
     * @param excludeRegexs the regular expressions
     * @throws IllegalArgumentException if one of the regular expressions is {@code null}.
     * @throws PatternSyntaxException if one of the regular expressions is invalid.
     */
    public void setExcludeFromContextRegexs(List<String> excludeRegexs) {
        validateRegexs(excludeRegexs);
        // Check if they've been changed
        if (excludeFromRegexs.equals(excludeRegexs)) {
            // No point reapplying the same regexs
            return;
        }

        excludeFromRegexs.clear();
        excludeFromPatterns.clear();
        for (String url : excludeRegexs) {
            url = url.trim();
            if (url.length() > 0) {
                Pattern p = Pattern.compile(url, Pattern.CASE_INSENSITIVE);
                excludeFromPatterns.add(p);
                excludeFromRegexs.add(url);
            }
        }
    }

    public void addExcludeFromContextRegex(String excludeRegex) {
        validateRegex(excludeRegex);
        excludeFromPatterns.add(Pattern.compile(excludeRegex, Pattern.CASE_INSENSITIVE));
        excludeFromRegexs.add(excludeRegex);
    }

    public void save() {
        this.session.saveContext(this);
    }

    public TechSet getTechSet() {
        return techSet;
    }

    public void setTechSet(TechSet techSet) {
        this.techSet = techSet;
    }

    /**
     * Gets the name of the context.
     *
     * @return the name of the context, never {@code null} (since 2.6.0).
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the context.
     *
     * @param name the new name of the context
     * @throws IllegalContextNameException (since 2.6.0) if the given name is {@code null} or empty.
     */
    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalContextNameException(
                    IllegalContextNameException.Reason.EMPTY_NAME,
                    "The context name must not be null nor empty.");
        }

        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the ID of the {@code Context}
     *
     * @deprecated (2.9.0) Use {@link #getId()} instead.
     */
    @Deprecated
    public int getIndex() {
        return getId();
    }

    /**
     * Returns the ID of the {@code Context}
     *
     * @since 2.9.0
     */
    public int getId() {
        return this.id;
    }

    public boolean isInScope() {
        return inScope;
    }

    public void setInScope(boolean inScope) {
        this.inScope = inScope;
    }

    /**
     * Gets the authentication method corresponding to this context.
     *
     * @return the authentication method
     */
    public AuthenticationMethod getAuthenticationMethod() {
        return authenticationMethod;
    }

    /**
     * Sets the authentication method corresponding to this context.
     *
     * @param authenticationMethod the new authentication method
     */
    public void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    /**
     * Gets the session management method corresponding to this context.
     *
     * @return the session management method
     */
    public SessionManagementMethod getSessionManagementMethod() {
        return sessionManagementMethod;
    }

    /**
     * Sets the session management method corresponding to this context.
     *
     * @param sessionManagementMethod the new session management method
     */
    public void setSessionManagementMethod(SessionManagementMethod sessionManagementMethod) {
        this.sessionManagementMethod = sessionManagementMethod;
    }

    /**
     * Gets the authorization detection method corresponding to this context.
     *
     * @return the authorization detection method
     */
    public AuthorizationDetectionMethod getAuthorizationDetectionMethod() {
        return authorizationDetectionMethod;
    }

    /**
     * Sets the authorization detection method corresponding to this context.
     *
     * @param authorizationDetectionMethod the new authorization detectionmethod
     */
    public void setAuthorizationDetectionMethod(
            AuthorizationDetectionMethod authorizationDetectionMethod) {
        this.authorizationDetectionMethod = authorizationDetectionMethod;
    }

    public ParameterParser getUrlParamParser() {
        return urlParamParser;
    }

    public void setUrlParamParser(ParameterParser paramParser) {
        this.urlParamParser = paramParser;
    }

    public ParameterParser getPostParamParser() {
        return postParamParser;
    }

    public void setPostParamParser(ParameterParser postParamParser) {
        this.postParamParser = postParamParser;
    }

    public void restructureSiteTree() {
        if (!View.isInitialised() || EventQueue.isDispatchThread()) {
            restructureSiteTreeEventHandler();
        } else {
            try {
                EventQueue.invokeLater(
                        new Runnable() {
                            @Override
                            public void run() {
                                restructureSiteTreeEventHandler();
                            }
                        });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void restructureSiteTreeEventHandler() {
        log.debug("Restructure site tree for context: {}", this.getName());
        List<SiteNode> nodes = this.getTopNodesInContextFromSiteTree();
        for (SiteNode sn : nodes) {
            checkNode(sn);
        }
    }

    private boolean checkNode(SiteNode sn) {
        // Loop backwards through the children TODO change for lowmem!
        // log.debug("checkNode " + sn.getHierarchicNodeName());		// Useful for debugging
        int origChildren = sn.getChildCount();
        int movedChildren = 0;
        for (SiteNode childNode : getChildren(sn)) {
            if (checkNode(childNode)) {
                movedChildren++;
            }
        }

        if (this.isInContext(sn)) {
            SiteMap sitesTree = this.session.getSiteTree();
            HistoryReference href = sn.getHistoryReference();

            try {
                SiteNode sn2;
                if (HttpRequestHeader.POST.equals(href.getMethod())) {
                    // Have to go to the db as POST data can be used in the name
                    sn2 = sitesTree.findNode(href.getHttpMessage());
                } else {
                    // This is better as it doesn't require a db read
                    sn2 = sitesTree.findNode(href.getURI());
                }

                if (sn2 == null
                        || !sn.getHierarchicNodeName().equals(sn2.getHierarchicNodeName())) {
                    if (!sn.isDataDriven()) {
                        moveNode(sitesTree, sn);
                        return true;
                    }
                }
                if (movedChildren > 0 && movedChildren == origChildren && sn.getChildCount() == 0) {
                    if (href.getHistoryType() == HistoryReference.TYPE_TEMPORARY) {
                        // Remove temp old node, no need to add new one in -
                        // that will happen when moving child nodes (if required)
                        deleteNode(sitesTree, sn);
                        return true;
                    }
                }
                // log.debug("Didn't need to move " + sn.getHierarchicNodeName());	// Useful for
                // debugging
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * Gets the child nodes of the given site node.
     *
     * @param siteNode the site node that will be used, must not be {@code null}
     * @return a {@code List} with the child nodes, never {@code null}
     */
    private List<SiteNode> getChildren(SiteNode siteNode) {
        int childCount = siteNode.getChildCount();
        if (childCount == 0) {
            return Collections.emptyList();
        }

        List<SiteNode> children = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            children.add((SiteNode) siteNode.getChildAt(i));
        }
        return children;
    }

    private void moveNode(SiteMap sitesTree, SiteNode sn) {
        List<Alert> alerts = sn.getAlerts();

        // And delete the old one
        deleteNode(sitesTree, sn);

        // Add into the right place
        SiteNode sn2 = sitesTree.addPath(sn.getHistoryReference());
        log.debug("Moved node {} to {}", sn.getHierarchicNodeName(), sn2.getHierarchicNodeName());

        // And sort out the alerts
        for (Alert alert : alerts) {
            sn2.addAlert(alert);
        }
    }

    private void deleteNode(SiteMap sitesTree, SiteNode sn) {
        log.debug("Deleting node {}", sn.getHierarchicNodeName());
        sn.deleteAlerts(sn.getAlerts());

        // Remove old one
        sitesTree.removeNodeFromParent(sn);
        sitesTree.removeHistoryReference(sn.getHistoryReference().getHistoryId());
    }

    public List<StructuralNodeModifier> getDataDrivenNodes() {
        List<StructuralNodeModifier> ddns = new ArrayList<>(this.dataDrivenNodes.size());
        for (StructuralNodeModifier ddn : this.dataDrivenNodes) {
            ddns.add(ddn.clone());
        }
        return ddns;
    }

    public void setDataDrivenNodes(List<StructuralNodeModifier> dataDrivenNodes) {
        this.dataDrivenNodes = dataDrivenNodes;
    }

    public void addDataDrivenNodes(StructuralNodeModifier ddn) {
        this.dataDrivenNodes.add(ddn.clone());
    }

    public String getDefaultDDNName() {
        int i = 1;
        while (true) {
            boolean found = false;
            String name = "DDN" + i;
            for (StructuralNodeModifier ddn : this.dataDrivenNodes) {
                if (ddn.getName().equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return name;
            }

            i++;
        }
    }

    /**
     * Gets an unmodifiable view of the list of custom pages.
     *
     * @return a List of custom pages
     */
    public List<CustomPage> getCustomPages() {
        return Collections.unmodifiableList(customPages);
    }

    /**
     * Returns {@code true} if the {@code Context} has Custom Pages.
     *
     * @return {@code true} if this context has Custom Pages, {@code false} otherwise.
     */
    public boolean hasCustomPages() {
        return !customPages.isEmpty();
    }

    /**
     * Returns {@code true} if the {@code Context} has Custom Page definitions of a specific {@code
     * CustomPage.Type}.
     *
     * @return {@code true} if this context has Custom Pages, {@code false} otherwise.
     */
    public boolean hasCustomPageOfType(CustomPage.Type cpType) {
        if (!hasCustomPages()) {
            return false;
        }
        for (CustomPage cp : customPages) {
            if (cp.getType().equals(cpType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets a new list of custom pages for this context. An internal copy of the provided list is
     * stored.
     *
     * @param customPages the list of custom pages
     */
    public void setCustomPages(List<CustomPage> customPages) {
        this.customPages = new ArrayList<>(customPages);
    }

    /**
     * Adds a custom page.
     *
     * @param customPage the custom page being added
     */
    public void addCustomPage(CustomPage customPage) {
        if (customPage != null) {
            this.customPages.add(customPage);
        }
    }

    /**
     * Removes a custom page.
     *
     * @param customPage the defaultCustomPage to be removed
     */
    public boolean removeCustomPage(CustomPage customPage) {
        return this.customPages.remove(customPage);
    }

    /** Removes all the custom pages. */
    public void removeAllCustomPages() {
        this.customPages.clear();
    }

    /**
     * Determines if a {@code HttpMessage} is a Custom Page of a particular {@code CustomPage.Type}.
     *
     * @param msg the HTTP message to be evaluated
     * @param cpType the CustomPage.Type of the Custom Pages against which the HTTP message should
     *     be evaluated
     * @return {@code true} if the HTTP message is a Custom Page of the type in question, {@code
     *     false} otherwise
     * @since 2.10.0
     * @see #isCustomPageWithFallback(HttpMessage,
     *     org.zaproxy.zap.extension.custompages.CustomPage.Type)
     */
    public boolean isCustomPage(HttpMessage msg, CustomPage.Type cpType) {
        return isCustomPage(msg, cpType, false);
    }

    /**
     * Determines if a {@code HttpMessage} is a Custom Page of a particular {@code CustomPage.Type}.
     * Falling back to check the message's status code.
     *
     * @param msg the HTTP message to be evaluated
     * @param cpType the CustomPage.Type of the Custom Pages against which the HTTP message should
     *     be evaluated
     * @return {@code true} if the HTTP message is a Custom Page of the type in question or the
     *     response has a relevant status code (500, 404, etc), {@code false} otherwise
     * @since 2.10.0
     * @see #isCustomPage(HttpMessage, org.zaproxy.zap.extension.custompages.CustomPage.Type)
     */
    public boolean isCustomPageWithFallback(HttpMessage msg, CustomPage.Type cpType) {
        return isCustomPage(msg, cpType, true);
    }

    private boolean isCustomPage(HttpMessage msg, CustomPage.Type cpType, boolean fallback) {
        for (CustomPage customPage : customPages) {
            if (customPage.isCustomPage(msg, cpType)) {
                return true;
            }
        }

        if (fallback) {
            return statusCodeFallback(msg, cpType);
        }
        return false;
    }

    private boolean statusCodeFallback(HttpMessage msg, CustomPage.Type cpType) {
        switch (cpType) {
            case ERROR_500:
                return msg.getResponseHeader().getStatusCode()
                        == HttpStatusCode.INTERNAL_SERVER_ERROR;
            case NOTFOUND_404:
                return msg.getResponseHeader().getStatusCode() == HttpStatusCode.NOT_FOUND;
            case OK_200:
                return msg.getResponseHeader().getStatusCode() == HttpStatusCode.OK;
            case AUTH_4XX:
                return ExtensionCustomPages.AUTH_HTTP_STATUS_CODES.contains(
                        msg.getResponseHeader().getStatusCode());
            default:
                return false;
        }
    }

    /**
     * Creates a copy of the Context. The copy is deep, with the exception of the TechSet.
     *
     * @return the context
     */
    public Context duplicate() {
        Context newContext = new Context(session, getId());
        newContext.description = this.description;
        newContext.name = this.name;
        newContext.includeInRegexs = new ArrayList<>(this.includeInRegexs);
        newContext.includeInPatterns = new ArrayList<>(this.includeInPatterns);
        newContext.excludeFromRegexs = new ArrayList<>(this.excludeFromRegexs);
        newContext.excludeFromPatterns = new ArrayList<>(this.excludeFromPatterns);
        newContext.inScope = this.inScope;
        newContext.techSet = new TechSet(this.techSet);
        newContext.authenticationMethod = this.authenticationMethod.clone();
        newContext.sessionManagementMethod = this.sessionManagementMethod.clone();
        newContext.urlParamParser = this.urlParamParser.clone();
        newContext.postParamParser = this.postParamParser.clone();
        newContext.authorizationDetectionMethod = this.authorizationDetectionMethod.clone();
        newContext.dataDrivenNodes = this.getDataDrivenNodes();
        newContext.setCustomPages(getCustomPages());
        return newContext;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Context other = (Context) obj;
        if (id != other.id) return false;
        return true;
    }
}
