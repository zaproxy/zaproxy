/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.zaproxy.zap.extension.ascan;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AlertNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;
	private String nodeName = null;
    // ZAP: Added risk and errorCount for the flag icons
    private int risk = -1;
    private int errorCount = 0;
    
	public AlertNode(int risk, String nodeName) {
        super();
        this.nodeName = nodeName;
        this.setRisk(risk);
    }
    
    public String toString() {
    	if (errorCount > 0) {
            return nodeName + " (" + errorCount + ")";
    	}
        return nodeName;
    }

	public String getNodeName() {
		return nodeName;
	}
	
    public int getErrorCount() {
		return errorCount;
	}
    
    public void resetErrorCount() {
		errorCount = 0;
	}
    
    public void incErrorCount() {
    	this.errorCount++;
    	if (this.getParent() != null) {
    		((AlertNode)this.getParent()).incErrorCount();
    	}
    }
    
    public void decErrorCount() {
    	this.errorCount--;
    	if (this.getParent() != null) {
    		((AlertNode)this.getParent()).decErrorCount();
    	}
    }

	public void setRisk(int risk) {
		this.risk = risk;
	}

	public int getRisk() {
		return risk;
	}

}
