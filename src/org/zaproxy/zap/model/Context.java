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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;

public class Context {
	
    private static Logger log = Logger.getLogger(Context.class);
    
	private Session session;
	private int index;
	private String name;
	private String description = "";
	
	private List<String> includeInRegexs = new ArrayList<String>();
	private List<String> excludeFromRegexs = new ArrayList<String>();
    private List<Pattern> includeInPatterns = new ArrayList<Pattern>();
    private List<Pattern> excludeFromPatterns = new ArrayList<Pattern>();
    
    private TechSet techSet = new TechSet(Tech.builtInTech);
    private boolean inScope = true;
    
    public Context(Session session, int index) {
    	this.session = session;
    	this.index = index;
    	this.name = "" + index;
    }

	public boolean isIncludedInScope(SiteNode sn) {
		if (! this.inScope) {
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

	/* Not needed right now, but may be neweded in the future?
	public boolean isExplicitlyIncluded(SiteNode sn) {
		if (sn == null) {
			return false;
		}
		return isExplicitlyIncluded(sn.getHierarchicNodeName());
	}
	
	public boolean isExplicitlyIncluded(String url) {
		if (url == null) {
			return false;
		}
        try {
			return this.includeInPatterns.contains(this.getPatternUrl(url, false)) ||
					this.includeInPatterns.contains(this.getPatternUrl(url, false));
		} catch (Exception e) {
			return false;
		}
	}
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
		if (! this.inScope) {
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
		if (! this.isIncluded(url)) {
			// Not explicitly included
			return false;
		}
		// Check to see if its explicitly excluded
		return ! this.isExcluded(url);
	}

	/**
	 * Gets the nodes from the site tree which are "In Scope". Searches recursively starting from
	 * the root node. Should be used with care, as it is time-consuming, querying the database for
	 * every node in the Site Tree.
	 * 
	 * @return the nodes in scope from site tree
	 */
	public List<SiteNode> getNodesInContextFromSiteTree() {
		List<SiteNode> nodes = new LinkedList<SiteNode>();
		SiteNode rootNode = (SiteNode) session.getSiteTree().getRoot();
		fillNodesInContext(rootNode, nodes);
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
			if (isInContext(sn))
				nodesList.add(sn);
			fillNodesInContext(sn, nodesList);
		}
	}
	
	public List<String> getIncludeInContextRegexs() {
		return includeInRegexs;
	}
	
	private void checkRegexs (List<String> regexs) throws Exception {
	    for (String url : regexs) {
	    	url = url.trim();
	    	if (url.length() > 0) {
				Pattern.compile(url, Pattern.CASE_INSENSITIVE);
	    	}
	    }
	}

	public void setIncludeInContextRegexs(List<String> includeRegexs) throws Exception {
		// Check they are all valid regexes first
		checkRegexs(includeRegexs);
		// Check if theyve been changed
		if (includeInRegexs.size() == includeRegexs.size()) {
			boolean changed = false;
		    for (int i=0; i < includeInRegexs.size(); i++) {
		    	if (! includeInRegexs.get(i).equals(includeRegexs.get(i))) {
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
	
	private String getPatternFromNode(SiteNode sn, boolean recurse) throws Exception {
		return this.getPatternUrl(new URI(sn.getHierarchicNodeName(), false).toString(), recurse);
	}
	
	private String getPatternUrl(String url, boolean recurse) throws Exception {
		if (url.indexOf("?") > 0) {
			// Strip off any parameters
			url = url.substring(0, url.indexOf("?"));
		}
		
        if (recurse) {
        	url = Pattern.quote(url) + ".*";
        } else {
            url = Pattern.quote(url);
        }
        return url;
	}
	
	public void excludeFromContext(SiteNode sn, boolean recurse) throws Exception {
        addExcludeFromContextRegex(this.getPatternFromNode(sn, recurse));
	}
	
	public void addIncludeInContextRegex(String includeRegex) {
		Pattern p = Pattern.compile(includeRegex, Pattern.CASE_INSENSITIVE);
		includeInPatterns.add(p);
		includeInRegexs.add(includeRegex);
	}
	
	public List<String> getExcludeFromContextRegexs() {
		return excludeFromRegexs;
	}

	public void setExcludeFromContextRegexs(List<String> excludeRegexs) throws Exception {
		// Check they are all valid regexes first
		checkRegexs(excludeRegexs);
		// Check if theyve been changed
		if (excludeFromRegexs.size() == excludeRegexs.size()) {
			boolean changed = false;
		    for (int i=0; i < excludeFromRegexs.size(); i++) {
		    	if (! excludeFromRegexs.get(i).equals(excludeRegexs.get(i))) {
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
		Pattern p = Pattern.compile(excludeRegex, Pattern.CASE_INSENSITIVE);
		excludeFromPatterns.add(p);
		excludeFromRegexs.add(excludeRegex);
	}
	
	public void save () {
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

}
