/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 The ZAP Development team
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
package org.zaproxy.zap.extension.alert;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AlertNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;
	private String nodeName = null;
    private int risk = -1;
    
	public AlertNode(int risk, String nodeName) {
        super();
        this.nodeName = nodeName;
        this.setRisk(risk);
    }
    
    @Override
    public String toString() {
    	if (this.getChildCount() > 1) {
            return nodeName + " (" + this.getChildCount() + ")";
    	}
        return nodeName;
    }

	public String getNodeName() {
		return nodeName;
	}
	
	public void setRisk(int risk) {
		this.risk = risk;
	}

	public int getRisk() {
		return risk;
	}

}
