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
 *   http://www.apache.org/licenses/LICENSE-2.0 
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

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.authentication.AuthenticationMethod;
import org.zaproxy.zap.authentication.ManualAuthenticationMethodType.ManualAuthenticationMethod;
import org.zaproxy.zap.extension.authorization.AuthorizationDetectionMethod;
import org.zaproxy.zap.extension.authorization.BasicAuthorizationDetectionMethod;
import org.zaproxy.zap.extension.authorization.BasicAuthorizationDetectionMethod.LogicalOperator;
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
	public static final String CONTEXT_CONFIG_URLPARSER_CONFIG = CONTEXT_CONFIG_URLPARSER + ".config";
	public static final String CONTEXT_CONFIG_POSTPARSER = CONTEXT_CONFIG + ".postparser";
	public static final String CONTEXT_CONFIG_POSTPARSER_CLASS = CONTEXT_CONFIG_POSTPARSER + ".class";
	public static final String CONTEXT_CONFIG_POSTPARSER_CONFIG = CONTEXT_CONFIG_POSTPARSER + ".config";
	public static final String CONTEXT_CONFIG_DATA_DRIVEN_NODES = CONTEXT_CONFIG + ".ddns";

	private static Logger log = Logger.getLogger(Context.class);

	private Session session;
	private int index;
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
	
	private TechSet techSet = new TechSet(Tech.builtInTech);
	private boolean inScope = true;
	private ParameterParser urlParamParser = new StandardParameterParser();
	private ParameterParser postParamParser = new StandardParameterParser();

	public Context(Session session, int index) {
		this.session = session;
		this.index = index;
		this.name = String.valueOf(index);
		this.sessionManagementMethod = new CookieBasedSessionManagementMethod(index);
		this.authenticationMethod = new ManualAuthenticationMethod(index);
		this.authorizationDetectionMethod = new BasicAuthorizationDetectionMethod(null, null, null,
				LogicalOperator.AND);
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
		SiteNode rootNode = (SiteNode) session.getSiteTree().getRoot();
		fillNodesInContext(rootNode, nodes);
		return nodes;
	}

	/**
	 * Tells whether or not there's at least one node from the sites tree in context.
	 * 
	 * @return {@code true} if the context has at least one node from the sites tree in context, {@code false} otherwise
	 * @since 2.5.0
	 * @see #getNodesInContextFromSiteTree()
	 */
	public boolean hasNodesInContextFromSiteTree() {
		return hasNodesInContext((SiteNode) session.getSiteTree().getRoot());
	}

	/**
	 * Tells whether or not there's at least one node from the sites tree in context.
	 * <p>
	 * The whole tree is traversed until is found one node in context.
	 * 
	 * @param node the node to check, recursively
	 * @return {@code true} if the context has at least one node from the sites tree in context, {@code false} otherwise
	 */
	private boolean hasNodesInContext(SiteNode node) {
		@SuppressWarnings("unchecked")
		Enumeration<SiteNode> en = node.children();
		while (en.hasMoreElements()) {
			SiteNode sn = en.nextElement();
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
		SiteNode rootNode = (SiteNode) session.getSiteTree().getRoot();
		@SuppressWarnings("unchecked")
		Enumeration<SiteNode> en = rootNode.children();
		while (en.hasMoreElements()) {
			SiteNode sn = en.nextElement();
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
		Enumeration<SiteNode> en = rootNode.children();
		while (en.hasMoreElements()) {
			SiteNode sn = en.nextElement();
			if (isInContext(sn)) {
				nodesList.add(sn);
			}
			fillNodesInContext(sn, nodesList);
		}
	}

	/**
	 * Fills a given list with nodes in scope, searching recursively.
	 * 
	 * @param rootNode the root node
	 * @param nodesList the nodes list
	 */
	private boolean isContainsNodesInContext(SiteNode node) {
		if (isInContext(node)) {
			return true;
		}
		@SuppressWarnings("unchecked")
		Enumeration<SiteNode> en = node.children();
		while (en.hasMoreElements()) {
			SiteNode sn = en.nextElement();
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
	 * Validates that the given regular expressions are not {@code null} nor invalid.
	 *
	 * @param regexs the regular expressions to be validated, must not be {@code null}
	 * @throws IllegalArgumentException if one of the regular expressions is {@code null}.
	 * @throws PatternSyntaxException if one of the regular expressions is invalid.
	 */
	private static void validateRegexs(List<String> regexs) {
		for (String regex : regexs) {
			validateRegex(regex);
		}
	}

	/**
	 * Validates that the given regular expression is not {@code null} nor invalid.
	 *
	 * @param regex the regular expression to be validated
	 * @throws IllegalArgumentException if the regular expression is {@code null}.
	 * @throws PatternSyntaxException if the regular expression is invalid.
	 */
	private static void validateRegex(String regex) {
		if (regex == null) {
			throw new IllegalArgumentException("The regular expression must not be null.");
		}
		String trimmedRegex = regex.trim();
		if (!trimmedRegex.isEmpty()) {
			Pattern.compile(trimmedRegex, Pattern.CASE_INSENSITIVE);
		}
	}

	/**
	 * Sets the regular expressions used to include a URL in context.
	 *
	 * @param includeRegexs the regular expressions
	 * @throws IllegalArgumentException if one of the regular expressions is {@code null}.
	 * @throws PatternSyntaxException if one of the regular expressions is invalid.
	 */
	public void setIncludeInContextRegexs(List<String> includeRegexs) {
		validateRegexs(includeRegexs);
		// Check if theyve been changed
		if (includeInRegexs.size() == includeRegexs.size()) {
			boolean changed = false;
			for (int i = 0; i < includeInRegexs.size(); i++) {
				if (!includeInRegexs.get(i).equals(includeRegexs.get(i))) {
					changed = true;
					break;
				}
			}
			if (!changed) {
				// No point reapplying the same regexs
				return;
			}
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
		// Check if theyve been changed
		if (excludeFromRegexs.size() == excludeRegexs.size()) {
			boolean changed = false;
			for (int i = 0; i < excludeFromRegexs.size(); i++) {
				if (!excludeFromRegexs.get(i).equals(excludeRegexs.get(i))) {
					changed = true;
					break;
				}
			}
			if (!changed) {
				// No point reapplying the same regexs
				return;
			}
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getIndex() {
		return this.index;
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
	public void setAuthorizationDetectionMethod(AuthorizationDetectionMethod authorizationDetectionMethod) {
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
        if (EventQueue.isDispatchThread()) {
        	restructureSiteTreeEventHandler();
        } else {
            try {
                EventQueue.invokeLater(new Runnable() {
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
		log.debug("Restructure site tree for context: " + this.getName());
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
					// This is better as it doesnt require a db read
					sn2 = sitesTree.findNode(href.getURI());
				}
				
				if (sn2 == null || ! sn.getHierarchicNodeName().equals(sn2.getHierarchicNodeName())) {
					if (! sn.isDataDriven()) {
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
				// log.debug("Didnt need to move " + sn.getHierarchicNodeName());	// Useful for debugging
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

	private void moveNode (SiteMap sitesTree, SiteNode sn) {
		List<Alert> alerts = sn.getAlerts();
		
		// And delete the old one
		deleteNode(sitesTree, sn);
		
		// Add into the right place
		SiteNode sn2 = sitesTree.addPath(sn.getHistoryReference());
		log.debug("Moved node " + sn.getHierarchicNodeName() + " to " + sn2.getHierarchicNodeName());

		// And sort out the alerts
		for (Alert alert : alerts) {
			sn2.addAlert(alert);
		}
	}
	
	private void deleteNode (SiteMap sitesTree, SiteNode sn) {
		log.debug("Deleting node " + sn.getHierarchicNodeName());
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
		int i=1;
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
	 * Creates a copy of the Context. The copy is deep, with the exception of the TechSet.
	 * 
	 * @return the context
	 */
	public Context duplicate() {
		Context newContext = new Context(session, getIndex());
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
		return newContext;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Context other = (Context) obj;
		if (index != other.index)
			return false;
		return true;
	}
	
	

}
