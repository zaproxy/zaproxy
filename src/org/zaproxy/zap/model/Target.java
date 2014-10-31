/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP Development Team
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

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.SiteNode;

/**
 * The target of a scan 
 * @author simon
 *
 */
public class Target {

	private SiteNode startNode;
	private Context context;
	private boolean inScopeOnly = false;
	private int maxChildren = -1;
	private int maxDepth = -1;
	private boolean recurse = false;
	
	public Target(boolean inScopeOnly) {
		super();
		this.inScopeOnly = inScopeOnly;
	}

	public Target(SiteNode startNode) {
		super();
		this.startNode = startNode;
	}

	public Target(SiteNode startNode, boolean recurse) {
		super();
		this.startNode = startNode;
		this.setRecurse(recurse);
	}

	public Target(Context context) {
		super();
		this.context = context;
	}

	/**
	 * Constructor.
	 * One or both startNode and context should be specified. If both a used then the startNode should 
	 * belong to the context. Note that nodes can belong to multiple contexts. 
	 * @param startNode
	 * @param context
	 * @param inScopeOnly Only scan nodes that are in scope - opnly relevant if context not specified
	 * @param maxChildren
	 * @param maxDepth
	 */
	public Target(SiteNode startNode, Context context, boolean inScopeOnly,
			int maxChildren, int maxDepth) {
		super();
		this.startNode = startNode;
		this.context = context;
		this.inScopeOnly = inScopeOnly;
		this.maxChildren = maxChildren;
		this.maxDepth = maxDepth;
	}

	public Target(SiteNode startNode, Context context, boolean inScopeOnly, boolean recurse) {
		super();
		this.startNode = startNode;
		this.context = context;
		this.inScopeOnly = inScopeOnly;
		this.recurse = recurse;
	}
	
	public boolean isValid() {
		return this.startNode != null || this.context != null || this.inScopeOnly;
	}

	public SiteNode getStartNode() {
		return startNode;
	}
	public Context getContext() {
		return context;
	}
	public boolean isInScopeOnly() {
		return inScopeOnly;
	}
	public int getMaxChildren() {
		return maxChildren;
	}
	public int getMaxDepth() {
		return maxDepth;
	}
	public void setStartNode(SiteNode startNode) {
		this.startNode = startNode;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public void setInScopeOnly(boolean inScopeOnly) {
		this.inScopeOnly = inScopeOnly;
	}
	public void setMaxChildren(int maxChildren) {
		this.maxChildren = maxChildren;
	}
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public boolean isRecurse() {
		return recurse;
	}

	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}
	
    public String getDisplayName() {
    	if (startNode == null) {
    		if (context != null) {
    			return Constant.messages.getString("context.prefixName", new Object[] {context.getName()});
    		} else if (this.inScopeOnly) {
    			return Constant.messages.getString("target.allInScope");
    		} else {
    			return Constant.messages.getString("target.empty");
    		}
    	} else {
    		String name = startNode.getHierarchicNodeName(); 
    		if (name.length() < 30) {
    			return name;
    		}
    		
    		// Just use the first and last 14 chrs to prevent huge urls messing up the display
    		return name.substring(0, 14) + ".." + name.substring(name.length()-15, name.length());
    	}
    }

}
