/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.view;

import org.apache.log4j.Logger;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.view.popup.PopupMenuItemHttpMessageContainer;

/**
 * @deprecated (2.3.0) Superseded by {@link PopupMenuItemHttpMessageContainer}. It will be removed in a future release.
 */
@Deprecated
public abstract class PopupMenuHttpMessage extends PopupMenuHistoryReference {

	private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(PopupMenuHttpMessage.class);

    /**
     * @param label
     */
    public PopupMenuHttpMessage(String label) {
        super(label);
    }

    @Override
    public boolean isEnabledForHistoryReference (HistoryReference href) {
    	try {
			return href != null && this.isEnabledForHttpMessage(href.getHttpMessage());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
    }

    public boolean isEnabledForHttpMessage (HttpMessage msg) {
    	// Can Override if required 
    	return msg != null;
    }
    
    @Override
    public void performAction (HistoryReference href) throws Exception {
    	if (href != null) {
    		this.performAction(href.getHttpMessage());
    	}
    }

    public abstract void performAction (HttpMessage msg) throws Exception;

    @Override
    public abstract boolean isEnableForInvoker(Invoker invoker);

}