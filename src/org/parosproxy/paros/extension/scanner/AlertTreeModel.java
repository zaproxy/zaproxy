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
package org.parosproxy.paros.extension.scanner;

import java.net.URL;

import javax.swing.tree.DefaultTreeModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.view.View;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
class AlertTreeModel extends DefaultTreeModel {

	private int totalInfo = 0;
	private int totalLow = 0;
	private int totalMedium = 0;
	private int totalHigh = 0;
	
    AlertTreeModel() {
        super(new AlertNode(-1, Constant.messages.getString("alerts.tree.title")));	// ZAP: i18n
        
    }
    
    // ZAP: Added resetAlertCounts
    public void resetAlertCounts() {
    	totalInfo = 0;
    	totalLow = 0;
    	totalMedium = 0;
    	totalHigh = 0;

        View.getSingleton().getMainFrame().setAlertHigh(0);
        View.getSingleton().getMainFrame().setAlertMedium(0);
        View.getSingleton().getMainFrame().setAlertLow(0);
        View.getSingleton().getMainFrame().setAlertInfo(0);

    	AlertNode parent = (AlertNode) getRoot();
    	if (parent != null) {
    		parent.resetErrorCount();
    	}
    }
    
    // Zap: Added icons to tree
    private String getRiskString (Alert alert) {
    	// Note that the number comments are to ensure the right ordering in the tree :)
		URL url = null;
    	if (alert.getReliability() == Alert.FALSE_POSITIVE) {
    		url = getClass().getResource("/resource/icon/16/072.png");
    		return "<html><!--5--><img src=\"" + url + "\">&nbsp;" +
    			alert.getAlert() + "<html>"; // 'Green flag' icon
    	}
    	switch (alert.getRisk()) {
    	case Alert.RISK_INFO:
    		url = getClass().getResource("/resource/icon/16/073.png");
    		return "<html><!--4--><img src=\"" + url + "\">&nbsp;" +
    			alert.getAlert() + "<html>"; // 'Blue flag' icon
    	case Alert.RISK_LOW:
    		url = getClass().getResource("/resource/icon/16/074.png");
    		return "<html><!--3--><img src=\"" + url + "\">&nbsp;" +
    			alert.getAlert() + "<html>"; // 'Yellow flag' icon
    	case Alert.RISK_MEDIUM:
    		url = getClass().getResource("/resource/icon/16/076.png");
    		return "<html><!--2--><img src=\"" + url + "\">&nbsp;" +
    			alert.getAlert() + "<html>"; // 'Orange flag' icon
    	case Alert.RISK_HIGH:
    		url = getClass().getResource("/resource/icon/16/071.png");
    		return "<html><!--1--><img src=\"" + url + "\">&nbsp;" +
    			alert.getAlert() + "<html>"; // 'Red flag' icon
        default:
        	return alert.getAlert();
    	}
    }
    
    private synchronized void incAlertCount (Alert alert) {
    	if (alert.getReliability() == Alert.FALSE_POSITIVE) {
    		return;
    	}
    	switch (alert.getRisk()) {
    	case Alert.RISK_INFO:
            View.getSingleton().getMainFrame().setAlertInfo(++totalInfo);
            break;
    	case Alert.RISK_LOW:
            View.getSingleton().getMainFrame().setAlertLow(++totalLow);
            break;
    	case Alert.RISK_MEDIUM:
            View.getSingleton().getMainFrame().setAlertMedium(++totalMedium);
            break;
    	case Alert.RISK_HIGH:
            View.getSingleton().getMainFrame().setAlertHigh(++totalHigh);
            break;
    	}
    }
    
    private synchronized void decAlertCount (Alert alert) {
    	if (alert.getReliability() == Alert.FALSE_POSITIVE) {
    		return;
    	}
    	switch (alert.getRisk()) {
    	case Alert.RISK_INFO:
            View.getSingleton().getMainFrame().setAlertInfo(--totalInfo);
            break;
    	case Alert.RISK_LOW:
            View.getSingleton().getMainFrame().setAlertLow(--totalLow);
            break;
    	case Alert.RISK_MEDIUM:
            View.getSingleton().getMainFrame().setAlertMedium(--totalMedium);
            break;
    	case Alert.RISK_HIGH:
            View.getSingleton().getMainFrame().setAlertHigh(--totalHigh);
            break;
    	}
    }
    
    /**
     * 
     * @param msg
     * @return true if the node is added.  False if not.
     */
    synchronized void addPath(Alert alert) {
        
        AlertNode parent = (AlertNode) getRoot();
        String alertNodeName = getRiskString(alert);;
        
        try {
            parent = findAndAddChild(parent, alertNodeName, null);
            parent = findAndAddLeaf(parent, alert.getUri().toString(), alert);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    private AlertNode findLeafNodeForAlert(AlertNode parent, Alert alert) {
        for (int i=0; i<parent.getChildCount(); i++) {
            AlertNode child = (AlertNode) parent.getChildAt(i);
            if (child.getChildCount() == 0) {
            	// Its a leaf node
	        	if (child.getUserObject() != null && 
	        			((Alert)child.getUserObject()).getAlertId() == alert.getAlertId()) {
	        		return child;
	        	}
            } else {
            	// check its children
	        	AlertNode node = findLeafNodeForAlert(child, alert);
	        	if (node != null) {
	        		return node;
	        	}
            }
        }
    	return null;
    }
    
    synchronized void updatePath(Alert originalAlert, Alert alert) {

    	if (originalAlert !=null) {
    		// Correct the global counts
    		this.decAlertCount(originalAlert);
    	}

        AlertNode node = findLeafNodeForAlert((AlertNode) getRoot(), alert);
        if (node != null) {
        	
        	// Remove the old version
        	AlertNode parent = (AlertNode) node.getParent();
        	if (originalAlert.getReliability() != Alert.FALSE_POSITIVE) {
            	parent.decErrorCount();
        	}

        	this.removeNodeFromParent(node);
            // TODO need to call this.valueForPathChanged(path, newValue) ??
        	
        	if (parent.getChildCount() == 0) {
        		// Parent has no other children, remove it also
        		this.removeNodeFromParent(parent);
        	}
        }
        // Add it back in again
        this.addPath(alert);
    }
    
 
    
    private AlertNode findAndAddChild(AlertNode parent, String nodeName, Alert alert) {
        AlertNode result = findChild(parent, nodeName);
        if (result == null) {
            AlertNode newNode = new AlertNode(-1, nodeName);
            int pos = parent.getChildCount();
            for (int i=0; i< parent.getChildCount(); i++) {
                if (nodeName.compareToIgnoreCase(parent.getChildAt(i).toString()) <= 0) {
                    pos = i;
                    break;
                }
            }
            insertNodeInto(newNode, parent, pos);
            result = newNode;
            result.setUserObject(alert);
        }
        return result;
    }

    private AlertNode findAndAddLeaf(AlertNode parent, String nodeName, Alert alert) {
        AlertNode result = findLeaf(parent, nodeName, alert);
        
        if (result == null) {
            AlertNode newNode = new AlertNode(alert.getRisk(), nodeName);
            int pos = parent.getChildCount();
            for (int i=0; i< parent.getChildCount(); i++) {
            	String childName = ((AlertNode)parent.getChildAt(i)).getNodeName();
                if (nodeName.compareToIgnoreCase(childName) <= 0) {
                    pos = i;
                    break;
                    
                }
            }
            
            insertNodeInto(newNode, parent, pos);
            result = newNode;
            result.setUserObject(alert);

            // Zap: Increment the error counts in alerts the tree
        	if (alert.getReliability() != Alert.FALSE_POSITIVE) {
                newNode.incErrorCount();
                this.incAlertCount(alert);
        	}
        	this.nodeChanged(newNode);
            
        }
        return result;
    }

    
    private AlertNode findChild(AlertNode parent, String nodeName) {
        for (int i=0; i<parent.getChildCount(); i++) {
            AlertNode child = (AlertNode) parent.getChildAt(i);
            if (child.getNodeName().equals(nodeName)) {
                return child;
            }
        }
        return null;
    }

    private AlertNode findLeaf(AlertNode parent, String nodeName, Alert alert) {
        for (int i=0; i<parent.getChildCount(); i++) {
            AlertNode child = (AlertNode) parent.getChildAt(i);
            if (child.getNodeName().equals(nodeName)) {
                if (child.getUserObject() == null) {
                    return null;
                }
                
                Alert tmp = (Alert) child.getUserObject();

                if (tmp.getParam().equals(alert.getParam())) {;
                	return child;
                }
            }
        }
        return null;
    }

    
}
