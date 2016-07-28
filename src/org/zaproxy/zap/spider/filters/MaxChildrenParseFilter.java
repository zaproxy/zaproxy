/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.spider.filters;

import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;

/**
 * The MaxChildrenFetchFilter defines a filter rule for limiting the number of children explored.
 */
public class MaxChildrenParseFilter extends ParseFilter {

	private int maxChildren = -1;
	
	private Model model;

	@Override
	public boolean isFiltered(HttpMessage responseMessage) {

		SiteNode parent = model.getSession().getSiteTree().findClosestParent(responseMessage);
		if (parent != null) {
			if (maxChildren > 0 && parent.getChildCount() > maxChildren) {
				return true;
			}
		}

		return false;
	}

	public void setMaxChildren(int maxChildren) {
		this.maxChildren = maxChildren;
	}

	/**
	 * Sets the model
	 * 
	 * @param model the model used to check the number of children of a node
	 */
	public void setModel(Model model) {
		this.model = model;
	}

}
