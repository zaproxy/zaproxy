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

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;

/**
 * The MaxChildrenFetchFilter defines a filter rule for limiting the number of children explored.
 */
public class MaxChildrenFetchFilter extends FetchFilter {

	private int maxChildren = -1;
	
	private Model model;

	@Override
	public FetchStatus checkFilter(URI uri) {
		log.debug("Checking: " + uri);

		SiteNode parent = model.getSession().getSiteTree().findClosestParent(uri);
		if (parent != null) {
			if (maxChildren > 0 && parent.getChildCount() > maxChildren) {
				return FetchStatus.USER_RULES;
			}
		}

		return FetchStatus.VALID;
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
