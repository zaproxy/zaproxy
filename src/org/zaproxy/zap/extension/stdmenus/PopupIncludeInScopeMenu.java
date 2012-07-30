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
package org.zaproxy.zap.extension.stdmenus;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.view.PopupMenuSiteNode;

public class PopupIncludeInScopeMenu extends PopupMenuSiteNode {

	private static final long serialVersionUID = 2282358266003940700L;

	/**
	 * This method initializes 
	 * 
	 */
	public PopupIncludeInScopeMenu() {
		super(Constant.messages.getString("sites.include.scope.popup"), true);
	}
	    
	@Override
	public void performAction(SiteNode sn) throws Exception {
		sn.setIncludedInScope(true, true);

        Session session = Model.getSingleton().getSession();
        String url = new URI(sn.getHierarchicNodeName(), false).toString();
        if (sn.isLeaf()) {
            url = Pattern.quote(url);
        } else {
        	url = Pattern.quote(url) + ".*";
        }
        session.addIncludeInScopeRegex(url);
	}

	@Override
    public void performActions (List<HistoryReference> hrefs) throws Exception {
		super.performActions(hrefs);
	}

	@Override
	public boolean isEnableForInvoker(Invoker invoker) {
		return true;
	}
	
	@Override
    public boolean isEnabledForSiteNode (SiteNode sn) {
		if (sn.isIncludedInScope() || sn.isExcludedFromScope()) {
			// Either explicitly included or excluded, so would have to change that regex in a non trivial way to include!
			return false;
		}
    	return true;
    }

}
