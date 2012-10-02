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

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.PopupMenuSiteNode;

public abstract class PopupContextMenu extends PopupMenuSiteNode {

	private static final long serialVersionUID = 2282358266003940700L;
	
	private Context context;
	private String parentMenu;

	/**
	 * This method initializes 
	 * 
	 */
	public PopupContextMenu() {
		super(Constant.messages.getString("context.new.title"), true);
		this.context = null;
		this.setPrecedeWithSeparator(true);
	}
	    
	public PopupContextMenu(Context context, String parentMenu, String name) {
		super(name, true);
		this.context = context;
		this.parentMenu = parentMenu;
	}
	
    @Override
    public String getParentMenuName() {
    	return parentMenu;
    }
    
    @Override
    public boolean isSubMenu() {
    	return true;
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
    public boolean isSafe() {
    	return true;
    }
    
    protected Context getContext() {
    	return this.context;
    }
}
